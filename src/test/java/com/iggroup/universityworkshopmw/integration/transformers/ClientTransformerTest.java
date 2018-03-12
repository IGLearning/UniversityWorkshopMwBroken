package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.integration.dto.CreateClientDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTransformerTest {

   @Test
   public void shouldTransformCreateClientDtoToClientModel() {
      CreateClientDto clientDto = CreateClientDto.builder()
            .userName("userName")
            .build();
      Client client = ClientTransformer.transform(clientDto);
      assertThat(client.getId()).isNull();
      assertThat(clientDto.getUserName()).isEqualTo(client.getUserName());
      assertThat(0.0).isEqualTo(client.getAvailableFunds());
      assertThat(0.0).isEqualTo(client.getRunningProfitAndLoss());
   }

   @Test
   public void shouldHandleClientDtoWithNullValues() {
      CreateClientDto clientDto = CreateClientDto.builder()
            .userName(null)
            .build();
      Client client = ClientTransformer.transform(clientDto);
      assertThat(client.getId()).isNull();
      assertThat(client.getUserName()).isNull();
      assertThat(0.0).isEqualTo(client.getAvailableFunds());
      assertThat(0.0).isEqualTo(client.getRunningProfitAndLoss());
   }

}