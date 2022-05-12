package com.dacubeking.autobuilder.gui.gui.notification;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NotificationHandler {
    private final static List<Notification> notifications = Collections.synchronizedList(new ArrayList<>());

    public static void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public void processNotification(ShapeDrawer drawer, Batch batch) {
        synchronized (notifications) {
            Iterator<Notification> iterable = notifications.iterator();
            while (iterable.hasNext()) {
                Notification notification = iterable.next();
                if (notification.tick(drawer, batch)) {
                    AutoBuilder.disableContinuousRendering(notification);
                    iterable.remove();
                }
            }
        }
    }
}
