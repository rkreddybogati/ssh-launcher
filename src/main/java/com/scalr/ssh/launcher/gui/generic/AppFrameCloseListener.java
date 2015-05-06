package com.scalr.ssh.launcher.gui.generic;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AppFrameCloseListener extends WindowAdapter {
    private Frame frame;

    public AppFrameCloseListener(Frame frame) {
        this.frame = frame;
    }

    public void windowClosing(WindowEvent e){
        frame.dispose();
    }
}
