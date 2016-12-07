package edu.bu.cs591.ateam.pavlokdrivingapp;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by sesha on 10/25/2016.
 * Container for mapping JSON response from pavlok Oauth API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Authorized {

    String access_token;
    String expires_in;
    String created_at;
    String token_type;
    public String getAccess_token() {
        return access_token;
    }
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
    public String getExpires_in() {
        return expires_in;
    }
    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }
    public String getCreated_at() {
        return created_at;
    }
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
    public String getToken_type() {
        return token_type;
    }
    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

}