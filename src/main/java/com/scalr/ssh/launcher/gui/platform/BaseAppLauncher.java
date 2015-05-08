package com.scalr.ssh.launcher.gui.platform;

import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.launcher.gui.generic.AppController;
import com.scalr.ssh.launcher.gui.generic.AppFrameView;
import com.scalr.ssh.launcher.gui.generic.AppHttpServerView;
import com.scalr.ssh.logging.Loggable;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileLock;
import java.nio.file.Files;

abstract public class BaseAppLauncher extends Loggable {
    protected String[] args;
    private FileSystemManager fs;

    public BaseAppLauncher (String[] args) {
        this.args = args;
        fs = new FileSystemManager();
    }

    private Boolean processArgsRemotely (String authorityKey) {
        Boolean remoteSucceeded = Boolean.FALSE;

        // TODO - Port variable..
        // TODO - If there is no arg, we shouldn't launch a new instance.

        /* Try remote instance */
        URI uri;
        for (String arg : args) {
            try {
                uri = new URI(arg);
            } catch (URISyntaxException e) {
                getLogger().warning(String.format("Argument is not an URI: %s", arg));
                continue;
            }

            try {
                Request.Get(String.format("http://127.0.0.1:8080/%s/?%s", authorityKey, uri.getRawQuery()))
                        .connectTimeout(1000)
                        .socketTimeout(1000)
                        .execute().returnContent().asString();
                getLogger().info(String.format("Successfully submitted to remote app instance: %s", arg));
            } catch (ClientProtocolException e) {
                // Authority is wrong
                getLogger().warning("Received Protocol Exception");
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                // Port is wrong
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

    private class LockManager {
        // Meh...
        File lockFile;
        FileLock lock;
        FileOutputStream stream;

        public LockManager (File lockFile) {
            this.lockFile = lockFile;
            lock = null;
        }

        public synchronized void acquireLock () throws IOException {
            stream = new FileOutputStream(lockFile);
            lock = stream.getChannel().lock();
        }

        public synchronized void releaseLock () throws IOException {
            if (lock == null || stream == null) {
                throw new IllegalStateException(String.format("Lock on '%s' is not currently held", lockFile.getPath()));
            }

            lock.release();
            stream.close();
            Files.delete(lockFile.toPath());

            lock = null;
            stream = null;
        }

    }

    private String acquireAuthority () {
        String userHome = fs.getUserHome();
        String authorityFileName = ".scalr-ssh-launcher-authority";

        File authorityFile = fs.pathJoin(userHome, authorityFileName);
        File authorityLockFile = fs.pathJoin(userHome, String.format("%s.lock", authorityFileName));
        String authorityKey;

        LockManager authorityLockManager = new LockManager(authorityLockFile);

        try {
            authorityLockManager.acquireLock();

            try {

                if (authorityFile.createNewFile()) {
                    // The file did not exist
                    getLogger().fine("Creating new authority.");
                    authorityKey = RandomStringUtils.randomAlphanumeric(20);

                    fs.chmod600(authorityFile);
                    fs.writeFile(authorityFile, authorityKey);
                } else {
                    // The file already existed
                    getLogger().fine("Acquiring existing authority.");
                    authorityKey = fs.readFile(authorityFile);
                    fs.chmod600(authorityFile);
                }

                return authorityKey;
            } finally {
                authorityLockManager.releaseLock();
            }
        } catch (IOException e) {
            getLogger().warning(String.format("Acquiring authority failed: %s\n%s",
                    ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e)));
            return null;
        }
    }

    protected void doMain() {
        String authorityKey = acquireAuthority();

        if (authorityKey != null) {
            getLogger().info(String.format("Authority is: '%s'", authorityKey));
            if (processArgsRemotely(authorityKey)) {
                // Remote processing succeeded.
                return;
            }
        } else {
            getLogger().warning("Failed to acquire authority");
        }

        // Either remote processing failed (no server), or we were unable to acquire authority.
        processArgsLocally(authorityKey);
    }
}
