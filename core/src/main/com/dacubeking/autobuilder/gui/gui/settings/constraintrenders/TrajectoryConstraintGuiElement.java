package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.DividerGuiElement;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.GuiElement;
import com.dacubeking.autobuilder.gui.gui.elements.scrollablegui.TextGuiElement;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

public abstract class TrajectoryConstraintGuiElement implements GuiElement {

    public static final int H_1_FONT_SIZE = 22;
    public static final int H_2_FONT_SIZE = 20;
    final int constraintIndex;

    public TrajectoryConstraintGuiElement(int constraintIndex) {
        this.constraintIndex = constraintIndex;
    }

    protected void updateConstraint(TrajectoryConstraint constraint) {
        ArrayList<TrajectoryConstraint> trajectoryConstraints = AutoBuilder.getConfig().getPathingConfig().trajectoryConstraints;
        trajectoryConstraints.remove(constraintIndex);
        trajectoryConstraints.add(constraintIndex, constraint);

        AutoBuilder.getConfig().setPathingConfig(AutoBuilder.getConfig().getPathingConfig());
        UndoHandler.getInstance().somethingChanged();
        UndoHandler.getInstance().reloadPaths();
    }

    private static final DividerGuiElement dividerGuiElement = new DividerGuiElement();

    public float renderTitle(@NotNull TextGuiElement title, @NotNull ShapeDrawer shapeRenderer,
                             @NotNull PolygonSpriteBatch spriteBatch, float drawStartX, float drawStartY, float drawWidth,
                             Camera camera) {
        float startY = drawStartY;
        startY -= title.render(shapeRenderer, spriteBatch, drawStartX, startY, drawWidth, camera, false);
        return drawStartY - startY;
    }

    public float getTitleHeight(@NotNull TextGuiElement title, float drawStartX, float drawStartY, float drawWidth) {
        float startY = drawStartY;
        startY -= title.getHeight(drawStartX, startY, drawWidth, null, false);
        return drawStartY - startY;
    }
}
