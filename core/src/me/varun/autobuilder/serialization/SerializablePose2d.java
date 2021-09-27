package me.varun.autobuilder.serialization;

import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import me.varun.autobuilder.wpi.math.geometry.Translation2d;

import java.io.Serializable;

public class SerializablePose2d implements Serializable {
    public SerializablePose2d(Pose2d poseMeters) {
        x = poseMeters.getX();
        y = poseMeters.getY();
        rcos = poseMeters.getRotation().getCos();
        rsin = poseMeters.getRotation().getSin();
    }

    double x;
    double y;
    double rcos;
    double rsin;

    public Pose2d getPose2d(){
        return new Pose2d(new Translation2d(x, y), new Rotation2d(rcos, rsin));
    }
}
