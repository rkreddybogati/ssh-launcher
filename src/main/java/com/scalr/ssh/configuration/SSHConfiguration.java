package com.scalr.ssh.configuration;

public class SSHConfiguration {
    private String username;
    private String host;
    private Integer port;
    private String openSSHPrivateKey;
    private String puttySSHPrivateKey;

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

    public String getOpenSSHPrivateKey() {
        return openSSHPrivateKey;
    }

    public void setOpenSSHPrivateKey(String openSSHPrivateKey) {
        this.openSSHPrivateKey = openSSHPrivateKey;
    }

    public String getPuttySSHPrivateKey() {
        return puttySSHPrivateKey;
    }

    public void setPuttySSHPrivateKey(String puttySSHPrivateKey) {
        this.puttySSHPrivateKey = puttySSHPrivateKey;
    }
}
