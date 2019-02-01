package com.infinitemind.loopover;

public class MoveHistory {

    private int pos;
    private int offset;
    private StartActivity.Direction direction;

    public MoveHistory(int pos, int offset, StartActivity.Direction direction) {
        this.direction = direction;
        this.offset = offset;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public StartActivity.Direction getDirection() {
        return direction;
    }

    public void setDirection(StartActivity.Direction direction) {
        this.direction = direction;
    }
}
