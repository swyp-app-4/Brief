package com.brife.user.exception;

public class InvalidWithdrawalRequestException extends RuntimeException {

    public InvalidWithdrawalRequestException(String message) {
        super(message);
    }
}
