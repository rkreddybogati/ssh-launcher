package com.scalr;

public class MacOSSSHLauncher extends UnixLauncher {
    @Override
    protected String[] getSSHCommandFromPath(String path) {
        return new String[] {"/usr/bin/open", "--fresh", "--new", "-b", "com.apple.terminal", path};
    }
}
