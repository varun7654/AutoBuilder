package com.dacubeking.autobuilder.gui.config.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.*;


public class UploadFileButton extends AbstractGuiButton {
    private final ConfigGUI configGUI;

    public UploadFileButton(int x, int y, int width, int height, ConfigGUI configGUI) {
        super(x, y, width, height, new Texture(Gdx.files.internal("folder.png"), true));
        this.configGUI = configGUI;
    }

    @Override
    public boolean checkClick() {
        if (super.checkClick()) {
            CompletableFuture<File> future = CompletableFuture.supplyAsync(() -> {
                PointerBuffer outPath = memAllocPointer(1);

                try {
                    return getFile(
                            NativeFileDialog.NFD_OpenDialog("json",
                                    AutoBuilder.getConfig().getAutoPath().getParentFile().getAbsolutePath(),
                                    outPath),
                            outPath);
                } finally {
                    memFree(outPath);
                    AutoBuilder.requestRendering();
                }
            });
            configGUI.setOpenedFile(future);
            return true;
        } else if (checkRightClick()) {
            CompletableFuture<File> future = CompletableFuture.supplyAsync(() -> {
                PointerBuffer outPath = memAllocPointer(1);

                try {
                    return getFile(
                            NativeFileDialog.NFD_SaveDialog("json",
                                    AutoBuilder.getConfig().getAutoPath().getParentFile().getAbsolutePath(),
                                    outPath),
                            outPath);
                } finally {
                    memFree(outPath);
                    AutoBuilder.requestRendering();
                }
            });
            configGUI.setNewAutoFile(future);
        }
        return false;
    }


    private static @Nullable File getFile(int result, PointerBuffer path) {
        switch (result) {
            case NFD_OKAY -> {
                nNFD_Free(path.get(0));
                return new File(path.getStringUTF8());
            }
            case NFD_CANCEL -> System.out.println("User pressed cancel opening file.");
            default -> // NFD_ERROR
                    System.err.format("Error: %s\n", NFD_GetError());
        }
        return null;
    }
}
