package com.brife.user.exception;

public class AccountWithdrawnException extends RuntimeException {

    public AccountWithdrawnException() {
        super("탈퇴 후 30일 동안 로그인하거나 재가입할 수 없습니다.");
    }
}
