package com.scalr.ssh.controller;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.ControllerExtension;
import com.scalr.ssh.controller.extension.openssh.OpensshIgnoreHostKeysControllerExtension;
import com.scalr.ssh.controller.extension.openssh.OpensshKeyAuthControllerExtension;
import com.scalr.ssh.controller.extension.openssh.OpensshPortControllerExtension;
import com.scalr.ssh.filesystem.FileSystemManager;

import java.io.File;

public class OpenSSHController extends BaseSSHController {
    public OpenSSHController(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    public OpenSSHController(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected ControllerExtension getKeyAuthControllerExtension() {
        return new OpensshKeyAuthControllerExtension(sshConfiguration, fsManager);
    }

    @Override
    protected ControllerExtension getPortControllerExtension() {
        return new OpensshPortControllerExtension(sshConfiguration, fsManager);
    }

    @Override
    protected ControllerExtension getIgnoreHostKeysControllerExtension() {
        return new OpensshIgnoreHostKeysControllerExtension(sshConfiguration, fsManager);
    }

    @Override
    protected File[] getExecutableExtraSearchPaths() {
        return new File[] {
            fsManager.pathJoin("/usr", "bin"), fsManager.pathJoin("/usr", "local", "bin"), new File("/bin"),
            fsManager.pathJoin("C:/", "Program Files (x86)", "OpenSSH", "bin"),
            fsManager.pathJoin("C:/", "Program Files", "OpenSSH", "bin"),
            fsManager.pathJoin("C:/", "cygwin64", "bin"),
            fsManager.pathJoin("C:/", "cygwin", "bin")
        };
    }

    @Override
    protected String[] getExecutableSearchNames() {
        return new String[] {"ssh", "ssh.exe"};
    }
}
