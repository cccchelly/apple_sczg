package com.sczg.apple.bean;

public class TokenBean {

    @Override
    public String toString() {
        return "token = " + token;
    }

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
