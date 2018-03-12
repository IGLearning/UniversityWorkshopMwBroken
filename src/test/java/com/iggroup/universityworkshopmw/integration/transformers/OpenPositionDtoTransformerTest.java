package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import com.iggroup.universityworkshopmw.integration.dto.OpenPositionDto;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.iggroup.universityworkshopmw.integration.transformers.OpenPositionDtoTransformer.transform;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenPositionDtoTransformerTest {

   @Test
   public void returnsTransformedList() {
      List<OpenPosition> openPositions = createOpenPositions();
      List<OpenPositionDto> openPositionDtos = transform(openPositions);

      assertThat(openPositionDtos.get(0).getId()).isEqualTo("open_position_id");
      assertThat(openPositionDtos.get(0).getMarketId()).isEqualTo("market_id");
      assertThat(openPositionDtos.get(0).getProfitAndLoss()).isEqualTo(1234.00);
      assertThat(openPositionDtos.get(0).getOpeningPrice()).isEqualTo(100.00);
      assertThat(openPositionDtos.get(0).getBuySize()).isEqualTo(10);
   }

   private List<OpenPosition> createOpenPositions() {
      return newArrayList(createOpenPosition());
   }

   private OpenPosition createOpenPosition() {
      return OpenPosition.builder()
            .id("open_position_id")
            .marketId("market_id")
            .profitAndLoss(1234.00)
            .openingPrice(100.00)
            .buySize(10)
            .build();
   }
}
