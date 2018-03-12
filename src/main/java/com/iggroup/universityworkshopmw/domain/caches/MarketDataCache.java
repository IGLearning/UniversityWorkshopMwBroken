package com.iggroup.universityworkshopmw.domain.caches;

import com.iggroup.universityworkshopmw.domain.enums.MarketName;
import com.iggroup.universityworkshopmw.domain.exceptions.NoMarketPriceAvailableException;
import com.iggroup.universityworkshopmw.domain.model.Market;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Slf4j
@Component
public class MarketDataCache {

   private Map<String, Market> marketIdToMarketModelMap = new ConcurrentHashMap<>();
   private final String ID_PREFIX = "market_";

   public MarketDataCache() {
      initialiseMarketModelMap();
   }

   private void initialiseMarketModelMap() {
      IntStream.range(0, MarketName.values().length)
            .forEach(idx -> {
               String marketId = ID_PREFIX + (idx + 1);
               MarketName marketName = MarketName.values()[idx];
               Double startingPrice = marketName.getStartingPrice();

               marketIdToMarketModelMap.put(marketId, Market.builder()
                     .id(marketId)
                     .marketName(marketName)
                     .currentPrice(startingPrice)
                     .build());
            });
   }

   public void put(String id, Market market) {
      marketIdToMarketModelMap.put(id, market);
   }

   public boolean containsKey(String marketId) {
      return marketIdToMarketModelMap.containsKey(marketId);
   }

   public Market get(String marketId) {
      return marketIdToMarketModelMap.get(marketId);
   }

   public Collection<Market> values() {
      return marketIdToMarketModelMap.values();
   }

   public Set<Map.Entry<String, Market>> entrySet() {
      return marketIdToMarketModelMap.entrySet();
   }

   public double getCurrentPriceForMarket(String marketId) throws NoMarketPriceAvailableException {
      Market market = null;
      if (marketIdToMarketModelMap.containsKey(marketId)) {
         market = marketIdToMarketModelMap.get(marketId);
      }
      return market == null ? throwException(marketId) : market.getCurrentPrice();
   }

   private double throwException(String marketId) throws NoMarketPriceAvailableException {
      log.error("Could not get current price for marketId={}", marketId);
      throw new NoMarketPriceAvailableException("Could not get current price for marketId=" + marketId);
   }

}
