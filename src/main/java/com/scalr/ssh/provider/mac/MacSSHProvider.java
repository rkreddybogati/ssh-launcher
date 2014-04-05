package com.scalr.ssh.provider.mac;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.provider.base.BaseUnixSSHProvider;

public class MacSSHProvider extends BaseUnixSSHProvider {
    public MacSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        return new String[] {"/usr/bin/open", "--fresh", "--wait-apps", "--new", "-b", "com.apple.terminal", path};
    }
}
