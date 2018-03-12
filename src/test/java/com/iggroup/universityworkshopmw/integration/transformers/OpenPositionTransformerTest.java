package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import com.iggroup.universityworkshopmw.integration.dto.AddOpenPositionDto;
import org.junit.Test;

import static com.iggroup.universityworkshopmw.integration.transformers.OpenPositionTransformer.transform;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenPositionTransformerTest {

   @Test
   public void shouldTransformDto() {
      AddOpenPositionDto openPositionDto = createOpenPositionDto();
      OpenPosition openPosition = transform(openPositionDto);

      assertThat(openPosition.getId()).isNull();
      assertThat(openPosition.getMarketId()).isEqualTo("market_id");
      assertThat(openPosition.getProfitAndLoss()).isEqualTo(0.0);
      assertThat(openPosition.getOpeningPrice()).isEqualTo(0.0);
      assertThat(openPosition.getBuySize()).isEqualTo(10);
   }

   private AddOpenPositionDto createOpenPositionDto() {
      return AddOpenPositionDto.builder()
            .marketId("market_id")
            .buySize(10)
            .build();
   }
}
