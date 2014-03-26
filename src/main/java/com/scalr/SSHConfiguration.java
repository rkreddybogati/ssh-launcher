package com.scalr;

public class SSHConfiguration {
    private String username;
    private String host;

    public SSHConfiguration (String username, String host) {
        this.username = username;
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }
}
