package com.scalr.ssh.launcher.gui.platform.mac;


import com.apple.eawt.Application;
import com.apple.eawt.QuitStrategy;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppFrameView;

public class MacSshLauncherApp {

    public static void main(String args[]) {
        // Native setup
        Application.getApplication().disableSuddenTermination();
        Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);

        // Actual app
        final AppController appController = new AppController();

        // Connect the bits
        final AppFrameView appFrame = new AppFrameView(appController);
        final MacAppSystemObserver appObserver = new MacAppSystemObserver(appController);

        appController.registerView(appFrame);
        appController.registerView(appObserver);

        appController.start();
    }
}
