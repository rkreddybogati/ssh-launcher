package com.scalr.ssh.launcher.gui.platform.win;

import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppFrameView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;


public class WinSshLauncherApp {

    public static void main(String args[]) {
        final AppController appController = new AppController();

        // Connect the bits
        final AppFrameView appFrame = new AppFrameView(appController);

        appController.registerView(appFrame);

        appController.start();

        Logger logger = Logger.getLogger(WinSshLauncherApp.class.getName());

        if (args.length == 0) {
            logger.info("No arguments");
            return;
        }

        for (String arg : args) {
            URI uri;

            try {
                uri = new URI(arg);
            } catch (URISyntaxException e) {
                logger.warning(String.format("Argument is not an URI: %s", arg));
                continue;
            }

            UriLauncherConfiguration launcherConfiguration = new UriLauncherConfiguration(uri);
            appController.launchSshSession(launcherConfiguration);
        }


    }
}
