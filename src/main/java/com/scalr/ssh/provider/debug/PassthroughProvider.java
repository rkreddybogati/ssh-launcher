package com.scalr.ssh.provider.debug;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.manager.OpenSSHManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import com.scalr.ssh.provider.base.BaseSSHProvider;

import java.util.ArrayList;
import java.util.Collections;

public class PassthroughProvider extends BaseSSHProvider {
    public PassthroughProvider(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    public PassthroughProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        sshManager.setUpSSHEnvironment();

        ArrayList<String> commandBits = new ArrayList<String>();
        Collections.addAll(commandBits, sshManager.getSSHCommandLineBits());
        commandBits.add("-vvv");

        return commandBits.toArray(new String[commandBits.size()]);
    }
}
