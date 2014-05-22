package com.scalr.ssh.provider.linux;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.filesystem.FileSystemManager;

public class LinuxGnomeTerminalSSHProvider extends LinuxBaseSSHProvider {
    public LinuxGnomeTerminalSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    public LinuxGnomeTerminalSSHProvider(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected String getTerminalEmulatorName() {
        return "gnome-terminal";
    }

    @Override
    protected String getTerminalEmulatorCommandFlag() {
        return "--execute";
    }
}
