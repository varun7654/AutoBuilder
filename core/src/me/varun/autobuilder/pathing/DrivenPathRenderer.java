package me.varun.autobuilder.pathing;

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

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull OrthographicCamera cam) {
        for (int i = 0; i < networkTables.getRobotPositions().size() - 1; i++) {
            Float[] pos1 = networkTables.getRobotPositions().get(i);
            Float[] pos2 = networkTables.getRobotPositions().get(i + 1);
            shapeRenderer.line(pos1[0] * config.getPointScaleFactor(), pos1[1] * config.getPointScaleFactor(),
                    pos2[0] * config.getPointScaleFactor(), pos2[1] * config.getPointScaleFactor(), Color.WHITE,
                    LINE_THICKNESS);
            if (i == robotPreviewIndex) {
                renderRobotBoundingBox(new Vector2(pos1[0] * config.getPointScaleFactor(), pos1[1] * config.getPointScaleFactor()),
                        pos2[2], shapeRenderer, orange, Color.WHITE);

                ArrayList<TextComponent> textComponents = new ArrayList<>();

                textComponents.add(new TextComponent("Last Estimated State @").setBold(true).setSize(15));
                addTextComponents(pos2, textComponents, 0);

                textComponents.add(new TextComponent("\n\nLatency Compensated State @").setBold(true).setSize(15));
                addTextComponents(pos1, textComponents, 6);

                if (networkTables.getRobotPositions().size() > 2) {
                    Float[] pos3 = networkTables.getRobotPositions().get(i - 2);

                    textComponents.add(new TextComponent("\n\nLatency Compensated State @").setBold(true).setSize(15));
                    addTextComponents(pos3, textComponents, 6);
                }

                HoverManager.setHoverText(new TextBlock(Fonts.ROBOTO, 13, 300, textComponents.toArray(new TextComponent[0])));
            }
        }
        robotPreviewIndex = -1;
    }

    private void addTextComponents(Float[] data, List<TextComponent> textComponents, int offset) {
        textComponents.add(new TextComponent(df.format(data[12]) + "s").setSize(15));
        textComponents.add(new TextComponent("x: ").setBold(true));
        textComponents.add(new TextComponent(df.format(data[offset]) + "m"));
        textComponents.add(new TextComponent(" y: ").setBold(true));
        textComponents.add(new TextComponent(df.format(data[1 + offset]) + "m"));
        textComponents.add(new TextComponent(" theta: ").setBold(true));
        textComponents.add(new TextComponent(df.format(data[2 + offset]) + "°\n"));
        textComponents.add(new TextComponent("Vx: ").setBold(true));
        textComponents.add(new TextComponent(df.format(data[3 + offset]) + "m/s\n"));
        textComponents.add(new TextComponent("Vy: ").setBold(true));
        textComponents.add(new TextComponent(df.format(data[4 + offset]) + "m/s\n"));
        textComponents.add(new TextComponent("Vtheta: ").setBold(true));
        textComponents.add(new TextComponent(df.format(Math.toDegrees(data[5 + offset])) + "°/s"));
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
