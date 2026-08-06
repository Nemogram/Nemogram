package org.nemogram.messenger.helpers;

import android.graphics.Bitmap;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.utils.BitmapsCache;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.RLottieDrawable;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import app.nekogram.gifski.Gifski;

public class StickerHelper {
    private static final int MAX_SIDE = 1024;
    private static final int DEFAULT_FPS = 30;
    // rendering keeps a full size bitmap plus a native encoder alive, running several at once runs out of memory
    private static final Executor rendererExecutor = Executors.newSingleThreadExecutor();

    public static void convertStickerFormat(String path, boolean animated, Consumer<String> callback) {
        if (path == null) {
            return;
        }
        var resultPath = path + ".gif";
        var cacheOptions = new BitmapsCache.CacheOptions();
        final BitmapsCache.Cacheable drawable;
        final int width, height;
        try {
            var created = animated ?
                    new RLottieDrawable(new File(path), null, 512, 512, cacheOptions, false, null, 0, false) :
                    new AnimatedFileDrawable(new File(path), true, 0, 0, null, null, null, 0, 0, false, 0, 0, cacheOptions);
            drawable = created;
            width = clampSide(created.getIntrinsicWidth());
            height = clampSide(created.getIntrinsicHeight());
        } catch (Throwable e) {
            FileLog.e(e);
            return;
        }
        rendererExecutor.execute(() -> {
            boolean success;
            try {
                success = width > 0 && height > 0 && renderToGif(resultPath, drawable, width, height);
            } finally {
                if (animated) {
                    ((RLottieDrawable) drawable).recycle(false);
                } else {
                    ((AnimatedFileDrawable) drawable).recycle();
                }
            }
            if (success) {
                callback.accept(resultPath);
            } else {
                //noinspection ResultOfMethodCallIgnored
                new File(resultPath).delete();
            }
        });
    }

    private static int clampSide(int side) {
        if (side <= 0) {
            return 0;
        }
        return Math.min(side, MAX_SIDE);
    }

    private static boolean renderToGif(String path, BitmapsCache.Cacheable source, int width, int height) {
        Bitmap bitmap = null;
        try {
            var fps = source.getFps();
            if (fps <= 0) {
                // a non positive fps makes every frame timestamp NaN or infinite and aborts the native encoder
                fps = DEFAULT_FPS;
            }
            FileLog.d("start gif rendering for path = " + path + ", width = " + width + ", height = " + height + ", fps = " + fps);
            source.prepareForGenerateCache();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            if (source.getNextFrame(bitmap) != 1) {
                // the decoder gave us nothing, don't start the encoder just to leave it unfinished
                return false;
            }
            var settings = new Gifski.Settings();
            settings.setHeight(height);
            settings.setWidth(width);
            settings.setQuality(90);
            settings.setRepeat((short) 0);
            var gifski = new Gifski(settings);
            gifski.setFileOutput(path);
            var framePosition = 0;
            do {
                var pts = (double) framePosition / fps;
                gifski.addFrameBitmap(framePosition, bitmap, pts);
                framePosition++;
            } while (source.getNextFrame(bitmap) == 1);
            gifski.finish();
            return true;
        } catch (Throwable e) {
            FileLog.e(e);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            source.releaseForGenerateCache();
        }
        return false;
    }
}
