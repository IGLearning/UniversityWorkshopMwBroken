package com.iggroup.universityworkshopmw.integration.controllers;

import com.iggroup.universityworkshopmw.domain.model.Market;
import com.iggroup.universityworkshopmw.domain.services.MarketDataService;
import com.iggroup.universityworkshopmw.integration.dto.MarketDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static com.iggroup.universityworkshopmw.domain.enums.MarketName.GOLD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MarketDataControllerTest {

   private MarketDataService marketDataService;
   private MarketDataController marketDataController;

   @Before
   public void setup() {
      marketDataService = mock(MarketDataService.class);
      marketDataController = new MarketDataController(marketDataService);
   }

   @Test
   public void getAllMarketData_returnsOkCodeAndListOfMarketInfo() throws Exception {
      //Given
      List<Market> listOfMarkets = new ArrayList<>();
      listOfMarkets.add(Market.builder()
            .id("market_1")
            .marketName(GOLD)
            .currentPrice(400.0)
            .build());
      when(marketDataService.getAllMarkets()).thenReturn(listOfMarkets);

      //When
      final ResponseEntity<?> responseEntity = marketDataController.getAllMarketData();

      //Then
      final List<MarketDto> marketDtos = (List<MarketDto>) responseEntity.getBody();
      assertThat(marketDtos.get(0).getId()).isEqualTo("market_1");
      assertThat(marketDtos.get(0).getMarketName()).isEqualTo(GOLD.getName());
      assertThat(marketDtos.get(0).getCurrentPrice()).isEqualTo(400.0);
   }

   @Test
   public void getAllMarketData_handlesAnyException() throws Exception {
      //Given
      when(marketDataService.getAllMarkets()).thenThrow(new RuntimeException("Server exception!"));

      //When
      final ResponseEntity<?> responseEntity = marketDataController.getAllMarketData();

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(responseEntity.getBody()).isEqualTo("Something went wrong when retrieving all market data");
   }

}