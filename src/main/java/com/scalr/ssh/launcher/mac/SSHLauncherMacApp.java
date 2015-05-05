package com.scalr.ssh.launcher.mac;

import com.apple.eawt.Application;

import javax.swing.*;


public class SSHLauncherMacApp {

    public static void main(String args[]) {
        final AppFrame appFrame = new AppFrame();
        final OpenURIObserver appObserver = new OpenURIObserver();


        // Launch app
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                appFrame.setVisible(true);
            }
        });

        // Set Mac OS Settings
        Application.getApplication().setOpenURIHandler(appObserver);
        Application.getApplication().enableSuddenTermination();
    }
}
