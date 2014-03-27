package com.scalr.ssh;

abstract public class BaseSSHManager implements SSHManager {
    SSHConfiguration sshConfiguration;

    public BaseSSHManager (SSHConfiguration sshConfiguration) {
        this.sshConfiguration = sshConfiguration;
    }

}
