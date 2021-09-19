package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import me.varun.autobuilder.pathing.PathRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddScriptButton extends  AbstractGuiButton{

    public AddScriptButton(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("script_icon.png"), true));
    }

    @Override
    public boolean checkClick(@NotNull Gui gui) {
        if(super.checkClick(gui)){

            List<Pose2d> pose2dList = new ArrayList<>();
            pose2dList.add(new Pose2d());
            pose2dList.add(new Pose2d(10, 10, Rotation2d.fromDegrees(0)));
            gui.guiItems.add(new TrajectoryItem(new PathRenderer(gui.getNextColor(), pose2dList, gui.executorService)));
            return true;
        }

        return false;

    }
}
