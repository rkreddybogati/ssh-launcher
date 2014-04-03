package com.scalr.ssh.provider;

import com.scalr.ssh.configuration.SSHConfiguration;

public class MacSSHProvider extends BaseUnixSSHProvider {
    public MacSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        return new String[] {"/usr/bin/open", "--fresh", "--wait-apps", "--new", "-b", "com.apple.terminal", path};
    }
}
