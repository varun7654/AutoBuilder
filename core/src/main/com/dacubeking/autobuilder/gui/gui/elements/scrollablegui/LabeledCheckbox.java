package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.dacubeking.autobuilder.gui.gui.elements.CheckBox;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.function.Consumer;

/**
 * A {@link GuiElement} that contains a label and a checkbox.
 */
public class LabeledCheckbox extends TextGuiElement {
    public final CheckBox checkBox;

    private boolean checked;
    private final Consumer<Boolean> checkBoxChangedCallback;


    /**
     * @param label                   The label of the checkbox.
     * @param checkBoxChangedCallback The callback that is called when the checkbox is changed.
     * @param checked                 Whether the checkbox should start checked.
     */
    public LabeledCheckbox(TextComponent label, Consumer<Boolean> checkBoxChangedCallback, boolean checked) {
        super(label);
        this.checked = checked;
        checkBox = new CheckBox(0, 0, 30, 30);
        this.checkBoxChangedCallback = checkBoxChangedCallback;
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {

        checkBox.setPosition(drawStartX + drawWidth - checkBox.getWidth() - 10, drawStartY - checkBox.getHeight() - 2);
        checkBox.checkHover();
        if (checkBox.checkClick()) {
            checked = !checked;
            UndoHandler.getInstance().somethingChanged();
            checkBoxChangedCallback.accept(checked);
        }
        RoundedShapeRenderer.roundedRect(shapeRenderer,
                drawStartX + drawWidth - checkBox.getWidth() - 12, drawStartY - checkBox.getHeight() - 4,
                checkBox.getWidth() + 4, checkBox.getHeight() + 4, 6, Color.BLACK);
        checkBox.render(shapeRenderer, spriteBatch, checked);
        float labelHeight = super.render(shapeRenderer, spriteBatch,
                drawStartX, drawStartY - 9,
                drawWidth - checkBox.getWidth() - 10, camera, isLeftMouseJustUnpressed);
        return Math.max(labelHeight + 5, checkBox.getHeight() + 6);
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        return Math.max(super.getHeight(drawStartX, drawStartY, drawWidth, isLeftMouseJustUnpressed) + 5,
                checkBox.getHeight() + 6);
    }

    /**
     * @param checked Whether the checkbox should be checked.
     */
    public void setCheckBox(boolean checked) {
        this.checked = checked;
    }

    @Override
    public void dispose() {
        super.dispose();
        checkBox.dispose();
    }
}
