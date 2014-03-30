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
import java.util.logging.ConsoleHandler;
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

        getContentPane().add(jScrollPane);

        //textArea.setText("Hello!\n");
//        textArea.append("from the applet");


        Logger loggerForAddition;

        // TODO - Check which logger is the right one.
        for (String loggerName: new String[] {Logger.GLOBAL_LOGGER_NAME, ""}) {
            loggerForAddition = Logger.getLogger(loggerName);
            loggerForAddition.addHandler(new JTextAreaHandler(textArea));
            loggerForAddition.addHandler(new ConsoleHandler());
        }

        // Info
        logger.info("Initialized applet.");
    }

    private SSHConfiguration getSSHConfiguration () throws InvalidConfigurationException {
        String user = getParameter("user");
        String host = getParameter("host");
        String port = getParameter("port");
        String sshPrivateKey = getParameter("sshPrivateKey");
        String name = getParameter("name");

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

        if (sshPrivateKey != null) {
            // The private key is base64 encoded to preserve newlines
            byte[] decoded = Base64.decodeBase64(sshPrivateKey);
            try {
                sshConfiguration.setPrivateKey(new String(decoded, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new InvalidConfigurationException("UTF-8 encoded is not supported"); // TODO -> Add info for those
            }
        }

        if (name != null) {
            sshConfiguration.setName(name);
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