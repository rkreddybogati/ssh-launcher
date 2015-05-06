package com.scalr.ssh.launcher.mac;


public class SSHLauncherMacApp {

    public static void main(String args[]) {
        final MacAppController appController = new MacAppController();

        // Connect the bits
        final MacAppFrameView appFrame = new MacAppFrameView(appController);
        final MacAppSystemObserver appObserver = new MacAppSystemObserver(appController);

        appController.registerView(appFrame);
        appController.registerView(appObserver);

        appController.start();
    }
}
