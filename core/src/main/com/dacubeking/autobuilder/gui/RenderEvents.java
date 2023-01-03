package com.dacubeking.autobuilder.gui;

import java.util.concurrent.ConcurrentHashMap;

public final class RenderEvents {
    private RenderEvents() {
    }

    private static final ConcurrentHashMap<Object, Runnable> renderCacheDeletionEventListeners = new ConcurrentHashMap<>();

    public static void addRenderCacheDeletionListener(Object key, Runnable runnable) {
        renderCacheDeletionEventListeners.put(key, runnable);
    }

    public static void removeRenderCacheDeletionListener(Object key) {
        renderCacheDeletionEventListeners.remove(key);
    }

    public static void fireRenderCacheDeletionEvent() {
        renderCacheDeletionEventListeners.forEach((key, value) -> value.run());
    }
}
