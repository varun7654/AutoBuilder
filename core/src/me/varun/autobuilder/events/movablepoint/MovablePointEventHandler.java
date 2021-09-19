package me.varun.autobuilder.events.movablepoint;

public interface MovablePointEventHandler {
    void onPointMove(PointMoveEvent event);

    public void onPointClick(PointClickEvent event);
}
