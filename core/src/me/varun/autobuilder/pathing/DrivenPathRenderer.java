package me.varun.autobuilder.pathing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.net.NetworkTablesHelper;
import me.varun.autobuilder.pathing.pointclicks.CloseTrajectoryPoint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

import static me.varun.autobuilder.AutoBuilder.LINE_THICKNESS;

public class DrivenPathRenderer implements PathRenderer {

    @NotNull NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();

    private int robotPreviewIndex = -1;

    @Override
    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull OrthographicCamera cam) {
        for (int i = 0; i < networkTables.getRobotPositions().size() - 1; i++) {
            Float[] pos1 = networkTables.getRobotPositions().get(i);
            Float[] pos2 = networkTables.getRobotPositions().get(i + 1);
            shapeRenderer.line(pos1[0] * config.getPointScaleFactor(), pos1[1] * config.getPointScaleFactor(),
                    pos2[0] * config.getPointScaleFactor(), pos2[1] * config.getPointScaleFactor(), Color.WHITE,
                    LINE_THICKNESS);
        }
        robotPreviewIndex = -1;
    }

    @Override
    public void updatePoint(OrthographicCamera camera, Vector3 mousePos, Vector3 lastMousePos) {

    }

    @Override
    public @NotNull ArrayList<CloseTrajectoryPoint> getCloseTrajectoryPoints(float maxDistance2, Vector3 mousePos) {
        ArrayList<CloseTrajectoryPoint> points = new ArrayList<>();
        for (int i = 0; i < networkTables.getRobotPositions().size(); i++) {
            Float[] pos = networkTables.getRobotPositions().get(i);
            float len2 = mousePos.dst2(pos[0] * config.getPointScaleFactor(), pos[1] * config.getPointScaleFactor(), 0);
            if (len2 < maxDistance2) {
                points.add(new CloseTrajectoryPoint(len2, this, i, 0));
            }
        }
        return points;
    }

    @Override
    public void setRobotPathPreviewPoint(CloseTrajectoryPoint closePoint) {
        robotPreviewIndex = closePoint.prevPointIndex;
    }

    @Override
    public boolean isTouchingSomething(Vector3 mousePos, float maxDistance2) {
        return false;
    }
}
