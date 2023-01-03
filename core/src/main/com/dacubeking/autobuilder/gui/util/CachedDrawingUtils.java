package com.dacubeking.autobuilder.gui.util;

import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.AbstractShapeDrawer;
import space.earlygrey.shapedrawer.Drawing;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public final class CachedDrawingUtils {

    private CachedDrawingUtils() {
    }

    private static final @NotNull MethodHandle createNewDrawingHandle;
    private static final @NotNull MethodHandle batchManagerFieldHandle;
    private static final @NotNull MethodHandle drawingFieldHandle;

    static {
        try {
            Field batchManagerField = AbstractShapeDrawer.class.getDeclaredField("batchManager");
            batchManagerField.setAccessible(true);
            batchManagerFieldHandle = MethodHandles.lookup().unreflectGetter(batchManagerField);

            Class<?> batchManagerClass = batchManagerField.getType();

            Method createNewDrawingMethod = batchManagerClass.getDeclaredMethod("createDrawing");
            createNewDrawingMethod.setAccessible(true);
            createNewDrawingHandle = MethodHandles.lookup().unreflect(createNewDrawingMethod);

            Field drawingField = batchManagerClass.getDeclaredField("drawing");
            drawingField.setAccessible(true);
            drawingFieldHandle = MethodHandles.lookup().unreflectSetter(drawingField);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Drawing createNewDrawing(ShapeDrawer shapeDrawer) {
        try {
            return (Drawing) createNewDrawingHandle.invoke(getBatchManager(shapeDrawer));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDrawing(ShapeDrawer shapeDrawer, Drawing drawing) {
        try {
            drawingFieldHandle.invoke(getBatchManager(shapeDrawer), drawing);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private static final HashMap<ShapeDrawer, Object> batchManagers = new HashMap<>();

    // We cache the batch manager because it's a final field and can't be changed
    // We also return object because we can't access the class of the batch manager
    private static Object getBatchManager(ShapeDrawer shapeDrawer) throws Throwable {
        var batchManager = batchManagers.get(shapeDrawer);

        if (batchManager == null) {
            batchManager = batchManagerFieldHandle.invoke(shapeDrawer);
            batchManagers.put(shapeDrawer, batchManager);
        }
        return batchManager;
    }
}
