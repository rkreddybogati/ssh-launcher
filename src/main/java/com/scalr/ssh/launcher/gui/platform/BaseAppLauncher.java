package com.scalr.ssh.launcher.gui.platform;

import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppFrameView;
import com.scalr.ssh.launcher.gui.generic.AppHttpServerView;
import com.scalr.ssh.logging.Loggable;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

abstract public class BaseAppLauncher extends Loggable {
    protected String[] args;

    public BaseAppLauncher (String[] args) {
        this.args = args;
    }

    private Boolean processArgsRemotely () {
        Boolean remoteSucceeded = Boolean.FALSE;

        /* Try remote instance */
        for (String arg : args) {
            String[] split = arg.split("\\?", 2);
            if (split.length != 2) {
                // Doesn't remotely look like a URI. Discard.
                getLogger().warning(String.format("Discarding unrecognized argument: %s", arg));
                continue;
            }

            try {
                Request.Get(String.format("http://127.0.0.1:8080/?%s", split[1]))
                        .connectTimeout(1000)
                        .socketTimeout(1000)
                        .execute().returnContent().asString();
                getLogger().info(String.format("Successfully submitted to remote app instance: %s", arg));
            } catch (ClientProtocolException e) {
                continue;
            } catch (IOException e) {
                continue;
            }

            remoteSucceeded = Boolean.TRUE;
        }

        return remoteSucceeded;
    }

    private Boolean processArgsLocally () {
        /* Use local instance*/
        getLogger().info("No remote app instance found: launching new instance");

        AppController appController = instantiateAppController();
        appController.start();

        for (String arg : args) {
            URI uri;

            try {
                uri = new URI(arg);
            } catch (URISyntaxException e) {
                getLogger().warning(String.format("Argument is not an URI: %s", arg));
                continue;
            }

            UriLauncherConfiguration launcherConfiguration = new UriLauncherConfiguration(uri);
            appController.launchSshSession(launcherConfiguration);
        }

        return Boolean.TRUE;
    }

    private AppController instantiateAppController () {
        AppController appController = new AppController();
        specializeAppController(appController);

        AppFrameView appFrame = new AppFrameView(appController);
        AppHttpServerView appHttpServer = new AppHttpServerView(appController);

        appController.registerView(appFrame);
        appController.registerView(appHttpServer);

        return appController;

    }

    abstract protected void specializeAppController(AppController appController);

    protected void doMain() {
        if(!processArgsRemotely())
            processArgsLocally();

    }

}
