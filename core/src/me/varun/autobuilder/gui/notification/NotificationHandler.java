package me.varun.autobuilder.gui.notification;

import com.badlogic.gdx.graphics.g2d.Batch;
import me.varun.autobuilder.AutoBuilder;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

public class NotificationHandler {
    private final static ArrayList<Notification> notifications = new ArrayList<>();

    public static void addNotification(Notification notification){
        notifications.add(notification);
    }

    ArrayList<Notification> notificationsToDelete = new ArrayList<>();
     public void processNotification(ShapeDrawer drawer, Batch batch) {
         notificationsToDelete.clear();
         for (Notification notification : notifications) {
             if (notification.tick(drawer, batch)) {
                 notificationsToDelete.add(notification);
             }
         }

         for (Notification notification : notificationsToDelete) {
             AutoBuilder.disableContinuousRendering(notification);
             notifications.remove(notification);
         }
    }

}
