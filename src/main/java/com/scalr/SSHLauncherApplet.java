package com.scalr;

import com.scalr.exception.EnvironmentSetupException;
import com.scalr.exception.InvalidEnvironmentException;

import java.applet.Applet;
import java.awt.*;
import java.io.IOException;

public class SSHLauncherApplet extends Applet {

    StringBuffer buffer;

    public void init() {
        buffer = new StringBuffer();
        addItem("initializing... ");
    }

    public void start() {
        addItem("starting... ");
        addItem("user");
        addItem(getParameter("user"));
        addItem("host");
        addItem(getParameter("host"));

        String user = getParameter("user");
        String host = getParameter("host");

        SSHConfiguration sshConfiguration = new SSHConfiguration(user, host);

        try {
            SSHLauncher.launchSSHFromConfiguration(sshConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EnvironmentSetupException e) {
            e.printStackTrace();
        } catch (InvalidEnvironmentException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        addItem("stopping... ");
    }

    public void destroy() {
        addItem("preparing for unloading...");
    }

    void addItem(String newWord) {
        System.out.println(newWord);
        buffer.append(newWord);
        buffer.append("\n");
        repaint();
    }

    public void paint(Graphics g) {
        //Draw a Rectangle around the applet's display area.
        g.drawRect(0, 0, size().width - 1, size().height - 1);

        //Draw the current string inside the rectangle.
        g.drawString(buffer.toString(), 5, 15);
    }
}