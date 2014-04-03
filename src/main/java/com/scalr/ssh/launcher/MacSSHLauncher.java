package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;

public class MacSSHLauncher extends BaseUnixSSHLauncher {
    public MacSSHLauncher(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        return new String[] {"/usr/bin/open", "--fresh", "--wait-apps", "--new", "-b", "com.apple.terminal", path};
    }
}
