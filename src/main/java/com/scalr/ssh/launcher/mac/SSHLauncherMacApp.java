package com.scalr.ssh.launcher.mac;

import com.apple.eawt.Application;

import javax.swing.*;


public class SSHLauncherMacApp {

    public static void main(String args[]) {
        final MacAppController appController = new MacAppController();

        final MacAppFrame appFrame = new MacAppFrame(appController);
        final MacAppOpenURIObserver appObserver = new MacAppOpenURIObserver(appController);

        // Launch app frame
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
