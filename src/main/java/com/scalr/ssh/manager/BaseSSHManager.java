package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;

abstract public class BaseSSHManager implements SSHManager {
    protected SSHConfiguration sshConfiguration;

    public BaseSSHManager (SSHConfiguration sshConfiguration) {
        this.sshConfiguration = sshConfiguration;
    }

}
