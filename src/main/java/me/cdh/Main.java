package me.cdh;

import me.cdh.state.Action;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static me.cdh.UIComposition.INSTANCE;
import static me.cdh.UIComposition.isDayTime;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIComposition.initSystemTray();
                INSTANCE.changeAction(Action.CURLED);
                new Timer(10, e -> {
                    INSTANCE.updateAction();
                    INSTANCE.actionPerform();
                    INSTANCE.updateAnimation();
                    INSTANCE.manageBubbleState();
                    INSTANCE.getWindow().repaint();
                }).start();
                if (!isDayTime())
                    new Timer(30000, e -> INSTANCE.tryWander()).start();
                else
                    new Timer(6000, e -> INSTANCE.tryWander()).start();
            } catch (IOException | AWTException e) {
                throw new RuntimeException(e);
            }
        });
    }
}