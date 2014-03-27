package com.scalr.ssh;

public class SSHConfiguration {
    private String username;
    private String host;
    private Integer port;
    private String privateKey;
    private String name = "Unknown";

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
