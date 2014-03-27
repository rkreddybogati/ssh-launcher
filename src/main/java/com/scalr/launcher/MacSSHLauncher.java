package com.scalr.launcher;

public class MacSSHLauncher extends UnixSSHLauncher {
    @Override
    protected String[] getSSHCommandFromPath(String path) {
        return new String[] {"/usr/bin/open", "--fresh", "--new", "-b", "com.apple.terminal", path};
    }
}
