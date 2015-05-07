package com.scalr.ssh.launcher.gui.platform.mac;


import com.apple.eawt.Application;
import com.apple.eawt.QuitStrategy;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.platform.BaseAppLauncher;

public class MacAppLauncher extends BaseAppLauncher {

    public MacAppLauncher(String[] args) {
        super(args);
    }

    protected void specializeAppController(AppController appController) {
        Application.getApplication().disableSuddenTermination();
        Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);

        MacAppSystemObserver appObserver = new MacAppSystemObserver(appController);
        appController.registerView(appObserver);
    }

    public static void main(String args[]) {
        new MacAppLauncher(args).doMain();
    }
}
