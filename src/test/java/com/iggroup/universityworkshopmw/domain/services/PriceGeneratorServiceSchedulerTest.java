package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.model.Market;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PriceGeneratorServiceSchedulerTest {

   @Autowired
   private MarketDataService marketDataService;

   @Test
   public void updateMarketPrices_runsWhenScheduled_updatesPrices() throws InterruptedException {
      //Given
      Map<String, Double> marketPrices = marketDataService.getAllMarkets().stream().collect(Collectors.toMap(Market::getId, Market::getCurrentPrice));
      List<Double> initialValues = new ArrayList<>(marketPrices.values());

      //When
      sleep(1000);

      //Then
      Map<String, Double> updatedMarketPrices = marketDataService.getAllMarkets().stream().collect(Collectors.toMap(Market::getId, Market::getCurrentPrice));
      assertThat(updatedMarketPrices.values().size()).isEqualTo(10);
      assertThat(updatedMarketPrices.keySet()).containsOnly("market_1", "market_2", "market_3", "market_4", "market_5", "market_6", "market_7", "market_8", "market_9", "market_10");
      assertThat(new ArrayList<>(updatedMarketPrices.values())).isNotEqualTo(initialValues);
   }
}