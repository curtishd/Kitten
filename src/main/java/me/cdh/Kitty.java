package me.cdh;

import me.cdh.state.Action;
import me.cdh.state.BubbleState;
import me.cdh.state.Direction;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static me.cdh.UIComposition.*;

public final class Kitty extends JPanel {

    public Kitty() {
        setOpaque(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        var img = INSTANCE.getCurrFrames().get(INSTANCE.getFrameNum());
        if ((INSTANCE.getAction() == Action.LAYING || INSTANCE.getAction() == Action.RISING || INSTANCE.getAction() == Action.SLEEP)
                && INSTANCE.getLayingDir() == Direction.LEFT
                || INSTANCE.getAction() == Action.CURLED
                && INSTANCE.getLayingDir() == Direction.RIGHT
        )
            img = flipImage(img);
        Objects.requireNonNull(g);
        g.drawImage(img, 0, 0, 100, 100, null);
        if (INSTANCE.getBubbleState() != BubbleState.NONE) {
            var curImg = INSTANCE.getCurrBubbleFrames().get(INSTANCE.getBubbleFrameNum());
            int x = 30, y = 40;
            switch (INSTANCE.getAction()) {
                case SLEEP, LAYING, LEFT, RIGHT -> x = INSTANCE.getLayingDir() == Direction.LEFT ? x - 30 : x + 30;
                case UP, LICKING, SITTING -> y -= 25;
                default -> {}
            }
            g.drawImage(curImg, x, y, 30, 30, null);
        }
    }
}
