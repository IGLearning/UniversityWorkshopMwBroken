package com.iggroup.universityworkshopmw.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenPositionDto {
   private String id;
   private String marketId;
   private Double profitAndLoss;
   private Double openingPrice;
   private Integer buySize;
}
