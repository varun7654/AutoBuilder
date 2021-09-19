package me.varun.autobuilder.events.movablepoint;

import com.badlogic.gdx.math.Vector2;
import me.varun.autobuilder.pathing.PointRenderer;
import org.jetbrains.annotations.NotNull;

public class PointClickEvent {
    private @NotNull Vector2 pos;
    private final @NotNull PointRenderer point;
    private final boolean leftClick;
    private final boolean rightClick;

    /**
     * The point should use the position value of this event to set it's position after the event is completed
     * @param pos Position of this point
     * @param point Point calling this event
     */
    public PointClickEvent(@NotNull Vector2 pos, @NotNull PointRenderer point, boolean leftClick, boolean rightClick) {
        this.pos = pos;
        this.point = point;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
    }

    /**
     *
     * @return The position of the point that is clicked
     */
    public @NotNull Vector2 getPos() {
        return pos;
    }

    /**
     *
     * @return The point that was clicked
     */
    public @NotNull PointRenderer getPoint() {
        return point;
    }

    /**
     *
     * @param position set the position of the point
     */
    public void setPosition(@NotNull Vector2 position){
        this.pos = position;
    }

    /**
     *
     * @return if the left click button on the mouse was pressed on this point
     */
    public boolean isLeftClick() {
        return leftClick;
    }

    /**
     *
     * @return if the right click button on the mouse was pressed on this point
     */
    public boolean isRightClick() {
        return rightClick;
    }

    @Override
    public @NotNull String toString() {
        return "PointClickEvent{" +
                "pos=" + pos +
                ", point=" + point +
                '}';
    }
}
