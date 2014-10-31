package com.scalr.ssh.configuration;

public class SSHConfiguration {
    private String username;
    private String host;
    private Integer port;
    private String openSSHPrivateKey;
    private String puttySSHPrivateKey;
    private String sshKeyName;
    private Boolean ignoreHostKeys = Boolean.FALSE;
    private Boolean disableKeyAuth = Boolean.FALSE;
    private Boolean enableAgentForwarding = Boolean.FALSE;


    public SSHConfiguration (String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getSSHKeyName() {
        return sshKeyName;
    }

    public void setSSHKeyName(String sshKeyName) {
        this.sshKeyName = sshKeyName;
    }

    public Boolean getIgnoreHostKeys() {
        return ignoreHostKeys;
    }

    public void setIgnoreHostKeys(Boolean ignoreHostKeys) {
        this.ignoreHostKeys = ignoreHostKeys;
    }

    public Boolean getDisableKeyAuth() {
        return disableKeyAuth;
    }

    public void setDisableKeyAuth(Boolean disableKeyAuth) {
        this.disableKeyAuth = disableKeyAuth;
    }

    public Boolean useKeyAuth() {
        return !disableKeyAuth;
    }

    public Boolean getEnableAgentForwarding() {
        return enableAgentForwarding;
    }

    public void setEnableAgentForwarding(Boolean enableAgentForwarding) {
        this.enableAgentForwarding = enableAgentForwarding;
    }
}
