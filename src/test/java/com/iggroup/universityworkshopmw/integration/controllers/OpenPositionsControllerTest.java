package com.iggroup.universityworkshopmw.integration.controllers;

import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import com.iggroup.universityworkshopmw.domain.services.OpenPositionsService;
import com.iggroup.universityworkshopmw.integration.dto.AddOpenPositionDto;
import com.iggroup.universityworkshopmw.integration.dto.OpenPositionDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.iggroup.universityworkshopmw.integration.transformers.OpenPositionTransformer.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class OpenPositionsControllerTest {

   private OpenPositionsController openPositionsController;
   private OpenPositionsService openPositionsService;

   @Before
   public void setUp() {
      openPositionsService = mock(OpenPositionsService.class);
      openPositionsController = new OpenPositionsController(openPositionsService);
   }

   @Test
   public void successfullyGetsOpenPositionsForClient() throws Exception {
      List<OpenPosition> openPositions = createOpenPositions();
      final String clientId1 = "client_12345";
      when(openPositionsService.getOpenPositionsForClient(clientId1)).thenReturn(openPositions);

      final ResponseEntity<?> responseEntity = openPositionsController.getOpenPositions(clientId1);
      List<OpenPositionDto> openPositionDtos = (List<OpenPositionDto>) responseEntity.getBody();


      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(openPositionDtos.get(0).getId()).isEqualTo("open_position_id");
      assertThat(openPositionDtos.get(0).getMarketId()).isEqualTo("market_id");
      assertThat(openPositionDtos.get(0).getProfitAndLoss()).isEqualTo(1000.0);
      assertThat(openPositionDtos.get(0).getBuySize()).isEqualTo(100);

      ArgumentCaptor<String> clientIdCaptor = forClass(String.class);
      verify(openPositionsService, times(1)).getOpenPositionsForClient(clientIdCaptor.capture());
      verifyNoMoreInteractions(openPositionsService);

      String clientId = clientIdCaptor.getValue();
      assertThat(clientId).isEqualTo(clientId1);
   }

   @Test
   public void returnsInternalServerErrorForFailedRequest() throws Exception {
      final String clientId = "client_12345";
      when(openPositionsService.getOpenPositionsForClient(clientId)).thenThrow(new RuntimeException("Internal server error"));

      final ResponseEntity<?> responseEntity = openPositionsController.getOpenPositions(clientId);
      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(responseEntity.getBody()).isEqualTo("Something went wrong while getting client positions");
   }

   @Test
   public void createsAnOpenPosition() throws Exception {
      AddOpenPositionDto addOpenPositionDto = createAddOpenPositionDto();
      OpenPosition openPosition = transform(addOpenPositionDto);
      final String clientId = "client_12345";
      when(openPositionsService.addOpenPositionForClient(eq(clientId), any(OpenPosition.class)))
            .thenReturn(openPosition);

      final ResponseEntity<?> responseEntity = openPositionsController.addOpenPosition(clientId, addOpenPositionDto);
      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
   }

   @Test
   public void removesAnOpenPosition() throws Exception {
      final String clientId = "client_12345";
      final String openPositionId = "open_position_id";
      when(openPositionsService.closeOpenPosition(clientId, openPositionId))
            .thenReturn(1000.00);

      final ResponseEntity<?> responseEntity = openPositionsController.closeOpenPosition(clientId, openPositionId);
      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(responseEntity.getBody()).isEqualTo(1000.0);
   }

   private AddOpenPositionDto createAddOpenPositionDto() {
      return AddOpenPositionDto.builder()
            .marketId("market_1")
            .buySize(50)
            .build();
   }

   private List<OpenPosition> createOpenPositions() {
      return newArrayList(createOpenPosition());
   }

   private OpenPosition createOpenPosition() {
      return OpenPosition.builder()
            .id("open_position_id")
            .marketId("market_id")
            .profitAndLoss(1000.00)
            .openingPrice(150.00)
            .buySize(100)
            .build();
   }
}
