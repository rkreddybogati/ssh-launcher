package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.logging.JTextAreaHandler;
import org.apache.commons.codec.binary.Base64;

import javax.swing.*;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHLauncherApplet extends JApplet {
    private final static Logger logger = Logger.getLogger(SSHLauncherApplet.class.getName());
    private final static int    paramLogLength = 20;
    private final static String hostParam                   = "host";
    private final static String userParam                   = "user";
    private final static String portParam                   = "port";
    private final static String logLevelParam               = "logLevel";
    private final static String openSSHKeyParam             = "sshPrivateKey";
    private final static String puttyKeyParam               = "puttyPrivateKey";
    private final static String sshKeyNameParam             = "sshKeyName";
    private final static String preferredLauncherParam      = "preferredLauncher";
    private final static String returnURLParam              = "returnURL";

    public SSHLauncherApplet () {
    }

    public void init() {
        GridLayout layout = new GridLayout(1, 1);
        setLayout(layout);

        JTextArea textArea  = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);

        JScrollPane jScrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        System.out.println("Added pane");

        getContentPane().add(jScrollPane);


        Handler textAreaHandler = new JTextAreaHandler(textArea);

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(textAreaHandler);


        String requestedLogLevel = getParameter(logLevelParam);
        Level logLevel;

        if (requestedLogLevel != null) {
            try {
                logLevel = Level.parse(requestedLogLevel);
            } catch (IllegalArgumentException e) {
                logLevel = Level.INFO;
                logger.warning(String.format("logLevel '%s' could not be parsed - defaulting to INFO", requestedLogLevel));
            }

            logger.info(String.format("Setting LogLevel to '%s'. Requested '%s'", logLevel.getName(),
                    requestedLogLevel));

            // Do not set this on the main logger. Too much output crashes the Java applet console (?!).
            Logger launcherLogger = Logger.getLogger("com.scalr.ssh");
            textAreaHandler.setLevel(logLevel);
            launcherLogger.setLevel(logLevel);
        }

        // Info
        logger.info("Initialized applet");

        // Log paramters
        String paramName;
        String paramValue;

        for (String[] paramDefinition : getParameterInfo()) {
            paramName = paramDefinition[0];
            paramValue = getParameter(paramName);
            if (paramValue == null) {
                paramValue = "";
            }
            if (paramValue.length() > paramLogLength) {
                paramValue = String.format("%s...", paramValue.substring(0, paramLogLength));
            }
            logger.config(String.format("Applet parameter '%s': '%s'", paramName, paramValue));
        }
    }

    private SSHConfiguration getSSHConfiguration () throws InvalidConfigurationException {
        String host = getParameter(hostParam);
        String user = getParameter(userParam);
        String port = getParameter(portParam);
        String openSSHPrivateKey = getParameter(openSSHKeyParam);
        String puttyPrivateKey = getParameter(puttyKeyParam);
        String sshKeyName = getParameter(sshKeyNameParam);


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

    public void start() {
        logger.info("Starting");


        try {
            SSHConfiguration sshConfiguration = getSSHConfiguration();

            try {

                logger.info("Creating SSH Session");
                String preferredLauncher = getParameter(preferredLauncherParam);
                SSHLauncher.launchSSHFromConfiguration(sshConfiguration, preferredLauncher);

                // If we did not fail, let's cleanup.

                String returnURL = getParameter(returnURLParam);
                if (returnURL == null) {
                    return;
                }

                try {
                    getAppletContext().showDocument(new URL(returnURL));
                } catch (MalformedURLException e) {
                    logger.warning(String.format("Unable to exit: %s", e.toString()));
                }

            } catch (LauncherException e) {
                logger.log(Level.SEVERE, "Unable to create SSH Session", e);
            }

        } catch (InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "Unable to create SSH Configuration", e);
        }
    }

    public void stop() {
        logger.info("Exiting");
    }

    public void destroy() {
        logger.info("Unloading");
    }

    @Override
    public String[][] getParameterInfo() {
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