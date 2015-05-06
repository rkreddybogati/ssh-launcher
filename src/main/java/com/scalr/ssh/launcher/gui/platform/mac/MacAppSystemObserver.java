package com.scalr.ssh.launcher.gui.platform.mac;

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenURIHandler;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppViewInterface;
import com.scalr.ssh.logging.Loggable;

import java.net.URI;

public class MacAppSystemObserver extends Loggable implements OpenURIHandler, AppViewInterface {
    private final AppController appController;

    public MacAppSystemObserver(AppController appController) {
        this.appController = appController;
    }

    @Override
    public void openURI(AppEvent.OpenURIEvent openURIEvent) {
        URI uri = openURIEvent.getURI();
        UriLauncherConfiguration launcherConfiguration = new UriLauncherConfiguration(uri);
        appController.launchSshSession(launcherConfiguration);
    }

    @Override
    public void appSettingsChanged(LauncherConfigurationInterface launcherConfiguration) {

    }

    @Override
    public void appStarts() {
        Application.getApplication().setOpenURIHandler(this);
    }
}
