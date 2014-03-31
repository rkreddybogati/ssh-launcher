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


        String requestedLogLevel = getParameter("logLevel");
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
    }

    private SSHConfiguration getSSHConfiguration () throws InvalidConfigurationException {
        String user = getParameter("user");
        String host = getParameter("host");
        String port = getParameter("port");
        String openSSHPrivateKey = getParameter("openssh-private-key");
        String puttyPrivateKey = getParameter("putty-private-key");


        if (user == null || host == null) {
            throw new InvalidConfigurationException("User ('user') and Host ('host') must be specified.");
        }

        SSHConfiguration sshConfiguration = new SSHConfiguration(user, host);

        if (port != null) {
            try {
                Integer intPort = Integer.parseInt(port);
                sshConfiguration.setPort(intPort);
            } catch (NumberFormatException e) {
                throw new InvalidConfigurationException(String.format("Port must be a number (received: '%s')", port));
            }
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

            logger.info("Creating SSH Session");

            SSHLauncher.launchSSHFromConfiguration(sshConfiguration);

            // If we did not fail, let's cleanup.

            String returnURL = getParameter("returnURL");
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
    }

    public void stop() {
        logger.info("Exiting");
    }

    public void destroy() {
        logger.info("Unloading");
    }
}