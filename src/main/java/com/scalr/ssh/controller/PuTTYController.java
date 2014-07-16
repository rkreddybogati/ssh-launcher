package com.scalr.ssh.controller;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.ControllerExtension;
import com.scalr.ssh.controller.extension.putty.PuttyKeyAuthControllerExtension;
import com.scalr.ssh.controller.extension.putty.PuttyPortControllerExtension;
import com.scalr.ssh.controller.extension.putty.PuttySshOptionControllerExtension;
import com.scalr.ssh.controller.extension.shared.NoopControllerExtension;
import com.scalr.ssh.filesystem.FileSystemManager;

import java.io.File;

public class PuTTYController extends BaseSSHController {
    public PuTTYController(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    public PuTTYController(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected ControllerExtension getKeyAuthControllerExtension() {
        return new PuttyKeyAuthControllerExtension(sshConfiguration, fsManager);
    }

    @Override
    protected ControllerExtension getPortControllerExtension() {
        return new PuttyPortControllerExtension(sshConfiguration, fsManager);
    }

    @Override
    protected ControllerExtension getIgnoreHostKeysControllerExtension() {
        return new NoopControllerExtension(sshConfiguration, fsManager);
    }

    @Override
    protected ControllerExtension[] getExtraControllerExtensions () {
        return new ControllerExtension[]{new PuttySshOptionControllerExtension(sshConfiguration, fsManager)};
    }

    @Override
    protected File[] getExecutableExtraSearchPaths() {
        return new File[] {
            fsManager.pathJoin("C:/", "Program Files (x86)", "PuTTY"),
            fsManager.pathJoin("C:/", "Program Files", "PuTTY")
        };
    }

    @Override
    protected String[] getExecutableSearchNames() {
        return new String[] {"putty.exe"};
    }
}
