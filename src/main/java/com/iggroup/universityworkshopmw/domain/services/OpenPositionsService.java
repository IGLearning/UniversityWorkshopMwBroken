package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.caches.MarketDataCache;
import com.iggroup.universityworkshopmw.domain.exceptions.InsufficientFundsException;
import com.iggroup.universityworkshopmw.domain.exceptions.MissingBuySizeException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoAvailableDataException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoMarketPriceAvailableException;
import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.iggroup.universityworkshopmw.domain.helpers.Helper.createUniqueId;
import static com.iggroup.universityworkshopmw.domain.helpers.Helper.roundToTwoDecimalPlaces;

@Slf4j
@Component
public class OpenPositionsService {

   private final ClientService clientService;
   private final MarketDataCache marketDataCache;
   private ConcurrentMap<String, List<OpenPosition>> clientPositionStore;

   public OpenPositionsService(ClientService clientService, MarketDataCache marketDataCache) {
      this.clientService = clientService;
      this.marketDataCache = marketDataCache;
      clientPositionStore = new ConcurrentHashMap<>();

   }

   public List<OpenPosition> getOpenPositionsForClient(String clientId) throws Exception {
      List<OpenPosition> openPositions = getPositionDataFromMap(clientId);

      if (openPositions != null) {
         return openPositions;
      }
      throw new NoAvailableDataException("No open positions exist for client: " + clientId);
   }

   public OpenPosition addOpenPositionForClient(String clientId, OpenPosition newOpenPosition) throws InsufficientFundsException, NoMarketPriceAvailableException, MissingBuySizeException, NoAvailableDataException {
      OpenPosition newOpenPositionCopy = OpenPosition.builder().buySize(newOpenPosition.getBuySize()).marketId(newOpenPosition.getMarketId()).build();

      if (newOpenPositionCopy.getBuySize() == 0) {
         log.error("Buy size is ZERO. Cannot open a position with size ZERO.");
         throw new MissingBuySizeException("Buy size is ZERO. Cannot open a position with size ZERO.");
      }
      newOpenPositionCopy.setOpeningPrice(marketDataCache.getCurrentPriceForMarket(newOpenPositionCopy.getMarketId()));

      double positionPrice = getPositionOpeningPrice(newOpenPositionCopy);
      List<OpenPosition> openPositionsForClient = clientPositionStore.get(clientId);

      double clientAvailableFunds = checkClientAvailableFunds(clientId, positionPrice);
      OpenPosition openPositionWithId = updateStoreWithNewPosition(clientId, newOpenPositionCopy, openPositionsForClient);

      double newAvailableFunds = calculateNewAvailableFunds(clientAvailableFunds, positionPrice);
      clientService.updateAvailableFunds(clientId, roundToTwoDecimalPlaces(newAvailableFunds));

      log.info("Added new openPosition={}", newOpenPositionCopy);
      return openPositionWithId;
   }

   public Double closeOpenPosition(String clientId, String openPositionToClose) throws NoAvailableDataException, NoMarketPriceAvailableException {
      List<OpenPosition> openPositions = getPositionDataFromMap(clientId);

      OpenPosition position = openPositions.stream()
            .filter(pos -> pos.getId().equals(openPositionToClose))
            .findFirst()
            .orElseThrow(() -> new NoAvailableDataException("No position exists with id: " + openPositionToClose));

      double closingPrice = marketDataCache.getCurrentPriceForMarket(position.getMarketId());

      double openingPositionPrice = getPositionOpeningPrice(position);
      double closingProfitAndLoss = calculateNewProfitAndLoss(closingPrice, position.getOpeningPrice(), position.getBuySize());

      openPositions.remove(position);
      if (openPositions.isEmpty()) {
         clientPositionStore.remove(clientId);
      }

      calculateAndUpdateRunningProfitAndLoss(clientId, clientPositionStore.get(clientId));
      final double closingFunds = calculateClosingFunds(clientId, closingProfitAndLoss, openingPositionPrice);
      clientService.updateAvailableFunds(clientId, roundToTwoDecimalPlaces(closingFunds));

      log.info("Closed openPosition={}, closingProfitAndLoss={}", position, closingProfitAndLoss);
      return closingProfitAndLoss;
   }

