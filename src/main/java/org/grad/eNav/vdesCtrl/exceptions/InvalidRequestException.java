package org.grad.eNav.vdesCtrl.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for informing that validation of  the request has failed.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String msg){
        super(msg);
    }
}
