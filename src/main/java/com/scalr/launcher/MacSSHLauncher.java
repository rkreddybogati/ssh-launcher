package com.scalr.launcher;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.scalr.fs.FileSystemManager;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class MacSSHLauncher extends UnixSSHLauncher {
    private NSDictionary getBaseTerminalConfiguration () {
        final String[] pathBits = {FileSystemManager.getUserHome(), "Library",
                                   "Preferences", "com.apple.Terminal.plist"};

        return AccessController.doPrivileged(new PrivilegedAction<NSDictionary>() {
            @Override
            public NSDictionary run() {
                File configFile = new File(StringUtils.join(pathBits, File.separator));

                NSDictionary root;

                try {
                    root = (NSDictionary) PropertyListParser.parse(configFile);
                } catch (Exception e) {
                    //TODO: Fix.
                    e.printStackTrace();
                    return new NSDictionary();  // Default configuration
                }

                String defaultConfiguration = root.objectForKey("Default Window Settings").toString();
                // TODO -> This is unsafe.
                NSDictionary configurations = (NSDictionary) root.objectForKey("Window Settings");

                NSObject ret = configurations.objectForKey(defaultConfiguration);

                if (ret == null) {
                    return new NSDictionary();
                }

                return (NSDictionary) ret;

            }
        });
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        return new String[] {"/usr/bin/open", "--fresh", "--new", "-b", "com.apple.terminal", path};
    }
}
