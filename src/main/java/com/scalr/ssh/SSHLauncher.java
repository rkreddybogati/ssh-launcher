package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.launcher.SSHLauncherInterface;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SSHLauncher {
    private final static Logger logger = Logger.getLogger(SSHLauncher.class.getName());
    private LauncherConfigurationInterface launcherConfiguration;

    public final static String hostParam                   = "host";
    public final static String userParam                   = "user";
    public final static String portParam                   = "port";
    public final static String logLevelParam               = "logLevel";
    public final static String openSSHKeyParam             = "sshPrivateKey";
    public final static String puttyKeyParam               = "puttyPrivateKey";
    public final static String sshKeyNameParam             = "sshKeyName";
    public final static String preferredLauncherParam      = "preferredLauncher";
    public final static String returnURLParam              = "returnURL";

    public SSHLauncher (LauncherConfigurationInterface launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    private void launchFromSSHConfiguration(SSHConfiguration sshConfiguration, String preferredLauncher) throws LauncherException {
        String platformName = AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name");
            }
        });

        logger.info(String.format("Detected Platform: '%s'", platformName));
        SSHLauncherManager sshLauncherManager = new SSHLauncherManager(platformName);

        ArrayList<SSHLauncherInterface> sshLaunchers = sshLauncherManager.getOrderedSSHLaunchers(sshConfiguration, preferredLauncher);
        if (sshLaunchers.isEmpty()) {
            logger.severe(String.format("No SSH Launcher available for platform '%s'", platformName));
        }

        for (SSHLauncherInterface sshLauncher: sshLaunchers) {
            logger.info(String.format("Creating SSH Session with launcher: '%s'", sshLauncher.getClass().getCanonicalName()));
            try {
                String[] sshCommand = sshLauncher.getSSHCommand();
                logger.info(String.format("Launcher Command Line: '%s'", StringUtils.join(sshCommand, " ")));

                ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);
                pb.start();

                logger.info("Assuming SSH session was launched. Exiting.");
                return;
            } catch (LauncherException e) {
                logger.log(Level.WARNING, String.format("Launcher '%s' failed to prepare SSH", sshLauncher.getClass().getCanonicalName()), e);
            } catch (IOException e) {
                logger.log(Level.WARNING, String.format("Launcher '%s' failed to launch SSH", sshLauncher.getClass().getCanonicalName()), e);
            }
        }

        throw new LauncherException("All launchers failed to launch SSH");
    }

    private SSHConfiguration getSSHConfiguration () throws InvalidConfigurationException {
        String host                 = launcherConfiguration.getOption(hostParam);
        String user                 = launcherConfiguration.getOption(userParam);
        String port                 = launcherConfiguration.getOption(portParam);
        String openSSHPrivateKey    = launcherConfiguration.getOption(openSSHKeyParam);
        String puttyPrivateKey      = launcherConfiguration.getOption(puttyKeyParam);
        String sshKeyName           = launcherConfiguration.getOption(sshKeyNameParam);


        if (host == null) {
            throw new InvalidConfigurationException("Host ('host') must be specified.");
        }

        SSHConfiguration sshConfiguration = new SSHConfiguration(host);

        if (user != null) {
            sshConfiguration.setUsername(user);
        }

        if (port != null) {
            try {
                Integer intPort = Integer.parseInt(port);
                sshConfiguration.setPort(intPort);
            } catch (NumberFormatException e) {
                throw new InvalidConfigurationException(String.format("Port must be a number (received: '%s')", port));
            }
        }

        if (sshKeyName != null) {
            sshConfiguration.setSSHKeyName(sshKeyName);
        }

        // The private keys are base64 encoded to preserve newlines
        try {
            if (openSSHPrivateKey != null) {
                byte[] decoded = Base64.decodeBase64(openSSHPrivateKey);
                sshConfiguration.setOpenSSHPrivateKey(new String(decoded, "UTF-8"));
            }

            if (puttyPrivateKey != null) {
                byte[] decoded = Base64.decodeBase64(puttyPrivateKey);
                sshConfiguration.setPuttySSHPrivateKey(new String(decoded, "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            throw new InvalidConfigurationException("UTF-8 encoded is not supported"); // TODO -> Add info for those
        }


        return sshConfiguration;
    }

    /*
    Launch an SSH session. Return whether the session was (apparently) launched or not.
     */
    public boolean launch() {
        try {
            SSHConfiguration sshConfiguration = getSSHConfiguration();
            try {
                logger.info("Creating SSH Session");
                String preferredLauncher = launcherConfiguration.getOption(preferredLauncherParam);
                launchFromSSHConfiguration(sshConfiguration, preferredLauncher);
                return true;
            } catch (LauncherException e) {
                logger.log(Level.SEVERE, "Unable to create SSH Session", e);
            }
        } catch (InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "Unable to create SSH Configuration", e);
        }
        return false;
    }
    public static String[][] getParameterInfo () {
        return new String [][] {
                {hostParam,                 "string",  "Host to SSH into"},
                {userParam,                 "boolean", "User to SSH as (optional)"},
                {portParam,                 "int",     "Port to SSH to (optional)"},
                {logLevelParam,             "string",  "Logging level (optional, defaults to INFO)"},
                {openSSHKeyParam,           "string",  "Base64-encoded OpenSSH Private Key to SSH with (optional)"},
                {puttyKeyParam,             "url",     "Base64-encoded PuTTY Private Key to SSH with (optional)"},
                {sshKeyNameParam,           "string",  "Name to use for the private key (optional)"},
                {preferredLauncherParam,    "url",     "Preferred SSH Launcher to use (optional)"},
                {returnURLParam,            "url",     "URL to return to once the applet exits (optional)"},
        };
    }
}
