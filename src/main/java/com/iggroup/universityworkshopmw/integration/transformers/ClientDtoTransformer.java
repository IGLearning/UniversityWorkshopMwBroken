package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.integration.dto.ClientDto;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class ClientDtoTransformer {

   public static ClientDto transform(Client client) {
      return ClientDto.builder()
            .id(client.getId())
            .userName(client.getUserName())
            .availableFunds(client.getAvailableFunds())
            .runningProfitAndLoss(client.getRunningProfitAndLoss())
            .build();
   }

   public static Map<String, ClientDto> transformAllClients(Map<String, Client> allClients) {
      Map<String, ClientDto> allClientsTransformed = newHashMap();

      allClients.keySet().stream()
         .forEach(clientId -> {
            Client client = allClients.get(clientId);
            ClientDto clientDto = transform(client);
            allClientsTransformed.put(clientId, clientDto);
         });

      return allClientsTransformed;
   }

}
