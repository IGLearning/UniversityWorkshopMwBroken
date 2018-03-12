package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.caches.MarketDataCache;
import com.iggroup.universityworkshopmw.domain.model.Market;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MarketDataService {

   private final OpenPositionsService openPositionsService;
   private final MarketDataCache marketDataCache;

   public MarketDataService(OpenPositionsService openPositionsService,
                            MarketDataCache marketDataCache) {
      this.openPositionsService = openPositionsService;
      this.marketDataCache = marketDataCache;
   }

   public List<Market> getAllMarkets() {
      final ArrayList<Market> markets = new ArrayList<>(marketDataCache.values());
//      log.info("Retrieving all markets={}", markets);
      return markets;
   }

   void updateMarket(Market market) {
      marketDataCache.put(market.getId(), market);
      // TODO: 12/03/2018 Uncomment line below before committing...
      // openPositionsService.updateMarketPrice(market.getId(), market.getCurrentPrice());
   }

   List<Map.Entry<String, Market>> getShuffledMapSubset() {
      List<Map.Entry<String, Market>> marketMapEntries = new ArrayList<>(marketDataCache.entrySet());
      Collections.shuffle(marketMapEntries);
      return marketMapEntries.subList(0, marketMapEntries.size() / 2);
   }

}
