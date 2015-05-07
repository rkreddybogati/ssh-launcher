package com.scalr.ssh.launcher.gui.platform.win;

import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppFrameView;
import com.scalr.ssh.launcher.gui.platform.BaseAppLauncher;


public class WinAppLauncher extends BaseAppLauncher {

    public WinAppLauncher(String[] args) {
        super(args);
    }

    @Override
    protected void specializeAppController(AppController appController) {
    }

    public static void main(String args[]) {
        new WinAppLauncher(args).doMain();
    }
}
