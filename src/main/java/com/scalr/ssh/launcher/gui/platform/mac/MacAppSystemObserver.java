package com.scalr.ssh.launcher.gui.platform.mac;

import com.apple.eawt.*;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppViewInterface;
import com.scalr.ssh.logging.Loggable;

import java.net.URI;

public class MacAppSystemObserver extends Loggable implements OpenURIHandler, QuitHandler, AppViewInterface {
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
    public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
        quitResponse.cancelQuit();
        appController.exit();
    }

    @Override
    public void appSettingsChanged(LauncherConfigurationInterface launcherConfiguration) {

    }

    @Override
    public void appStarts() {
        setHandlers(this);
    }

    @Override
    public void appExits() {
        setHandlers(null);
    }


    private void setHandlers (MacAppSystemObserver to) {
        Application.getApplication().setOpenURIHandler(to);
        Application.getApplication().setQuitHandler(to);
    }
}
