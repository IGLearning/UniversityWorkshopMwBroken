package com.iggroup.universityworkshopmw.integration;

import com.iggroup.universityworkshopmw.TestHelper;
import com.iggroup.universityworkshopmw.domain.caches.MarketDataCache;
import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.domain.services.ClientService;
import com.iggroup.universityworkshopmw.domain.services.OpenPositionsService;
import com.iggroup.universityworkshopmw.integration.controllers.OpenPositionsController;
import com.iggroup.universityworkshopmw.integration.dto.AddOpenPositionDto;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.iggroup.universityworkshopmw.TestHelper.APPLICATION_JSON_UTF8;
import static com.iggroup.universityworkshopmw.TestHelper.convertObjectToJsonBytes;
import static com.iggroup.universityworkshopmw.domain.services.ClientService.INITIAL_FUNDS;
import static com.iggroup.universityworkshopmw.integration.transformers.OpenPositionTransformer.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class OpenPositionFlowIntegrationTest {

   private MockMvc mockMvc;

   private ClientService clientService = spy(ClientService.class);
   private MarketDataCache marketDataCache = spy(MarketDataCache.class);
   private OpenPositionsService openPositionsService = spy(new OpenPositionsService(clientService, marketDataCache));

   @Before
   public void setup() {
      OpenPositionsController openPositionsController = new OpenPositionsController(openPositionsService);
      mockMvc = standaloneSetup(openPositionsController).build();
   }

   @Test
   public void openPositionFlow() throws Exception {
      Client client = clientService.storeNewClient(createClient());
      String clientId = client.getId();

      //Open a position
      AddOpenPositionDto openPositionDto = createOpenPositionDto();
      final Integer buySize = openPositionDto.getBuySize();
      TestHelper.ResultCaptor<Double> openingPriceCaptor = new TestHelper.ResultCaptor<>();
      doAnswer(openingPriceCaptor).when(marketDataCache).getCurrentPriceForMarket(openPositionDto.getMarketId());

      MvcResult addOPResponse = mockMvc.perform(post("/openPositions/" + clientId)
            .contentType(APPLICATION_JSON_UTF8)
            .content(convertObjectToJsonBytes(openPositionDto))
      )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andReturn();

      final Double openingPrice = openingPriceCaptor.getResult();
      String openPositionId = JsonPath.read(addOPResponse.getResponse().getContentAsString(), "$.openPositionId");
      verify(openPositionsService, times(1)).addOpenPositionForClient(clientId, transform(openPositionDto));
      verify(clientService, times(1)).updateAvailableFunds(clientId, INITIAL_FUNDS - (buySize * openingPrice));


      //Get open positions
      MvcResult mvcResult = mockMvc.perform(get("/openPositions/" + clientId)
            .contentType(APPLICATION_JSON_UTF8)
      )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andReturn();

      String contentFromGetResponse = mvcResult.getResponse().getContentAsString();

      verify(openPositionsService, times(1)).getOpenPositionsForClient(clientId);
      assertThat(contentFromGetResponse).isEqualTo("[{\"id\":\"" + openPositionId + "\",\"marketId\":\"market_1\",\"profitAndLoss\":0.0,\"openingPrice\":" + openingPrice + ",\"buySize\":10}]");

      //Delete a position
      TestHelper.ResultCaptor<Double> closingPriceCaptor = new TestHelper.ResultCaptor<>();
      doAnswer(closingPriceCaptor).when(marketDataCache).getCurrentPriceForMarket(openPositionDto.getMarketId());
      MvcResult deleteOPResponse = mockMvc.perform(post("/openPositions/" + clientId + "/" + openPositionId)
            .contentType(APPLICATION_JSON_UTF8)
      )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andReturn();

      String content = deleteOPResponse.getResponse().getContentAsString();

      final double profitAndLoss = (buySize * closingPriceCaptor.getResult()) - (buySize * openingPrice);
      final double updatedAvailableFunds = INITIAL_FUNDS + profitAndLoss;

      assertThat(Double.parseDouble(content)).isEqualTo(profitAndLoss);
      verify(openPositionsService, times(1)).closeOpenPosition(clientId, openPositionId);
      verify(clientService, times(1)).updateAvailableFunds(clientId, updatedAvailableFunds);

      //Verify client funds
      Client clientData = clientService.getClientData(clientId);
      assertThat(clientData.getRunningProfitAndLoss()).isEqualTo(0);
      assertThat(clientData.getAvailableFunds()).isEqualTo(updatedAvailableFunds);
   }

   private Client createClient() {
      return Client.builder()
            .id("client_1")
            .availableFunds(INITIAL_FUNDS)
            .runningProfitAndLoss(10000)
            .userName("username")
            .build();
   }

   private AddOpenPositionDto createOpenPositionDto() {
      return AddOpenPositionDto.builder()
            .marketId("market_1")
            .buySize(10)
            .build();
   }

}
