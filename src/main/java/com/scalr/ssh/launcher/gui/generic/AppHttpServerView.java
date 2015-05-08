package com.scalr.ssh.launcher.gui.generic;

import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.launcher.configuration.NameValuePairLauncherConfiguration;
import com.scalr.ssh.logging.Loggable;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

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

            // For some unexplainable reason, request.getParameter('host') *sometimes* returns null. The same goes for
            // request.getParameterMap(). To work around this issue, we parse the querystring ourselves. This is
            // downright idiotic, but it does the job for now, until we replace Jetty with something else or get to the
            // bottom of the problem (this issue is a bit difficult to debug considering that you don't know it has
            // failed until parsing is complete, at which point it's to late to debug parsing).
            List<NameValuePair> parameters = URLEncodedUtils.parse(request.getQueryString(), Charset.forName("UTF-8"));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("ACCEPTED");   // Make this a constant

            NameValuePairLauncherConfiguration launcherConfiguration = new NameValuePairLauncherConfiguration(parameters);
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