package com.scalr.ssh.launcher.gui.generic;

import com.scalr.ssh.launcher.SSHLauncher;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.logging.JTextAreaHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class AppFrameView extends JFrame implements AppViewInterface {
    private final Logger logger = Logger.getLogger(AppFrameView.class.getName());
    private final AppController appController;
    private final JButton launchButton ;

    private static String getAppName () {
        return "Scalr SSH Launcher";
    }

    private static String getAppVersion () {
        return SSHLauncher.class.getPackage().getImplementationVersion();
    }

    public AppFrameView(final AppController appController) {
        super(String.format("%s %s", getAppName(), getAppVersion()));

        this.appController = appController;

        // Frame initialization
        setSize(800, 600);

        // Close Behavior
        addWindowListener(new AppFrameCloseListener(this));

        // Log window
        JTextArea textArea  = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Handler textAreaHandler = new JTextAreaHandler(textArea);

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(textAreaHandler);

        // Quit Button
        final AppFrameView _this = this;
        launchButton = new JButton("Launch New Session");
        launchButton.setEnabled(Boolean.FALSE);
        launchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        _this.appController.launchSshSession();
                    }
                });
            }
        });

        // Layout
        setLayout(new BorderLayout());

        Container buttonContainer = new Container();
        buttonContainer.setLayout(new GridLayout(1, 3));
        buttonContainer.setPreferredSize(new Dimension(0, 50));

        buttonContainer.add(new Label());
        buttonContainer.add(launchButton);
        buttonContainer.add(new Label());

        add(jScrollPane, BorderLayout.CENTER);
        add(buttonContainer, BorderLayout.PAGE_END);

        // Info
        logger.info(String.format("Loaded: %s %s", getAppName(), getAppVersion()));
    }

    @Override
    public void appSettingsChanged(LauncherConfigurationInterface launcherConfiguration) {
        if (launcherConfiguration == null) {
            return;
        }

        String host = launcherConfiguration.getOption(SSHLauncher.hostParam);  // TODO - Better isolation.
        if (host == null) {
            return;
        }

        launchButton.setEnabled(Boolean.TRUE);
        launchButton.setText(String.format("Launch New Session: %s", host));
    }

    @Override
    public void appStarts() {
        final AppFrameView _this = this;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _this.setVisible(true);

            }
        });
    }
}
