package com.iggroup.universityworkshopmw.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "No buy size data given.")
public class MissingBuySizeException extends Exception {

   public MissingBuySizeException(String message) { super(message); }

}
