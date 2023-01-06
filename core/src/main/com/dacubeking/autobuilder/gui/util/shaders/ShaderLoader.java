package com.dacubeking.autobuilder.gui.util.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ShaderLoader {

    private ShaderLoader() {
    }

    private static final @NotNull ShaderProgramLoader.ShaderProgramParameter defaultShaderParameter =
            new ShaderProgramLoader.ShaderProgramParameter();

    @Contract("_ -> new")
    public static @NotNull ShaderProgram loadShader(@NotNull String name) {
        var assetManager = AutoBuilder.getInstance().getAssetManager();
        return new ShaderProgram(Gdx.files.internal("shaders/" + name + ".vert"),
                Gdx.files.internal("shaders/" + name + ".frag"));
    }
}
