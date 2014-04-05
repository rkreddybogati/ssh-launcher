package com.scalr.ssh.provider.linux;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.provider.base.BaseUnixSSHProvider;

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
