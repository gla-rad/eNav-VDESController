package org.grad.eNav.vdesCtrl.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for informing that saving has failed.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SavingFailedException extends RuntimeException {
    public SavingFailedException(String msg){
        super(msg);
    }
}
