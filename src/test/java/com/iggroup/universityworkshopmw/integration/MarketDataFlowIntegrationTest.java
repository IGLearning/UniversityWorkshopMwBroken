package com.iggroup.universityworkshopmw.integration;

import com.iggroup.universityworkshopmw.TestHelper;
import com.iggroup.universityworkshopmw.domain.caches.MarketDataCache;
import com.iggroup.universityworkshopmw.domain.services.ClientService;
import com.iggroup.universityworkshopmw.domain.services.MarketDataService;
import com.iggroup.universityworkshopmw.domain.services.OpenPositionsService;
import com.iggroup.universityworkshopmw.integration.controllers.MarketDataController;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.iggroup.universityworkshopmw.domain.enums.MarketName.SUGAR;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MarketDataFlowIntegrationTest {

   //Given
   private ClientService clientService = spy(new ClientService());
   private MarketDataCache marketDataCache = spy(new MarketDataCache());
   private OpenPositionsService openPositionsService = new OpenPositionsService(clientService, marketDataCache);
   private MarketDataService marketDataService = new MarketDataService(openPositionsService, marketDataCache);
   private MarketDataController marketDataController = new MarketDataController(marketDataService);
   private MockMvc mockMvc = MockMvcBuilders.standaloneSetup(marketDataController).build();

   @Test
   public void marketDataFlow() throws Exception {
      //When
      mockMvc.perform(get("/marketData/allMarkets"))
            //Then
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestHelper.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(10)))
            .andExpect(jsonPath("$.[0].id", is("market_10")))
            .andExpect(jsonPath("$.[0].marketName", is(SUGAR.getName())))
            .andExpect(jsonPath("$.[0].currentPrice", is(148.0)));
   }
}
