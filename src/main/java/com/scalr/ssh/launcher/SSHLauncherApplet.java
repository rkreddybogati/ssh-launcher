package com.scalr.ssh.launcher;

import com.scalr.ssh.launcher.configuration.AppletLauncherConfiguration;
import com.scalr.ssh.logging.JTextAreaHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHLauncherApplet extends JApplet {
    private final static Logger logger = Logger.getLogger(SSHLauncherApplet.class.getName());
    private final static String logLevelParam = "logLevel";
    private final static int    paramLogLength = 20;

    public SSHLauncherApplet () {
    }

    public void init() {
        // Another session button
        JButton button = new JButton("Launch another session");
        button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
               launchNewSSHSession();
            }
        });

        // Log window
        JTextArea textArea  = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
        logger.info(String.format("Version: %s", SSHLauncher.class.getPackage().getImplementationVersion()));

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


        // Add items to layout
        button.setPreferredSize(new Dimension(0, 50));

        setLayout(new BorderLayout());
        getContentPane().add(jScrollPane, BorderLayout.CENTER);
        getContentPane().add(button, BorderLayout.PAGE_END);
    }


    public void start() {
        logger.info("Starting");
        launchNewSSHSession();
    }

    private void launchNewSSHSession() {
        final SSHLauncher sshLauncher = new SSHLauncher(new AppletLauncherConfiguration(this));

        new Thread() {
            public void run() {
                sshLauncher.launch();
            }
        }.start();
    }

    public void stop() {
        logger.info("Exiting");
    }

    public void destroy() {
        logger.info("Unloading");
    }

    public String[][] getParameterInfo () {
        return ArrayUtils.addAll(SSHLauncher.getParameterInfo(),
                new String[][] {{logLevelParam, "string", "Logging level (optional, defaults to INFO)"}});
    }
}