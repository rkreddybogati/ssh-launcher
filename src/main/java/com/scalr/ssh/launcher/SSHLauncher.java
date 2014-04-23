package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.provider.base.SSHProviderInterface;
import com.scalr.ssh.provider.manager.SSHProviderManager;
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

    private final static String hostParam               = "host";
    private final static String userParam               = "user";
    private final static String portParam               = "port";
    private final static String openSSHKeyParam         = "sshPrivateKey";
    private final static String puttyKeyParam           = "puttyPrivateKey";
    private final static String sshKeyNameParam         = "sshKeyName";
    private final static String preferredProviderParam  = "preferredProvider";
    private final static String ignoreHostKeysParam     = "ignoreHostKeys";

    private final static String paramTrue = "1";

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
        SSHProviderManager sshLauncherManager = new SSHProviderManager(platformName);

        ArrayList<SSHProviderInterface> sshLaunchers = sshLauncherManager.getOrderedSSHProviders(sshConfiguration, preferredLauncher);
        if (sshLaunchers.isEmpty()) {
            logger.severe(String.format("No SSH Launcher available for platform '%s'", platformName));
        }

        for (SSHProviderInterface sshLauncher: sshLaunchers) {
            logger.info(String.format("Creating SSH Session with provider: '%s'", sshLauncher.getClass().getCanonicalName()));
            try {
                String[] sshCommand = sshLauncher.getSSHCommand();
                logger.info(String.format("Launcher Command Line: '%s'", StringUtils.join(sshCommand, " ")));

                ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);
                pb.start();

                logger.info("Started SSH process.");
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
        String ignoreHostKeys       = launcherConfiguration.getOption(ignoreHostKeysParam);


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

        if (ignoreHostKeys != null) {
            sshConfiguration.setIgnoreHostKeys(ignoreHostKeys.equals(paramTrue));
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
                String preferredLauncher = launcherConfiguration.getOption(preferredProviderParam);
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
                {hostParam,                 "string",   "Host to SSH into"},
                {userParam,                 "boolean",  "User to SSH as (optional)"},
                {portParam,                 "int",      "Port to SSH to (optional)"},
                {openSSHKeyParam,           "string",   "Base64-encoded OpenSSH Private Key to SSH with (optional)"},
                {puttyKeyParam,             "url",      "Base64-encoded PuTTY Private Key to SSH with (optional)"},
                {sshKeyNameParam,           "string",   "Name to use for the private key (optional)"},
                {preferredProviderParam,    "url",      "Preferred SSH Launcher to use (optional)"},
                {ignoreHostKeysParam,       "int",      "Set to 1 to ignore Host Keys (optional)"},
        };
    }
}
