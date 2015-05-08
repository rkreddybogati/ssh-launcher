package com.scalr.ssh.launcher.gui.platform;

import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppFrameView;
import com.scalr.ssh.launcher.gui.generic.AppHttpServerView;
import com.scalr.ssh.logging.Loggable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

abstract public class BaseAppLauncher extends Loggable {
    protected String[] args;

    public BaseAppLauncher (String[] args) {
        this.args = args;
    }

    private Boolean processArgsRemotely (String authorityKey) {
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
                Request.Get(String.format("http://127.0.0.1:8080/%s/?%s", authorityKey, split[1]))
                        .connectTimeout(1000)
                        .socketTimeout(1000)
                        .execute().returnContent().asString();
                getLogger().info(String.format("Successfully submitted to remote app instance: %s", arg));
            } catch (ClientProtocolException e) {
                getLogger().warning("Received Protocol Exception");
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                getLogger().warning("Received IO Exception");
                e.printStackTrace();
                continue;
            }

            remoteSucceeded = Boolean.TRUE;
        }

        return remoteSucceeded;
    }

    private Boolean processArgsLocally (String authorityKey) {
        /* Use local instance*/
        getLogger().info("No remote app instance found: launching new instance");

        AppController appController = instantiateAppController(authorityKey);
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

    private AppController instantiateAppController (String authorityKey) {
        AppController appController = new AppController(authorityKey);
        specializeAppController(appController);

        AppFrameView appFrame = new AppFrameView(appController);
        AppHttpServerView appHttpServer = new AppHttpServerView(appController);

        appController.registerView(appFrame);
        appController.registerView(appHttpServer);

        return appController;

    }

    abstract protected void specializeAppController(AppController appController);

    protected void doMain() {
        try {
            // TODO - Permissions
            String authorityFile = "/tmp/authority";
            String authorityKey;

            Charset UTF_8 = Charset.forName("UTF-8");

            // Load, or write, authority file
            FileInputStream in = null;

            try {
                in = new FileInputStream(authorityFile);
            } catch (FileNotFoundException e) {
                // No file!
            }

            if (in == null) {
                // Create
                authorityKey = RandomStringUtils.randomAlphanumeric(20);

                // TODO - Make dir!
                FileOutputStream out = new FileOutputStream(authorityFile);

                try {
                    FileLock lock = out.getChannel().lock();
                    try {
                        Writer writer = new OutputStreamWriter(out, UTF_8);
                        writer.write(authorityKey);
                        writer.flush();
                    } finally {
                        lock.release();
                    }
                } finally {
                    out.close();
                }

            } else {
                // Read
                try {
                    FileLock lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
                    try {
                        Reader reader = new InputStreamReader(in, UTF_8);
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(reader, writer);
                        authorityKey = writer.toString();
                    } finally {
                        lock.release();
                    }
                } finally {
                    in.close();
                }
            }

            // Run!
            // TODO - Kinda sucks to pass it like that
            if (!processArgsRemotely(authorityKey)) {
                processArgsLocally(authorityKey);
            }
        // TODO - Handle those!
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
