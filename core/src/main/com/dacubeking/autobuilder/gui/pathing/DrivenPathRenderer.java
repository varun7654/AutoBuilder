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
    @NotNull Vector2 currPointLeft = new Vector2();
    @NotNull Vector2 currPointRight = new Vector2();

    @Override
    public void render(@NotNull ShapeDrawer shapeRenderer, @NotNull OrthographicCamera cam) {
        List<List<RobotPosition>> robotPositions = networkTables.getRobotPositions();

        synchronized (robotPositions) {
            float robotWidth = AutoBuilder.getConfig().getRobotWidth();
            float robotLength = AutoBuilder.getConfig().getRobotLength();
            float pointScaleFactor = AutoBuilder.getConfig().getPointScaleFactor();

            if (robotPositions.size() > 0) {
                RobotPosition initialPos = robotPositions.get(0).get(0);
                lastPointLeft.set((float) (initialPos.x() * pointScaleFactor),
                        (float) (initialPos.x() * pointScaleFactor));
                lastPointRight.set((float) (initialPos.x() * pointScaleFactor),
                        (float) (initialPos.x() * pointScaleFactor));
            }

            for (int i = 0; i < robotPositions.size() - 1; i++) {
                RobotPosition pos1 = robotPositions.get(i).get(0);

                RobotPosition pos2 = robotPositions.get(i + 1).get(0);
                nextPointLeft.set(0, -AutoBuilder.LINE_THICKNESS / 2);
                nextPointRight.set(0, AutoBuilder.LINE_THICKNESS / 2);

                currPointLeft.set(0, -AutoBuilder.LINE_THICKNESS / 2);
                currPointRight.set(0, AutoBuilder.LINE_THICKNESS / 2);

                float angle = (float) Math.atan2(pos1.y() - pos2.y(), pos1.x() - pos2.x());

                if (i < robotPositions.size() - 2) {
                    RobotPosition pos3 = robotPositions.get(i + 2).get(0);
                }

                nextPointLeft.rotateRad(angle);
                nextPointRight.rotateRad(angle);

                currPointLeft.rotateRad(angle);
                currPointRight.rotateRad(angle);

                nextPointLeft.add((float) (pos2.x() * pointScaleFactor),
                        (float) (pos2.y() * pointScaleFactor));
                nextPointRight.add((float) (pos2.x() * pointScaleFactor),
                        (float) (pos2.y() * pointScaleFactor));

                currPointLeft.add((float) (pos1.x() * pointScaleFactor),
                        (float) (pos1.y() * pointScaleFactor));
                currPointRight.add((float) (pos1.x() * pointScaleFactor),
                        (float) (pos1.y() * pointScaleFactor));


                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.filledPolygon(new float[]{
                        lastPointRight.x, lastPointRight.y,
                        currPointRight.x, currPointRight.y,
                        nextPointRight.x, nextPointRight.y,
                        nextPointLeft.x, nextPointLeft.y,
                        currPointLeft.x, currPointLeft.y,
                        lastPointLeft.x, lastPointLeft.y,
                });

                lastPointLeft.set(nextPointLeft);
                lastPointRight.set(nextPointRight);

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

    private void renderRobotBoundingBox(@NotNull ShapeDrawer shapeRenderer, RobotPosition pos1,
                                        Color color) {
        renderRobotBoundingBox(
                new Vector2((float) (pos1.x() * AutoBuilder.getConfig().getPointScaleFactor()),
                        (float) (pos1.y() * AutoBuilder.getConfig().getPointScaleFactor())),
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
                float len2 = mousePos.dst2((float) (robotPosition.x() * AutoBuilder.getConfig().getPointScaleFactor()),
                        (float) (robotPosition.y() * AutoBuilder.getConfig().getPointScaleFactor()), 0);
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
    public double distToClosestPointNotMainPoint(Vector3 mousePos) {
        return Double.MAX_VALUE;
    }

    public void update() {
        if ((isControlPressed()) &&
                ((Gdx.input.isKeyPressed(Keys.BACKSPACE)) || Gdx.input.isKeyPressed(Keys.DEL))) {
            networkTables.getRobotPositions().clear();
        }
    }
}
