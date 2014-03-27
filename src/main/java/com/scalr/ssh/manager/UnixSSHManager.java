package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class UnixSSHManager extends BaseSSHManager {
    public UnixSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String getSSHCommandLine() {
        //TODO: Get me that SSH PK path
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add("ssh");

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add("-p");
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (sshConfiguration.getPrivateKey() != null) {
            sshCommandLineBits.add("-i");
            sshCommandLineBits.add(getSSHPrivateKeyFilePath());
        }

        String[] destinationBits = {sshConfiguration.getUsername(), "@", sshConfiguration.getHost()};
        String   destination = StringUtils.join(destinationBits, "");
        sshCommandLineBits.add(destination);

        return StringUtils.join(sshCommandLineBits, " ");
    }
}
