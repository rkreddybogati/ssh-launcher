package com.scalr.ssh.provider;

import com.scalr.ssh.configuration.SSHConfiguration;

public class LinuxSSHProvider extends BaseUnixSSHProvider {

    public LinuxSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        //TODO
        return new String[0];
    }


}
