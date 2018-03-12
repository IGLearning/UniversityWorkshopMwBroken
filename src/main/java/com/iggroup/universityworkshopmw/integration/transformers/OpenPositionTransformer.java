package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import com.iggroup.universityworkshopmw.integration.dto.AddOpenPositionDto;

public class OpenPositionTransformer {

   public static OpenPosition transform(AddOpenPositionDto openPositionDto) {
      return OpenPosition.builder()
            .marketId(openPositionDto.getMarketId())
            .buySize(openPositionDto.getBuySize())
            .build();
   }
}
