package com.scalr.ssh.controller;

import com.scalr.ssh.exception.LauncherException;

public interface SSHController {
    public void setupEnvironment() throws LauncherException;
    public String[] getSSHCommandLineBits() throws LauncherException;
}
