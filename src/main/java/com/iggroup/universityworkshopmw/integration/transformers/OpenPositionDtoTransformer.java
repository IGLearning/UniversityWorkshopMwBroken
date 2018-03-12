package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import com.iggroup.universityworkshopmw.integration.dto.OpenPositionDto;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class OpenPositionDtoTransformer {

   public static List<OpenPositionDto> transform(List<OpenPosition> openPositions) {
      return openPositions.stream()
            .map(OpenPositionDtoTransformer::transformDto)
            .collect(toList());
   }

   private static OpenPositionDto transformDto(OpenPosition openPosition) {
      return OpenPositionDto.builder()
            .id(openPosition.getId())
            .marketId(openPosition.getMarketId())
            .profitAndLoss(openPosition.getProfitAndLoss())
            .openingPrice(openPosition.getOpeningPrice())
            .buySize(openPosition.getBuySize())
            .build();
   }
}
