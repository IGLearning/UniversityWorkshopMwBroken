package com.iggroup.universityworkshopmw.integration.controllers;

import com.iggroup.universityworkshopmw.domain.exceptions.DuplicatedDataException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoAvailableDataException;
import com.iggroup.universityworkshopmw.domain.model.Client;
import com.iggroup.universityworkshopmw.domain.services.ClientService;
import com.iggroup.universityworkshopmw.integration.dto.ClientDto;
import com.iggroup.universityworkshopmw.integration.dto.CreateClientDto;
import com.iggroup.universityworkshopmw.integration.transformers.ClientDtoTransformer;
import com.iggroup.universityworkshopmw.integration.transformers.ClientTransformer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Api(value = "/client", description = "Operations relating to the client")
@Slf4j
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

   private final ClientService clientService;

   @ApiOperation(value = "Create a new client",
         notes = "Creates a single client",
         response = ClientDto.class)
   @ApiResponses(value = {
         @ApiResponse(code = HTTP_OK,
               message = "Successfully created a client"),
         @ApiResponse(code = HTTP_BAD_REQUEST,
               message = "Couldn't recognise request"),
         @ApiResponse(code = HTTP_INTERNAL_ERROR,
               message = "Couldn't create client")
   })
   @PostMapping("/createClient")
   @CrossOrigin
   public ResponseEntity<?> createClient(@RequestBody CreateClientDto clientDto) {
         return new ResponseEntity<>("Something went wrong when creating a new client", INTERNAL_SERVER_ERROR);
   }

   @ApiOperation(value = "Get a client",
         notes = "Gets data for a single client",
         response = ClientDto.class)
   @ApiResponses(value = {
         @ApiResponse(code = HTTP_OK,
               message = "Successfully retrieved client data"),
         @ApiResponse(code = HTTP_BAD_REQUEST,
               message = "Couldn't recognise request"),
         @ApiResponse(code = HTTP_NOT_FOUND,
               message = "Couldn't find client for clientId provided"),
         @ApiResponse(code = HTTP_INTERNAL_ERROR,
               message = "Couldn't get client data")
   })
   @CrossOrigin
   @GetMapping("/{clientId}")
   public ResponseEntity<?> getClient(@PathVariable("clientId") String clientId) {
         return new ResponseEntity<>("Something went wrong when retrieving client data", INTERNAL_SERVER_ERROR);
   }

   @ApiOperation(value = "Get a client by username",
         notes = "Gets data for a single client, by username",
         response = ClientDto.class)
   @ApiResponses(value = {
         @ApiResponse(code = HTTP_OK,
               message = "Successfully retrieved client data"),
         @ApiResponse(code = HTTP_BAD_REQUEST,
               message = "Couldn't recognise request"),
         @ApiResponse(code = HTTP_NOT_FOUND,
               message = "Couldn't find client for clientUsername provided"),
         @ApiResponse(code = HTTP_INTERNAL_ERROR,
               message = "Couldn't get client data")
   })
   @CrossOrigin
   @GetMapping("/login/{clientUsername}")
   public ResponseEntity<?> getClientByUserName(@PathVariable("clientUsername") String username) {
         return new ResponseEntity<>("Something went wrong when retrieving client data", INTERNAL_SERVER_ERROR);
   }

}
