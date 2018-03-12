package com.iggroup.universityworkshopmw.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
   private String id;
   private String userName;
   private Double availableFunds;
   private Double runningProfitAndLoss;
}
