package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.manager.PuTTYManager;

public class WindowsSSHLauncher extends BaseSSHLauncher {
    public WindowsSSHLauncher(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        PuTTYManager sshManager = new PuTTYManager(sshConfiguration);

        sshManager.setUpSSHEnvironment();
        //TODO -> Quoting of the SSH command
        return sshManager.getSSHCommandLineBits();
    }
}
