package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.caches.MarketDataCache;
import com.iggroup.universityworkshopmw.domain.model.Market;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.iggroup.universityworkshopmw.domain.enums.MarketName.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MarketDataServiceTest {

   private MarketDataService marketDataService;

   @Before
   public void setup() {
      //Given
      OpenPositionsService openPositionsService = mock(OpenPositionsService.class);
      marketDataService = new MarketDataService(openPositionsService, new MarketDataCache());
   }

   @Test
   public void getAllMarkets_returnsAllMarkets_marketMapIsInitialised() {
      //When
      List<Market> allMarkets = marketDataService.getAllMarkets();

      //Then
      assertThat(allMarkets.size()).isEqualTo(10);
      assertThat(allMarkets).extracting(Market::getMarketName).containsOnly(GOLD, SILVER, PLATINUM, COPPER, NATURAL_GAS, COFFEE, WHEAT, COCOA, COTTON, SUGAR);
      assertThat(allMarkets).extracting(Market::getId).containsOnly("market_1", "market_2", "market_3", "market_4", "market_5", "market_6", "market_7", "market_8", "market_9", "market_10");
   }

   @Test
   public void updateMarket_updatesMarketInMap() {
      //Given
      Market aRandomMarketFromMap = marketDataService.getAllMarkets().get(0);
      Market updatedMarket = Market.builder()
            .id(aRandomMarketFromMap.getId())
            .marketName(SILVER)
            .currentPrice(123.1)
            .build();

      //When
      marketDataService.updateMarket(updatedMarket);

      //Then
      List<Market> allMarkets = marketDataService.getAllMarkets();
      assertThat(allMarkets).doesNotContain(aRandomMarketFromMap);
      assertThat(allMarkets).contains(updatedMarket);
   }

   @Test
   public void getShuffledMapSubset_returnsRandomlyOrderedPartialMap() {
      //Given
      List<Map.Entry<String, Market>> initialShuffledMapSubset = marketDataService.getShuffledMapSubset();

      //When
      List<Map.Entry<String, Market>> secondShuffledMapSubset = marketDataService.getShuffledMapSubset();

      //Then
      assertThat(initialShuffledMapSubset).isNotEqualTo(secondShuffledMapSubset);
   }
}