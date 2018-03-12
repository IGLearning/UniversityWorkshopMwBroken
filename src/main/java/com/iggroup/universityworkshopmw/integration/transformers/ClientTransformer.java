package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.integration.dto.CreateClientDto;

public class ClientTransformer {

   public static Client transform(CreateClientDto clientDto) {
      return Client.builder()
            .userName(clientDto.getUserName())
            .build();
   }

}
