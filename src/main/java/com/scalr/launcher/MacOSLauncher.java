package com.scalr.launcher;

public class MacOSLauncher extends UnixLauncher {
    @Override
    protected String[] getSSHCommandFromPath(String path) {
        return new String[] {"/usr/bin/open", "--fresh", "--new", "-b", "com.apple.terminal", path};
    }
}
