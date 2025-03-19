package me.cdh.state;

import me.cdh.Animate;

public enum BubbleState implements Animate {
    DIZZY(4, 30),
    ZZZ(4, 30),
    HEART(4, 50),
    NONE(-1, -1)
    ;
    private final int frame;
    private final int delay;
    BubbleState(int frame,int delay) {
        this.frame=frame;
        this.delay=delay;
    }
    @Override
    public int delay() {
        return delay;
    }
    @Override
    public int frame() {
        return frame;
    }
}
