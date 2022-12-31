package com.dacubeking.autobuilder.gui.util;

import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.AbstractShapeDrawer;
import space.earlygrey.shapedrawer.Drawing;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CachedDrawingUtils {

    private CachedDrawingUtils() {
    }

    private static final @NotNull Method createNewDrawingMethod;
    private static final @NotNull Field batchManagerField;
    private static final @NotNull Field drawingField;

    static {
        try {
            batchManagerField = AbstractShapeDrawer.class.getDeclaredField("batchManager");
            batchManagerField.setAccessible(true);

            Class<?> batchManagerClass = batchManagerField.getType();
            
            createNewDrawingMethod = batchManagerClass.getDeclaredMethod("createDrawing");
            createNewDrawingMethod.setAccessible(true);

            drawingField = batchManagerClass.getDeclaredField("drawing");
            drawingField.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Drawing createNewDrawing(ShapeDrawer shapeDrawer) {
        try {
            return (Drawing) createNewDrawingMethod.invoke(batchManagerField.get(shapeDrawer));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDrawing(ShapeDrawer shapeDrawer, Drawing drawing) {
        try {
            drawingField.set(batchManagerField.get(shapeDrawer), drawing);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
