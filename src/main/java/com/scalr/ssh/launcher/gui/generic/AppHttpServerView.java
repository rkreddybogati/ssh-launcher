package com.scalr.ssh.launcher.gui.generic;

import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.launcher.configuration.ServletRequestLauncherConfiguration;
import com.scalr.ssh.logging.Loggable;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AppHttpServerView extends Loggable implements AppViewInterface {
    private AppController appController;
    private Server server;

    private class SshHandler extends AbstractHandler {
        public void handle(String target,
                Request baseRequest,
                HttpServletRequest request,
                HttpServletResponse response ) throws IOException,
                ServletException
        {
            getLogger().info(String.format("Got request: %s", target));
            getLogger().info(String.format("WTF (%s) (%s) (%s)", baseRequest.getQueryString(), baseRequest.getParameter("host"), baseRequest.getQueryString()));
            // TODO - Fix

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Thanks!");

            ServletRequestLauncherConfiguration launcherConfiguration = new ServletRequestLauncherConfiguration(baseRequest);
            appController.launchSshSession(launcherConfiguration);

            baseRequest.setHandled(true);
        }

    }

    private class WebThread extends Thread  {
        private Server server;

        private WebThread (Server server) {
            this.server = server;
            this.setDaemon(Boolean.TRUE);
        }

        public void run () {
            try {
                server.start();
                server.join();
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
        }
    }

    public AppHttpServerView (AppController appController) {
        this.appController = appController;
    }

    @Override
    public void appSettingsChanged(LauncherConfigurationInterface launcherConfiguration) {

    }

    @Override
    public void appStarts() {
        server = new Server(8080);
        server.setHandler(new SshHandler());
        new WebThread(server).start();
    }

    @Override
    public void appExits() {
        new Thread() {
            public void run() {
                try {
                    server.stop();
                } catch (Exception e) {
                    getLogger().warning("Web server failed to exit!");
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
