package com.scalr;

import com.scalr.exception.InvalidConfigurationException;
import com.scalr.ssh.SSHConfiguration;

import java.applet.Applet;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class SSHLauncherApplet extends Applet {

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
            throw new InvalidConfigurationException();
        }

        SSHConfiguration sshConfiguration = new SSHConfiguration(user, host);

        if (port != null) {
            try {
                Integer intPort = Integer.parseInt(port);
                sshConfiguration.setPort(intPort);
            } catch (NumberFormatException e) {
                throw new InvalidConfigurationException();
            }
        }

        if (sshPrivateKey != null) {
            sshConfiguration.setPrivateKey(sshPrivateKey);
        }

        if (name != null) {
            sshConfiguration.setName(name);
        }

        return sshConfiguration;
    }

    public void start() {
        addItem("Starting.");

        try {
            SSHConfiguration sshConfiguration = getSSHConfiguration();
            SSHLauncher.launchSSHFromConfiguration(sshConfiguration);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            addItem("Invalid configuration!");
        } catch (Exception e) {
            e.printStackTrace();
            addItem("Error!");
            //TODO: better handling.
        }

        addItem("SSH session launched.");

        String returnURL = getParameter("returnURL");
        if (returnURL == null) {
            return;
        }
        try {
            getAppletContext().showDocument(new URL(returnURL));
        } catch (MalformedURLException e) {
            return;
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