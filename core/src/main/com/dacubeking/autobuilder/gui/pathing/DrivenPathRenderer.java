package com.dacubeking.autobuilder.gui.pathing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.net.NetworkTablesHelper;
import com.dacubeking.autobuilder.gui.pathing.pointclicks.CloseTrajectoryPoint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.isControlPressed;

public class DrivenPathRenderer implements PathRenderer {

    @NotNull NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();

    private int robotPreviewIndex = -1;

    DecimalFormat df = new DecimalFormat("#.##");

    @NotNull Vector2 lastPointLeft = new Vector2();
    @NotNull Vector2 lastPointRight = new Vector2();
    @NotNull Vector2 nextPointLeft = new Vector2();
    @NotNull Vector2 nextPointRight = new Vector2();

    @Override
    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull OrthographicCamera cam) {
        List<List<RobotPosition>> robotPositions = networkTables.getRobotPositions();
        synchronized (robotPositions) {
            for (int i = 0; i < robotPositions.size() - 1; i++) {
                RobotPosition pos1 = robotPositions.get(i).get(0);
                //getPerfectConnectingPoints(pos1, lastPointLeft, lastPointRight);

                RobotPosition pos2 = robotPositions.get(i + 1).get(0);
//            getPerfectConnectingPoints(pos2, nextPointLeft, nextPointRight);
//
//            shapeRenderer.setColor(Color.WHITE);
//            shapeRenderer.polygon(new float[]{
//                    lastPointLeft.x, lastPointLeft.y,
//                    lastPointRight.x, lastPointRight.y,
//                    nextPointRight.x, nextPointRight.y,
//                    nextPointLeft.x, nextPointLeft.y
//            });

                shapeRenderer.line((float) (pos1.x() * config.getPointScaleFactor()),
                        (float) (pos1.y() * config.getPointScaleFactor()),
                        (float) (pos2.x() * config.getPointScaleFactor()), (float) (pos2.y() * config.getPointScaleFactor()),
                        Color.WHITE,
                        AutoBuilder.LINE_THICKNESS);

                if (i == robotPreviewIndex) {
                    ArrayList<TextComponent> textComponents = new ArrayList<>();

                    for (int j = 0; j < robotPositions.get(i).size(); j++) {
                        RobotPosition robotPosition = robotPositions.get(i).get(j);
                        renderRobotBoundingBox(shapeRenderer, robotPosition, getColor(robotPosition.name()));

                        textComponents.add(new TextComponent(robotPosition.name() + " @").setBold(true).setSize(15));
                        addTextComponents(robotPosition, textComponents);
                    }

                    HoverManager.setHoverText(new TextBlock(Fonts.ROBOTO, 13, 300, textComponents.toArray(new TextComponent[0])),
                            0, Gdx.graphics.getHeight() - 2);
                }
            }
        }

        //render the robot preview at the latest position
        if (robotPositions.size() - 1 > 0) {
            List<RobotPosition> positions = robotPositions.get(
                    robotPositions.size() - 1);
            for (RobotPosition robotPosition : positions) {
                renderRobotBoundingBox(shapeRenderer, robotPosition, getColor(robotPosition.name()));
            }
        }
        robotPreviewIndex = -1;
    }

    private final HashMap<String, Color> colors = new HashMap<>();
    private int colorIndex = 0;

    private Color getColor(String name) {
        if (!colors.containsKey(name)) {
            // Lazy generate colors as needed
            Random random = new Random(name.hashCode());
            Color color = new Color().fromHsv(random.nextFloat(360), 1, 1);
            color.a = 1;
            colors.put(name, color);
            colorIndex += 1;
        }
        return colors.get(name);
    }


    private void getPerfectConnectingPoints(RobotPosition pos, Vector2 nextPointLeft, Vector2 nextPointRight) {
        nextPointLeft.set(0, -AutoBuilder.LINE_THICKNESS / 2);
        nextPointRight.set(0, AutoBuilder.LINE_THICKNESS / 2);

        nextPointLeft.rotateRad((float) pos.theta());
        nextPointRight.rotateRad((float) pos.theta());

        nextPointLeft.add((float) (pos.x() * config.getPointScaleFactor()), (float) (pos.y() * config.getPointScaleFactor()));
        nextPointRight.add((float) (pos.x() * config.getPointScaleFactor()), (float) (pos.y() * config.getPointScaleFactor()));
    }

    private void renderRobotBoundingBox(@NotNull ShapeDrawer shapeRenderer, RobotPosition pos1,
                                        Color color) {
        renderRobotBoundingBox(
                new Vector2((float) (pos1.x() * config.getPointScaleFactor()),
                        (float) (pos1.y() * config.getPointScaleFactor())),
                (float) pos1.theta(), shapeRenderer, color, Color.WHITE);
    }

    private void addTextComponents(RobotPosition robotPosition, List<TextComponent> textComponents) {
        textComponents.add(new TextComponent(df.format(robotPosition.time()) + "s\n").setSize(15));
        textComponents.add(new TextComponent("x: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.x()) + "m"));
        textComponents.add(new TextComponent(" y: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.y()) + "m"));
        textComponents.add(new TextComponent(" theta: ").setBold(true));
        textComponents.add(new TextComponent(df.format(Math.toDegrees(robotPosition.theta())) + "°\n"));
        textComponents.add(new TextComponent("Vx: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.vx()) + "m/s\n"));
        textComponents.add(new TextComponent("Vy: ").setBold(true));
        textComponents.add(new TextComponent(df.format(robotPosition.vy()) + "m/s\n"));
        textComponents.add(new TextComponent("Vtheta: ").setBold(true));
        textComponents.add(new TextComponent(df.format(Math.toDegrees(robotPosition.vtheta())) + "°/s\n"));
    }

    @Override
    public void updatePoint(OrthographicCamera camera, Vector3 mousePos,
                            Vector3 mouseDiff) {

    }

    @Override
    public @NotNull ArrayList<CloseTrajectoryPoint> getCloseTrajectoryPoints(float maxDistance2, Vector3 mousePos) {
        ArrayList<CloseTrajectoryPoint> points = new ArrayList<>();
        List<List<RobotPosition>> robotPositions = networkTables.getRobotPositions();
        synchronized (robotPositions) {
            for (int i = 0; i < robotPositions.size(); i++) {
                RobotPosition robotPosition = robotPositions.get(i).get(0);
                float len2 = mousePos.dst2((float) (robotPosition.x() * config.getPointScaleFactor()),
                        (float) (robotPosition.y() * config.getPointScaleFactor()), 0);
                if (len2 < maxDistance2) {
                    points.add(new CloseTrajectoryPoint(len2, this, i, 0));
                }
            }
        }
        return points;
    }

    @Override
    public void setRobotPathPreviewPoint(CloseTrajectoryPoint closePoint) {
        robotPreviewIndex = closePoint.prevPointIndex();
    }

    @Override
    public boolean isTouchingSomething(Vector3 mousePos, float maxDistance2) {
        return false;
    }

    public void update() {
        if ((isControlPressed()) &&
                ((Gdx.input.isKeyPressed(Keys.BACKSPACE)) || Gdx.input.isKeyPressed(Keys.DEL))) {
            networkTables.getRobotPositions().clear();
        }
    }
}
