package com.iggroup.universityworkshopmw.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No available data")
public class NoAvailableDataException extends Exception {

   public NoAvailableDataException(String message) {
      super(message);
   }

}
