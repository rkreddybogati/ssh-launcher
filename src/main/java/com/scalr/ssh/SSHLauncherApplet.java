package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidConfigurationException;
import org.apache.commons.codec.binary.Base64;

import java.applet.Applet;
import java.awt.*;
import java.io.UnsupportedEncodingException;
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
            // The private key is base64 encoded to preserve newlines
            byte[] decoded = Base64.decodeBase64(sshPrivateKey);
            try {
                sshConfiguration.setPrivateKey(new String(decoded, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new InvalidConfigurationException(); // TODO -> Add info for those
            }
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