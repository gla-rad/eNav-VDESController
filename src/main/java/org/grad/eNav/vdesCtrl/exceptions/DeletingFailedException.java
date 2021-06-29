package org.grad.eNav.vdesCtrl.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for informing that deleting has failed.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class DeletingFailedException extends RuntimeException {
    public DeletingFailedException(String msg){
        super(msg);
    }
}
