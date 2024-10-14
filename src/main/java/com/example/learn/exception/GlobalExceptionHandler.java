package com.example.learn.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e, WebRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimeStamp(new Date());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=",""));

        String message = e.getMessage();

//      request body
        if(e instanceof  MethodArgumentNotValidException){
            int start = message.lastIndexOf("[");
            int end = message.lastIndexOf("]");
            message = message.substring(start+1, end-1);
            errorResponse.setError("payload invalid");
//      pathVariable internal error 500
        }else if (e instanceof ConstraintViolationException) {
            message = message.substring(message.indexOf(" ")+1);
            errorResponse.setError("pathVariable invalid");
        }
        errorResponse.setMessage(message);

        return errorResponse;
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerException(Exception e, WebRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimeStamp(new Date());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=",""));

        String message = e.getMessage();

        if(e instanceof  MethodArgumentTypeMismatchException){
            message = message.substring(message.indexOf(" ")+1);
            errorResponse.setError("internal server error");
        }
        errorResponse.setMessage(message);

        return errorResponse;
    }
}
