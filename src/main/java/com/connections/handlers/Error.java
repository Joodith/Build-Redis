package com.connections.handlers;

public class Error {
    private String errorMessage;
    public Error(){

    }
    public Error(String err){
        this.errorMessage=err;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
