package org.nemogram.messenger.export;

public class ExportSettings {
    public static final int[] MESSAGE_LIMITS = {0, 500, 1_000, 5_000, 10_000};
    public static final long[] MEDIA_LIMITS = {0L,
            50L * 1024 * 1024, // 50 MB
            200L * 1024 * 1024, // 200 MB
            500L * 1024 * 1024, // 500 MB
            1024L * 1024 * 1024}; // 1 GB
    public Format format = Format.HTML;
    public boolean includePhotos = true;
    public boolean includeVideos = true;
    public boolean includeFiles = true;
    public boolean includeVoice = true;
    public boolean includeMusic = true;
    public int maxMessages = 0;
    public long maxMediaBytes = 0L;

    public enum Format {HTML, JSON}
}