package com.iggroup.universityworkshopmw.integration.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketDto {
   private String id;
   private String marketName;
   private Double currentPrice;
}
