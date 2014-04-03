package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;

public class LinuxSSHLauncher extends BaseUnixSSHLauncher {

    public LinuxSSHLauncher(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        //TODO
        return new String[0];
    }


}
