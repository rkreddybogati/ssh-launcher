package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.provider.SSHProvider;
import com.scalr.ssh.provider.debug.PassthroughProvider;
import com.scalr.ssh.provider.manager.SSHProviderManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
    private final static String disableKeyAuthParam     = "disableKeyAuth";
    private final static String ignoreHostKeysParam     = "ignoreHostKeys";
    public  final static String logLevelParam           = "logLevel";  // A bit hackish to make it public.

    private final static String paramTrue = "1";

    public SSHLauncher (LauncherConfigurationInterface launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    private void logGenericError(Throwable e) {
        logger.log(Level.INFO, ExceptionUtils.getMessage(e));
        logger.log(Level.FINEST, ExceptionUtils.getStackTrace(e));
    }

    private void logProviderError(SSHProvider sshProvider, Throwable e) {
        logger.log(Level.WARNING, String.format("Provider '%s' failed to launch SSH", sshProvider.getClass().getCanonicalName()));
        logGenericError(e);
    }

    private void launchFromSSHConfiguration(SSHConfiguration sshConfiguration, String preferredLauncher) throws LauncherException {
        ArrayList<SSHProvider> sshProviders = getSSHProviders(sshConfiguration, preferredLauncher);

        for (SSHProvider sshProvider: sshProviders) {
            logger.info(String.format("Creating SSH Session with provider: '%s'", sshProvider.getClass().getCanonicalName()));
            try {
                String[] sshCommand = sshProvider.getSSHCommand();
                logger.info(String.format("Launcher Command Line: '%s'", StringUtils.join(sshCommand, " ")));

                ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);
                pb.start();

                logger.info("Started SSH process.");
                return;
            } catch (LauncherException e) {
                logProviderError(sshProvider, e);
            } catch (IOException e) {
                logProviderError(sshProvider, e);
            }
        }

        throw new LauncherException("All launchers failed to launch SSH");
    }

    private ArrayList<SSHProvider> getSSHProviders(final SSHConfiguration sshConfiguration, String preferredLauncher) {
        // Are we in debug mode?
        Boolean debugMode = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return "1".equals(System.getenv("SCALR_SSH_LAUNCHER_DEBUG"));
            }
        });

        if (debugMode) {
            return new ArrayList<SSHProvider>() {{
                add(new PassthroughProvider(sshConfiguration));
            }};
        }

        // Not in debug mode - proceed
        String platformName = AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name");
            }
        });

        logger.info(String.format("Detected Platform: '%s'", platformName));
        SSHProviderManager sshProviderManager = new SSHProviderManager(platformName);

        ArrayList<SSHProvider> sshProviders = sshProviderManager.getOrderedSSHProviders(sshConfiguration, preferredLauncher);
        if (sshProviders.isEmpty()) {
            logger.severe(String.format("No SSH Launcher available for platform '%s'", platformName));
        }
        return sshProviders;
    }

    private SSHConfiguration getSSHConfiguration () throws InvalidConfigurationException {
        String host                 = launcherConfiguration.getOption(hostParam);
        String user                 = launcherConfiguration.getOption(userParam);
        String port                 = launcherConfiguration.getOption(portParam);
        String openSSHPrivateKey    = launcherConfiguration.getOption(openSSHKeyParam);
        String puttyPrivateKey      = launcherConfiguration.getOption(puttyKeyParam);
        String sshKeyName           = launcherConfiguration.getOption(sshKeyNameParam);
        String ignoreHostKeys       = launcherConfiguration.getOption(ignoreHostKeysParam);
        String disableKeyAuth       = launcherConfiguration.getOption(disableKeyAuthParam);

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

       sshConfiguration.setIgnoreHostKeys(paramTrue.equals(ignoreHostKeys));
       sshConfiguration.setDisableKeyAuth(paramTrue.equals(disableKeyAuth));

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
                String preferredProvider = launcherConfiguration.getOption(preferredProviderParam);
                launchFromSSHConfiguration(sshConfiguration, preferredProvider);
                return true;
            } catch (LauncherException e) {
                logger.log(Level.SEVERE, "Unable to create SSH Session", e);
                logGenericError(e);
            }
        } catch (InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "Unable to create SSH Configuration", e);
            logGenericError(e);
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
                {disableKeyAuthParam,       "int",      "Set to 1 to not use SSH keys (optional)"},
                {logLevelParam,             "string",   "Logging level (optional, defaults to INFO)"}
        };
    }
}
