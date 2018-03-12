package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.exceptions.DuplicatedDataException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoAvailableDataException;
import com.iggroup.universityworkshopmw.domain.model.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.iggroup.universityworkshopmw.domain.helpers.Helper.roundToTwoDecimalPlaces;

@Slf4j
@Component
public class ClientService {

   private Map<String, Client> clientIdToClientModelMap = new ConcurrentHashMap<>();
   private final String ID_PREFIX = "client_";
   public static final double INITIAL_FUNDS = 25000;

   public Client storeNewClient(Client client) throws DuplicatedDataException {
      return Client.builder().build();
   }

   public Client getClientData(String clientId) throws NoAvailableDataException {
      return Client.builder().build();
   }

   public Client getClientDataByUsername(String username) throws NoAvailableDataException {
      return Client.builder().build();
   }

   public void updateAvailableFunds(String clientId, double updatedAvailableFunds) throws NoAvailableDataException {
   }

   public Map<String, Client> getAllClients() {
      return clientIdToClientModelMap;
   }

   void updateRunningProfitAndLoss(String clientId, double sumOfPositionProfitAndLoss) throws NoAvailableDataException {
      Client client = getClientDataFromMap(clientId);
      double oldProfitAndLoss = client.getRunningProfitAndLoss();

      client.setRunningProfitAndLoss(roundToTwoDecimalPlaces(sumOfPositionProfitAndLoss));
      clientIdToClientModelMap.put(clientId, client);

      double newAvailableFunds = (client.getAvailableFunds() - oldProfitAndLoss) + sumOfPositionProfitAndLoss;
      updateAvailableFunds(clientId, roundToTwoDecimalPlaces(newAvailableFunds));
   }

   private Client getClientDataFromMap(String clientId) throws NoAvailableDataException {
      if (clientIdToClientModelMap.containsKey(clientId)) {
         return clientIdToClientModelMap.get(clientId);
      } else {
         throw new NoAvailableDataException("No available client data in clientIdToClientModelMap for clientId=" + clientId);
      }
   }

}
