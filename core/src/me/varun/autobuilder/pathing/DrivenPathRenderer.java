package me.varun.autobuilder.pathing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.gui.hover.HoverManager;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.net.NetworkTablesHelper;
import me.varun.autobuilder.pathing.pointclicks.CloseTrajectoryPoint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static me.varun.autobuilder.AutoBuilder.LINE_THICKNESS;

public class DrivenPathRenderer implements PathRenderer {

    @NotNull NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();

    private int robotPreviewIndex = -1;

    private final Color orange = Color.valueOf("ff9800ff");
    private final Color aqua = Color.valueOf("4dc5c6ff");
    private final Color red = Color.valueOf("fe0911ff");

    private final Color[] colors = {orange, aqua, red};

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull OrthographicCamera cam) {
        for (int i = 0; i < networkTables.getRobotPositions().size() - 1; i++) {
            RobotPosition pos1 = networkTables.getRobotPositions().get(i).get(0);
            RobotPosition pos2 = networkTables.getRobotPositions().get(i + 1).get(0);
            shapeRenderer.line((float) (pos1.x * config.getPointScaleFactor()), (float) (pos1.y * config.getPointScaleFactor()),
                    (float) (pos2.x * config.getPointScaleFactor()), (float) (pos2.y * config.getPointScaleFactor()), Color.WHITE,
                    LINE_THICKNESS);
            if (i == robotPreviewIndex) {
                ArrayList<TextComponent> textComponents = new ArrayList<>();

                for (int j = 0; j < networkTables.getRobotPositions().get(i).size(); j++) {
                    RobotPosition robotPosition = networkTables.getRobotPositions().get(i).get(j);
                    renderRobotBoundingBox(shapeRenderer, robotPosition, colors[j]);

                    textComponents.add(new TextComponent(robotPosition.name + " @").setBold(true).setSize(15));
                    addTextComponents(robotPosition, textComponents);
                }


//                textComponents.add(new TextComponent("\n\nLatency Compensated State @").setBold(true).setSize(15));
//                addTextComponents(pos1, textComponents);

                HoverManager.setHoverText(new TextBlock(Fonts.ROBOTO, 13, 300, textComponents.toArray(new TextComponent[0])),
                        0, Gdx.graphics.getHeight() - 2);
            }
        }
        robotPreviewIndex = -1;
    }

    private void renderRobotBoundingBox(@NotNull ShapeDrawer shapeRenderer, RobotPosition pos1,
                                        Color color) {
        renderRobotBoundingBox(
                new Vector2((float) (pos1.x * config.getPointScaleFactor()),
                        (float) (pos1.y * config.getPointScaleFactor())),
                (float) pos1.theta, shapeRenderer, color, Color.WHITE);
    }

    private void addTextComponents(RobotPosition robotPosition, List<TextComponent> textComponents) {
        textComponents.add(new TextComponent(df.format(robotPosition.time) + "s\n").setSize(15));
        textComponents.add(new TextComponent("x: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.x) + "m"));
        textComponents.add(new TextComponent(" y: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.y) + "m"));
        textComponents.add(new TextComponent(" theta: ").setBold(true));
        textComponents.add(new TextComponent(df.format(Math.toDegrees(robotPosition.theta)) + "°\n"));
        textComponents.add(new TextComponent("Vx: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.vx) + "m/s\n"));
        textComponents.add(new TextComponent("Vy: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.vy) + "m/s\n"));
        textComponents.add(new TextComponent("Vtheta: ").setBold(true));
        textComponents.add(new TextComponent(df.format(Math.toDegrees(robotPosition.vtheta)) + "°/s"));
    }

    @Override
    public void updatePoint(OrthographicCamera camera, Vector3 mousePos, Vector3 lastMousePos) {

    }

    @Override
    public @NotNull ArrayList<CloseTrajectoryPoint> getCloseTrajectoryPoints(float maxDistance2, Vector3 mousePos) {
        ArrayList<CloseTrajectoryPoint> points = new ArrayList<>();
        for (int i = 0; i < networkTables.getRobotPositions().size(); i++) {
            RobotPosition robotPosition = networkTables.getRobotPositions().get(i).get(0);
            float len2 = mousePos.dst2((float) (robotPosition.x * config.getPointScaleFactor()),
                    (float) (robotPosition.y * config.getPointScaleFactor()), 0);
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
