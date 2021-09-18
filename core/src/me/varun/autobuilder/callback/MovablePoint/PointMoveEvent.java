package me.varun.autobuilder.callback.MovablePoint;

import com.badlogic.gdx.math.Vector2;
import me.varun.autobuilder.rendering.MovablePointRenderer;

public class PointMoveEvent {
    private final Vector2 prevPos;
    private Vector2 newPos;
    private final MovablePointRenderer movablePointInterface;

    /**
     * The point should use the newPos value of this event to set it's position after the event is completed
     * @param prevPos Position of the point in the previous frame
     * @param newPos Position the point will be set to after this event
     * @param movablePointRenderer Point calling this event
     */
    public PointMoveEvent(Vector2 prevPos, Vector2 newPos, MovablePointRenderer movablePointRenderer){
        this.prevPos = prevPos;
        this.newPos = newPos;
        this.movablePointInterface = movablePointRenderer;
    }

    /**
     *
     * @return The position of the point in the previous frame
     */
    public Vector2 getPreviousPos() {
        return prevPos;
    }

    /**
     *
     * @return The position the point will be set to after the completion of this event
     */
    public Vector2 getNewPos() {
        return newPos;
    }

    /**
     *
     * @return Get the point that was moved
     */
    public MovablePointRenderer getPoint() {
        return movablePointInterface;
    }

    /**
     *
     * @param position Set the position that the point will be set to
     */
    public void setPosition(Vector2 position){
        this.newPos = position;
    }

    @Override
    public String toString() {
        return "PointMoveEvent{" +
                "prevPos=" + prevPos +
                ", curPos=" + newPos +
                ", movablePointInterface=" + movablePointInterface +
                '}';
    }

    /**
     * Set the position that the point will be set to
     * @param posX X coordinate
     * @param posY Y coordinate
     */
    public void setPosition(float posX, float posY) {
        newPos.x = posX;
        newPos.y = posY;
    }
}
