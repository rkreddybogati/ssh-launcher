package com.scalr.ssh.launcher.mac;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowCloseListener extends WindowAdapter {
    private Frame frame;

    public WindowCloseListener(Frame frame) {
        this.frame = frame;
    }

    public void windowClosing(WindowEvent e){
        frame.dispose();
    }
}
