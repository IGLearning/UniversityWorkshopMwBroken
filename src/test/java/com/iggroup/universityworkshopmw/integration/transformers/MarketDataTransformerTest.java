package com.iggroup.universityworkshopmw.integration.transformers;

import com.iggroup.universityworkshopmw.domain.model.Market;
import com.iggroup.universityworkshopmw.integration.dto.MarketDto;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.iggroup.universityworkshopmw.domain.enums.MarketName.SILVER;
import static org.assertj.core.api.Assertions.assertThat;

public class MarketDataTransformerTest {

   @Test
   public void transformMarketListToMarketDtoList_returnsListOfMarketDtos() {
      List<Market> listOfMarkets = new ArrayList<>();
      listOfMarkets.add(Market.builder()
            .id("market_1")
            .marketName(SILVER)
            .currentPrice(100.0)
            .build());

      List<MarketDto> marketDtos = MarketDataTransformer.transform(listOfMarkets);

      assertThat(marketDtos.size()).isEqualTo(1);
      assertThat(marketDtos.get(0).getId()).isEqualTo("market_1");
      assertThat(marketDtos.get(0).getMarketName()).isEqualTo(SILVER.getName());
      assertThat(marketDtos.get(0).getCurrentPrice()).isEqualTo(100.0);
   }
}