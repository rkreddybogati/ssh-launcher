package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.manager.PuTTYSSHManager;

public class WindowsSSHLauncher extends BaseSSHLauncher {
    public WindowsSSHLauncher(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        PuTTYSSHManager sshManager = new PuTTYSSHManager(sshConfiguration);

        sshManager.setUpSSHEnvironment();
        //TODO -> Quoting of the SSH command
        return sshManager.getSSHCommandLineBits();
        //return new String[] {"cmd.exe", "/c", "start", "\"Scalr SSH Session\"", "/b", sshManager.getSSHCommandLineBits()};
    }
}
