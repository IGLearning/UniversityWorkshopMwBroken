package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.model.Market;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
public class PriceGeneratorService {

   private final NormalDistribution normalDistribution = new NormalDistribution(0, 40);
   private MarketDataService marketDataService;

   public PriceGeneratorService(MarketDataService marketDataService) {
      this.marketDataService = marketDataService;
   }

   @Scheduled(fixedRate = 300)
   private void updateMarketPrices() {
      List<Map.Entry<String, Market>> updateSubList = marketDataService.getShuffledMapSubset();

      updateSubList.forEach(entry -> {
         Market entryValue = entry.getValue();
         String id = entryValue.getId();
         Double oldPrice = entryValue.getCurrentPrice();

         double newMarketPrice = generateNewMarketPrice(oldPrice);
         marketDataService.updateMarket(Market.builder()
               .id(id)
               .marketName(entryValue.getMarketName())
               .currentPrice(newMarketPrice)
               .build()
         );

      });

   }

   private double generateNewMarketPrice(double oldPrice) {
      double normalDist = normalDistribution.sample();
      double newPrice = (oldPrice + normalDist);
      if (newPrice <= 1) {
         newPrice = 1;
      }
      return new BigDecimal(newPrice).setScale(2, RoundingMode.HALF_UP).doubleValue(); //2 decimal places
   }
}
