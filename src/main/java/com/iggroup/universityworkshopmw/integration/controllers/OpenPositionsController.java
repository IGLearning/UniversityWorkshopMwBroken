package com.iggroup.universityworkshopmw.integration.controllers;

import com.iggroup.universityworkshopmw.domain.exceptions.InsufficientFundsException;
import com.iggroup.universityworkshopmw.domain.exceptions.MissingBuySizeException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoAvailableDataException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoMarketPriceAvailableException;
import com.iggroup.universityworkshopmw.domain.model.OpenPosition;
import com.iggroup.universityworkshopmw.domain.services.OpenPositionsService;
import com.iggroup.universityworkshopmw.integration.dto.AddOpenPositionDto;
import com.iggroup.universityworkshopmw.integration.dto.OpenPositionDto;
import com.iggroup.universityworkshopmw.integration.transformers.OpenPositionDtoTransformer;
import com.iggroup.universityworkshopmw.integration.transformers.OpenPositionTransformer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Api(value = "/openPositions", description = "Operations relating to open positions")
@Slf4j
@RestController
@RequestMapping(value = "/openPositions")
@RequiredArgsConstructor
public class OpenPositionsController {
   private final OpenPositionsService openPositionsService;

   @ApiOperation(value = "Get all open positions for client",
         notes = "Returns all open positions for a given clientId",
         response = OpenPositionDto.class,
         responseContainer = "List")
   @ApiResponses(value = {
         @ApiResponse(code = HTTP_OK,
               message = "Successfully retrieved position data"),
         @ApiResponse(code = HTTP_BAD_REQUEST,
               message = "Couldn't recognise request"),
         @ApiResponse(code = HTTP_NOT_FOUND,
               message = "Couldn't find positions for client"),
         @ApiResponse(code = HTTP_INTERNAL_ERROR,
               message = "Couldn't get position data")
   })
   @CrossOrigin
   @GetMapping("/{clientId}")
   public ResponseEntity<?> getOpenPositions(@PathVariable("clientId") String clientId) {
      try {
         List<OpenPosition> openPositions = openPositionsService.getOpenPositionsForClient(clientId);
         List<OpenPositionDto> responseBody = OpenPositionDtoTransformer.transform(openPositions);

         return new ResponseEntity<>(responseBody, OK);
      } catch (NoAvailableDataException e) {
         log.info("No open positions for client={}, ", clientId, e);
         return new ResponseEntity<>("No open positions were available for client: " + clientId, NOT_FOUND);
      } catch (Exception e) {
         log.info("Could not retrieve open positions for client={}, ", clientId, e);
         return new ResponseEntity<>("Something went wrong while getting client positions", INTERNAL_SERVER_ERROR);
      }
   }

   @ApiOperation(value = "Create an open position",
         notes = "Create a new position for a given client",
         response = String.class)
   @ApiResponses(value = {
         @ApiResponse(code = HTTP_OK,
               message = "Successfully opened position"),
         @ApiResponse(code = HTTP_BAD_REQUEST,
               message = "Couldn't recognise request"),
         @ApiResponse(code = HTTP_NOT_FOUND,
               message = "Couldn't find data needed to open position"),
         @ApiResponse(code = HTTP_INTERNAL_ERROR,
               message = "Couldn't open position")
   })
   @CrossOrigin
   @PostMapping("/{clientId}")
   public ResponseEntity<?> addOpenPosition(@PathVariable("clientId") String clientId,
                                            @RequestBody AddOpenPositionDto openPositionDto) {
      try {
         OpenPosition openPosition = OpenPositionTransformer.transform(openPositionDto);
         OpenPosition openPositionWithId = openPositionsService.addOpenPositionForClient(clientId, openPosition);
         Map<String, String> headers = newHashMap();
         headers.put("openPositionId", openPositionWithId.getId());

         return new ResponseEntity<>(headers, OK);
      } catch (InsufficientFundsException e) {
         log.info("Client={} lacked sufficient funds to trade, ", clientId, e);
         return new ResponseEntity<>("Client: " + clientId + " lacked sufficient funds to trade", BAD_REQUEST);
      } catch (NoAvailableDataException e) {
         log.info("Could not open position for client={}, ", clientId, e);
         return new ResponseEntity<>("Could not open position for clientId: " + clientId, NOT_FOUND);
      } catch (NoMarketPriceAvailableException e) {
         log.info("No available price for marketId={}, ", openPositionDto.getMarketId(), e);
         return new ResponseEntity<>("No available price for marketId=" + openPositionDto.getMarketId(), BAD_REQUEST);
      } catch (MissingBuySizeException e) {
         log.info("No buy size given for open position, so cannot open position.", openPositionDto.getMarketId(), e);
         return new ResponseEntity<>("No buy size given for open position, so cannot open position.", BAD_REQUEST);
      } catch (Exception e) {
         log.info("Could not add an open position for client={}, ", clientId, e);
         return new ResponseEntity<>("Something went wrong when opening a position", INTERNAL_SERVER_ERROR);
      }
   }

   @ApiOperation(value = "Delete an open position",
         notes = "Deletes an open position for a given client. Responds with closing profit and loss",
         response = Double.class)
   @ApiResponses(value = {
         @ApiResponse(code = HTTP_OK,
               message = "Successfully closed position"),
         @ApiResponse(code = HTTP_BAD_REQUEST,
               message = "Couldn't recognise request"),
         @ApiResponse(code = HTTP_NOT_FOUND,
               message = "Couldn't find position for positionID provided"),
         @ApiResponse(code = HTTP_INTERNAL_ERROR,
               message = "Couldn't close position")
   })
   @CrossOrigin
   @PostMapping("/{clientId}/{openPositionId}")
   public ResponseEntity<?> closeOpenPosition(@PathVariable("clientId") String clientId,
                                              @PathVariable("openPositionId") String openPositionId) {
      try {
         Double response = openPositionsService.closeOpenPosition(clientId, openPositionId);
         return new ResponseEntity<>(response, OK);
      } catch (NoAvailableDataException e) {
         log.info("Client={} had no open positions, or no positions matching clientId={}, ", clientId, openPositionId, e);
         return new ResponseEntity<>("Client: " + clientId + " had no open positions or no positions matching id: " + openPositionId, NOT_FOUND);
      } catch (NoMarketPriceAvailableException e) {
         log.info("No available price for open position's market, ", e);
         return new ResponseEntity<>("No available price for open position's market.", BAD_REQUEST);
      } catch (Exception e) {
         log.info("Could not delete position={}, for client={}, ", openPositionId, clientId, e);
         return new ResponseEntity<>("Could not close position: " + openPositionId + " for client: " + clientId, INTERNAL_SERVER_ERROR);
      }
   }
}
