package com.iggroup.universityworkshopmw.integration.controllers;

import com.iggroup.universityworkshopmw.domain.model.Market;
import com.iggroup.universityworkshopmw.domain.services.MarketDataService;
import com.iggroup.universityworkshopmw.integration.dto.MarketDto;
import com.iggroup.universityworkshopmw.integration.transformers.MarketDataTransformer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@Api(value = "/marketData", description = "Operations relating to market data")
@Slf4j
@RestController
@RequestMapping("/marketData")
@RequiredArgsConstructor
public class MarketDataController {

   private final MarketDataService marketDataService;

   @ApiOperation(value = "Get all market data",
         notes = "Returns a list of markets")
   @ApiResponses(value = {
         @ApiResponse(code = HTTP_OK,
               message = "Successfully retrieved market data"),
         @ApiResponse(code = HTTP_BAD_REQUEST,
               message = "Couldn't recognise request"),
         @ApiResponse(code = HTTP_INTERNAL_ERROR,
               message = "Couldn't get market data")
   })
   @GetMapping("/allMarkets")
   @CrossOrigin
   public ResponseEntity<?> getAllMarketData() {
      try {
         List<Market> listOfMarkets = marketDataService.getAllMarkets();
         List<MarketDto> responseBody = MarketDataTransformer.transform(listOfMarkets);
         return new ResponseEntity<>(responseBody, OK);

      } catch (Exception e) {
         log.info("Exception when retrieving all market data, exceptionMessage=", e);
         return new ResponseEntity<>("Something went wrong when retrieving all market data", INTERNAL_SERVER_ERROR);
      }
   }

}
