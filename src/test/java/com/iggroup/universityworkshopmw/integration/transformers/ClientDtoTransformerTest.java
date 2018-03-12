package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.integration.dto.ClientDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientDtoTransformerTest {
   @Test
   public void shouldTransformClientModelToClientDto() {
      Client client = Client.builder()
            .id("client_12345")
            .userName("userName")
            .availableFunds(400.0)
            .runningProfitAndLoss(400.0)
            .build();
      ClientDto clientDto = ClientDtoTransformer.transform(client);
      assertThat(clientDto).isEqualToComparingFieldByFieldRecursively(client);
   }

   @Test
   public void shouldHandleClientWithPotentialNullValues() {
      Client client = Client.builder()
            .id("client_1235")
            .userName(null)
            .availableFunds(400.0)
            .runningProfitAndLoss(400.0)
            .build();
      ClientDto clientDto = ClientDtoTransformer.transform(client);
      assertThat(clientDto).isEqualToComparingFieldByFieldRecursively(client);
   }
}