   void updateMarketPrice(String marketId, Double newValue) {
      clientPositionStore.keySet()
            .forEach(clientId -> {
               List<OpenPosition> openPositions = clientPositionStore.get(clientId);
               openPositions.stream()
                     .filter(openPosition -> openPosition.getMarketId().equals(marketId))
                     .forEach(openPosition -> updateProfitAndLoss(newValue, openPosition, openPositions));

               if (openPositions.stream().anyMatch(openPosition -> openPosition.getMarketId().equals(marketId))) {
                  calculateAndUpdateRunningProfitAndLoss(clientId, openPositions);
               }
            });
   }

   private void calculateAndUpdateRunningProfitAndLoss(String clientId, List<OpenPosition> openpositionsForClient) {
      double sumOfPositionProfitAndLoss;
      if (openpositionsForClient == null) {
         sumOfPositionProfitAndLoss = 0;
      } else {
         sumOfPositionProfitAndLoss = openpositionsForClient
               .stream()
               .map(OpenPosition::getProfitAndLoss)
               .mapToDouble(d -> d)
               .sum();
      }
      try {
         clientService.updateRunningProfitAndLoss(clientId, sumOfPositionProfitAndLoss);
      } catch (NoAvailableDataException e) {
         log.error("Could not update running profit and loss and available funds for client, when updating market price, as no available data for clientId={}. Exception={}", clientId, e.getMessage());
      }
   }

   private double getPositionOpeningPrice(OpenPosition newOpenPosition) {
      return newOpenPosition.getBuySize() * newOpenPosition.getOpeningPrice();
   }

   private double calculateNewAvailableFunds(double clientAvailableFunds, double positionPrice) {
      return (clientAvailableFunds - positionPrice);
   }

   private void updateProfitAndLoss(Double newValue, OpenPosition openPosition, List<OpenPosition> openPositions) {
      Double newProfitAndLoss = calculateNewProfitAndLoss(newValue, openPosition.getOpeningPrice(), openPosition.getBuySize());
      openPositions.set(openPositions.indexOf(openPosition), createNewPosition(openPosition, newProfitAndLoss, false));
   }

   private OpenPosition createNewPosition(OpenPosition openPosition, Double profitAndLoss, boolean generateId) {
      String id = checkForDuplicateOpenPositionId(openPosition, generateId);

      return OpenPosition.builder()
            .id(id)
            .marketId(openPosition.getMarketId())
            .buySize(openPosition.getBuySize())
            .openingPrice(openPosition.getOpeningPrice())
            .profitAndLoss(profitAndLoss)
            .build();
   }

   private Double calculateNewProfitAndLoss(double newValue, double openingPrice, int buySize) {
      return (newValue - openingPrice) * buySize;
   }

   private String checkForDuplicateOpenPositionId(OpenPosition openPosition, boolean generateId) {
      String id;
      do {
         id = generateId ? createUniqueId("opid_") : openPosition.getId();
      } while (generateId && clientPositionStore.containsKey(id));
      return id;
   }

   private double checkClientAvailableFunds(String clientId, double positionPrice) throws NoAvailableDataException, InsufficientFundsException {
      Client client = clientService.getClientData(clientId);

      if ((client.getAvailableFunds()) < positionPrice) {
         throw new InsufficientFundsException("Client: " + clientId + " lacks sufficient funds to place that trade");
      }
      return client.getAvailableFunds();
   }

   private OpenPosition updateStoreWithNewPosition(String clientId, OpenPosition newOpenPosition, List<OpenPosition> openPositionsForClient) {
      OpenPosition openPosition = createNewPosition(newOpenPosition, newOpenPosition.getProfitAndLoss(), true);
      if (openPositionsForClient != null) {
         openPositionsForClient.add(openPosition);
      } else {
         clientPositionStore.put(clientId, newArrayList(openPosition));
      }
      return openPosition;
   }

   private List<OpenPosition> getPositionDataFromMap(String clientId) throws NoAvailableDataException {
      if (clientPositionStore.containsKey(clientId)) {
         return clientPositionStore.get(clientId);
      }
      throw new NoAvailableDataException("No positions available for client: " + clientId);
   }

   private double calculateClosingFunds(String clientId, double closingProfitAndLoss, double openingPositionPrice) throws NoAvailableDataException {
      Client client = clientService.getClientData(clientId);
      return client.getAvailableFunds() + openingPositionPrice + closingProfitAndLoss;
   }

}
