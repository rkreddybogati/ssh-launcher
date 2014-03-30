package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;

import java.util.ArrayList;

public class UnixSSHManager extends BaseSSHManager {
    public UnixSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String[] getSSHCommandLineBits() {
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add("ssh");  // TODO -> Where is this installed?

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add("-p");
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (sshConfiguration.getPrivateKey() != null) {
            sshCommandLineBits.add("-i");
            sshCommandLineBits.add(getSSHPrivateKeyFilePath());
        }

        sshCommandLineBits.add(getDestination());

        return sshCommandLineBits.toArray(new String[sshCommandLineBits.size()]);
    }
}
