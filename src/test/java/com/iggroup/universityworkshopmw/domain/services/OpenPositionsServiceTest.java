package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.caches.MarketDataCache;
import com.iggroup.universityworkshopmw.domain.exceptions.InsufficientFundsException;
import com.iggroup.universityworkshopmw.domain.exceptions.MissingBuySizeException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoAvailableDataException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoMarketPriceAvailableException;
import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenPositionsServiceTest {

   private OpenPosition openPosition1, openPosition2, openPosition3, openPosition4, openPosition5, openPosition6;
   private OpenPositionsService openPositionsService;
   private ClientService clientService;
   private MarketDataCache marketDataCache;

   @Before
   public void setUp() {
      clientService = mock(ClientService.class);
      marketDataCache = mock(MarketDataCache.class);
      openPositionsService = new OpenPositionsService(clientService, marketDataCache);
      initialiseOpenPositions();
   }

   @Test
   public void openPositionsAreCorrectForClient() throws Exception {
      initialiseClientPositions();
      List<OpenPosition> openPositions = openPositionsService.getOpenPositionsForClient("client_2");

      assertAddedOpenPosition(openPositions.get(1), openPosition2);
      assertAddedOpenPosition(openPositions.get(0), openPosition3);
      assertAddedOpenPosition(openPositions.get(2), openPosition5);
   }

   @Test(expected = NoAvailableDataException.class)
   public void getShouldReturnNullIfClientHasNoOpenPositions() throws Exception {
      assertThat(openPositionsService.getOpenPositionsForClient("client_1")).isNull();
   }

   @Test
   public void shouldAddOpenPositionsForClientWithNoPositions() throws Exception {
      when(clientService.getClientData("client_1")).thenReturn(createClient("client_1"));

      openPositionsService.addOpenPositionForClient("client_1", openPosition1);

      List<OpenPosition> positions = singletonList(openPosition1);
      List<OpenPosition> clientPositions = openPositionsService.getOpenPositionsForClient("client_1");
      IntStream.range(0, clientPositions.size())
            .forEach(idx -> {
               try {
                  assertNewOpenPosition(clientPositions.get(idx), positions.get(idx));
               } catch (Exception e) {
                  e.printStackTrace();
               }
            });
   }

   @Test
   public void shouldAddNewPositionForClientWithExistingPositions() throws Exception {
      when(clientService.getClientData("client_3")).thenReturn(createClient("client_3"));
      when(marketDataCache.getCurrentPriceForMarket(openPosition1.getMarketId())).thenReturn(openPosition1.getOpeningPrice());
      when(marketDataCache.getCurrentPriceForMarket(openPosition5.getMarketId())).thenReturn(openPosition5.getOpeningPrice());

      openPositionsService.addOpenPositionForClient("client_3", openPosition1);
      openPositionsService.addOpenPositionForClient("client_3", openPosition5);

      List<OpenPosition> clientPositions = openPositionsService.getOpenPositionsForClient("client_3");
      assertAddedOpenPosition(clientPositions.get(0), openPosition1);
      assertAddedOpenPosition(clientPositions.get(1), openPosition5);
   }

   @Test(expected = InsufficientFundsException.class)
   public void shouldThrowExceptionIfClientLacksFundsToTrade() throws Exception {
      when(marketDataCache.getCurrentPriceForMarket(openPosition1.getMarketId())).thenReturn(openPosition1.getOpeningPrice());
      when(clientService.getClientData("client_1")).thenReturn(Client.builder()
            .id("client_1")
            .userName("username")
            .availableFunds(1)
            .build());

      openPositionsService.addOpenPositionForClient("client_1", openPosition1);
   }

   @Test
   public void addOpenPositionForClient_updatedAvailableFunds() throws NoMarketPriceAvailableException, NoAvailableDataException, MissingBuySizeException {
      when(marketDataCache.getCurrentPriceForMarket(openPosition1.getMarketId())).thenReturn(openPosition1.getOpeningPrice());
      when(clientService.getClientData("client_3")).thenReturn(createClient("client_3"));

      openPositionsService.addOpenPositionForClient("client_3", openPosition1);

      ArgumentCaptor<Double> availableFundsCaptor = ArgumentCaptor.forClass(Double.class);
      ArgumentCaptor<String> clientIdCaptor = ArgumentCaptor.forClass(String.class);
      verify(clientService, times(1)).updateAvailableFunds(clientIdCaptor.capture(), availableFundsCaptor.capture());
      assertThat(clientIdCaptor.getValue()).isEqualTo("client_3");
      assertThat(availableFundsCaptor.getValue()).isEqualTo(11145.0);
   }

   @Test
   public void shouldUpdateProfitAndLossForAllClientsWhenPriceIncreases() throws Exception {
      initialiseClientPositions();
      openPositionsService.updateMarketPrice("market_2", 200.00);

      List<OpenPosition> client1Positions = openPositionsService.getOpenPositionsForClient("client_1");
      List<OpenPosition> client2Positions = openPositionsService.getOpenPositionsForClient("client_2");

      assertThat(client1Positions.get(1).getProfitAndLoss()).isEqualTo(60.00);
      assertThat(client2Positions.get(1).getProfitAndLoss()).isEqualTo(60.00);
   }

   @Test(expected = MissingBuySizeException.class)
   public void shouldThrowExceptionIfBuySizeIsZeroForAnOpenPosition() throws NoAvailableDataException, NoMarketPriceAvailableException, MissingBuySizeException {
      openPositionsService.addOpenPositionForClient("clientId", OpenPosition.builder().buySize(0).build());
   }

   @Test
   public void shouldUpdateProfitAndLossForAllClientsWhenPriceDecreases() throws Exception {
      initialiseClientPositions();
      openPositionsService.updateMarketPrice("market_2", 100.00);

      List<OpenPosition> client1Positions = openPositionsService.getOpenPositionsForClient("client_1");
      List<OpenPosition> client2Positions = openPositionsService.getOpenPositionsForClient("client_2");

      assertThat(client1Positions.get(1).getProfitAndLoss()).isEqualTo(-40.00);
      assertThat(client2Positions.get(1).getProfitAndLoss()).isEqualTo(-40.00);
   }

   @Test
   public void shouldUpdateProfitAndLossForMultiplePositionsOnTheSameMarket() throws Exception {
      initialiseClientPositions();
      when(marketDataCache.getCurrentPriceForMarket(openPosition6.getMarketId())).thenReturn(openPosition6.getOpeningPrice());
      openPositionsService.addOpenPositionForClient("client_1", openPosition6);

      openPositionsService.updateMarketPrice("market_1", 200.00);
      List<OpenPosition> openPositions = openPositionsService.getOpenPositionsForClient("client_1");

      assertThat(openPositions.get(0).getProfitAndLoss()).isEqualTo(1200.00);
      assertThat(openPositions.get(2).getProfitAndLoss()).isEqualTo(-600.00);
   }

   @Test
   public void shouldUpdateProfitAndLossForMultiplePositionsOnTheSameMarketForMultipleClients() throws Exception {
      initialiseClientPositions();
      when(marketDataCache.getCurrentPriceForMarket(openPosition6.getMarketId())).thenReturn(openPosition6.getOpeningPrice());
      openPositionsService.addOpenPositionForClient("client_1", openPosition6);
      when(marketDataCache.getCurrentPriceForMarket(openPosition6.getMarketId())).thenReturn(openPosition6.getOpeningPrice());
      openPositionsService.addOpenPositionForClient("client_2", openPosition6);
      when(marketDataCache.getCurrentPriceForMarket(openPosition1.getMarketId())).thenReturn(openPosition1.getOpeningPrice());
      openPositionsService.addOpenPositionForClient("client_2", openPosition1);

      openPositionsService.updateMarketPrice("market_1", 200.00);
      List<OpenPosition> client1Positions = openPositionsService.getOpenPositionsForClient("client_1");
      List<OpenPosition> client2Positions = openPositionsService.getOpenPositionsForClient("client_2");

      assertThat(client1Positions.get(0).getProfitAndLoss()).isEqualTo(1200.00);
      assertThat(client1Positions.get(2).getProfitAndLoss()).isEqualTo(-600.00);
      assertThat(client2Positions.get(3).getProfitAndLoss()).isEqualTo(-600.00);
      assertThat(client2Positions.get(4).getProfitAndLoss()).isEqualTo(1200.00);
   }

   @Test
   public void shouldCloseSpecifiedPosition() throws Exception {
      initialiseClientPositions();
      when(marketDataCache.getCurrentPriceForMarket(openPosition1.getMarketId())).thenReturn(200.0);
      List<OpenPosition> clientPositions = openPositionsService.getOpenPositionsForClient("client_1");
      assertAddedOpenPosition(clientPositions.get(0), openPosition1);
      assertAddedOpenPosition(clientPositions.get(1), openPosition2);

      Double finalProfitAndLoss = openPositionsService.closeOpenPosition("client_1", clientPositions.get(0).getId());
      clientPositions = openPositionsService.getOpenPositionsForClient("client_1");

      assertThat(finalProfitAndLoss).isEqualTo(1200.00);
      assertAddedOpenPosition(clientPositions.get(0), openPosition2);
      assertThat(clientPositions.contains(openPosition1)).isFalse();
   }

   @Test(expected = NoAvailableDataException.class)
   public void shouldThrowExceptionWhenClosingPositionThatDoesntExist() throws Exception {
      openPositionsService.closeOpenPosition("client_1", "made_up_position");
   }

   @Test
   public void closeOpenPosition_calculatesAndUpdatedAvailableFunds() throws Exception {
      when(clientService.getClientData("client_1")).thenReturn(createClient("client_1"));
      when(marketDataCache.getCurrentPriceForMarket(openPosition1.getMarketId())).thenReturn(250.0);
      openPositionsService.addOpenPositionForClient("client_1", openPosition1);
      List<OpenPosition> clientPositions = openPositionsService.getOpenPositionsForClient("client_1");

      openPositionsService.closeOpenPosition("client_1", clientPositions.get(0).getId());

      ArgumentCaptor<String> clientIdCaptor2 = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Double> runningProfitAndLossCaptor = ArgumentCaptor.forClass(Double.class);
      verify(clientService, times(1)).updateRunningProfitAndLoss(clientIdCaptor2.capture(), runningProfitAndLossCaptor.capture());
      assertThat(clientIdCaptor2.getAllValues()).containsOnly("client_1");
      assertThat(runningProfitAndLossCaptor.getValue()).isEqualTo(0);

      ArgumentCaptor<String> clientIdCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Double> availableFundsCaptor = ArgumentCaptor.forClass(Double.class);
      verify(clientService, times(2)).updateAvailableFunds(clientIdCaptor.capture(), availableFundsCaptor.capture());
      assertThat(clientIdCaptor.getAllValues()).containsOnly("client_1");
      assertThat(availableFundsCaptor.getAllValues()).contains(15345.0);
   }

   @Test
   public void updateMarketPrice_shouldUpdateClientRunningProfitAndLossAndAvailableFunds() throws Exception {
      initialiseClientPositions();

      openPositionsService.updateMarketPrice("market_1", 200.00);

      ArgumentCaptor<String> clientIdCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Double> sumProfitAndLossCaptor = ArgumentCaptor.forClass(Double.class);
      verify(clientService, times(2)).updateRunningProfitAndLoss(clientIdCaptor.capture(), sumProfitAndLossCaptor.capture());
      assertThat(clientIdCaptor.getAllValues()).containsOnly("client_1", "client_3");
      assertThat(sumProfitAndLossCaptor.getAllValues()).containsOnly(1200.0, 18950.0);
   }

   private void initialiseOpenPositions() {
      openPosition1 = OpenPosition.builder()
            .id("pos_1")
            .marketId("market_1")
            .profitAndLoss(1234.55)
            .openingPrice(100.00)
            .buySize(12)
            .build();

      openPosition2 = OpenPosition.builder()
            .id("pos_2")
            .marketId("market_2")
            .profitAndLoss(124.12)
            .openingPrice(140.00)
            .buySize(1)
            .build();

      openPosition3 = OpenPosition.builder()
            .id("pos_3")
            .marketId("market_3")
            .profitAndLoss(12.1)
            .openingPrice(50.5)
            .buySize(70)
            .build();

      openPosition4 = OpenPosition.builder()
            .id("pos_4")
            .marketId("market_1")
            .profitAndLoss(134.00)
            .openingPrice(10.5)
            .buySize(100)
            .build();

      openPosition5 = OpenPosition.builder()
            .id("pos_5")
            .marketId("market_4")
            .profitAndLoss(543.98)
            .openingPrice(234.42)
            .buySize(50)
            .build();

      openPosition6 = OpenPosition.builder()
            .id("pos_6")
            .marketId("market_1")
            .profitAndLoss(12.00)
            .openingPrice(250.00)
            .buySize(12)
            .build();
   }

   private void initialiseClientPositions() throws Exception {
      when(clientService.getClientData("client_1")).thenReturn(createClient("client_1"));
      when(clientService.getClientData("client_2")).thenReturn(createClient("client_2"));
      when(clientService.getClientData("client_3")).thenReturn(createClient("client_3"));

      newArrayList(openPosition1, openPosition2)
            .forEach(openPosition -> {
               try {
                  mockMarketDataCacheCall(openPosition);
                  openPositionsService.addOpenPositionForClient("client_1", openPosition);
               } catch (NoAvailableDataException | NoMarketPriceAvailableException | MissingBuySizeException e) {
                  e.printStackTrace();
               }
            });

      newArrayList(openPosition3, openPosition2, openPosition5)
            .forEach(openPosition -> {
               try {
                  mockMarketDataCacheCall(openPosition);
                  openPositionsService.addOpenPositionForClient("client_2", openPosition);
               } catch (NoAvailableDataException | NoMarketPriceAvailableException | MissingBuySizeException e) {
                  e.printStackTrace();
               }
            });

      newArrayList(openPosition4)
            .forEach(openPosition -> {
               try {
                  mockMarketDataCacheCall(openPosition);
                  openPositionsService.addOpenPositionForClient("client_3", openPosition);
               } catch (NoAvailableDataException | NoMarketPriceAvailableException | MissingBuySizeException e) {
                  e.printStackTrace();
               }
            });
   }

   private void mockMarketDataCacheCall(OpenPosition openPosition) throws NoMarketPriceAvailableException {
      when(marketDataCache.getCurrentPriceForMarket(openPosition.getMarketId())).thenReturn(openPosition.getOpeningPrice());
   }

   private Client createClient(String clientId) {
      return Client.builder()
            .id(clientId)
            .userName("username")
            .availableFunds(12345)
            .runningProfitAndLoss(500)
            .build();
   }

   private void assertNewOpenPosition(OpenPosition resultingOpenPosition, OpenPosition openPosition) {
      assertThat(resultingOpenPosition.getProfitAndLoss()).isEqualTo(0.0);
      assertThat(resultingOpenPosition.getBuySize()).isEqualTo(openPosition.getBuySize());
      assertThat(resultingOpenPosition.getMarketId()).isEqualTo(openPosition.getMarketId());
      assertThat(resultingOpenPosition.getOpeningPrice()).isEqualTo(0.0);
   }

   private void assertAddedOpenPosition(OpenPosition resultingOpenPosition, OpenPosition openPosition) {
      assertThat(resultingOpenPosition.getProfitAndLoss()).isEqualTo(0.0);
      assertThat(resultingOpenPosition.getBuySize()).isEqualTo(openPosition.getBuySize());
      assertThat(resultingOpenPosition.getMarketId()).isEqualTo(openPosition.getMarketId());
      assertThat(resultingOpenPosition.getOpeningPrice()).isEqualTo(openPosition.getOpeningPrice());
   }

}
