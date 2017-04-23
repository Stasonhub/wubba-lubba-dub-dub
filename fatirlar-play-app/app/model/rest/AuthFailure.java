package model.rest;

public class AuthFailure {

    private String message;

    public String getMessage() {
        return message;
    }

    public AuthFailure setMessage(String message) {
        this.message = message;
        return this;
    }
}
