package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;
import org.apache.commons.codec.binary.Base64;

import java.applet.Applet;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class SSHLauncherApplet extends Applet {
    private final static Logger logger = Logger.getLogger(SSHLauncherApplet.class.getName());


    StringBuffer buffer;

    public void init() {
        buffer = new StringBuffer();
        addItem("Initializing.");
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
        addItem("Starting.");
        logger.addHandler(new ConsoleHandler());
        logger.info("Starting");

        try {
            SSHConfiguration sshConfiguration = getSSHConfiguration();

            addItem("Launching SSH Session");
            SSHLauncher.launchSSHFromConfiguration(sshConfiguration);

            // If we did not fail, let's cleanup.

            String returnURL = getParameter("returnURL");
            if (returnURL == null) {
                return;
            }
            try {
                getAppletContext().showDocument(new URL(returnURL));
            } catch (MalformedURLException e) {
                addItem("Unable to exit.");
                addItem(e.toString());
            }

        } catch (LauncherException e) {
            e.printStackTrace();
            addItem("Error!");
            addItem(e.toString());
        }
    }

    public void stop() {
        addItem("Stopping.");
    }

    public void destroy() {
        addItem("Unloading.");
    }

    void addItem(String newWord) {
        System.out.println(newWord);
        buffer.append(newWord);
        buffer.append(" ");
        repaint();
    }

    public void paint(Graphics g) {
        //Draw a Rectangle around the applet's display area.
        g.drawRect(0, 0, size().width - 1, size().height - 1);

        //Draw the current string inside the rectangle.
        g.drawString(buffer.toString(), 5, 15);
    }
}