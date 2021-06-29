package org.grad.eNav.vdesCtrl.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for informing that the needed resource is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String msg){
        super(msg);
    }
}
