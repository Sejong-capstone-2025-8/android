package com.toprunner.imagestory.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\b\u0007\u0018\u0000 !2\u00020\u0001:\u0001!B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0004J\u0016\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\u0004J\"\u0010\u0011\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\u0012\u001a\u00020\n2\b\b\u0002\u0010\t\u001a\u00020\nJ\u000e\u0010\u0013\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u0004J\u000e\u0010\u0014\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u0004J\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u00162\u0006\u0010\r\u001a\u00020\u0004J\"\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u00062\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0004J\"\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0004J\"\u0010\u001d\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001e\u001a\u00020\u00042\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0004J\"\u0010\u001f\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010 \u001a\u00020\u00162\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/toprunner/imagestory/util/FileStorageManager;", "", "()V", "TAG", "", "bitmapToByteArray", "", "bitmap", "Landroid/graphics/Bitmap;", "quality", "", "deleteFile", "", "filePath", "generateUniqueFileName", "prefix", "extension", "optimizeImage", "maxDimension", "readAudioFile", "readTextFile", "readVoiceFeatures", "Lcom/toprunner/imagestory/model/VoiceFeatures;", "saveAudioFile", "context", "Landroid/content/Context;", "audioData", "fileName", "saveImageFile", "saveTextFile", "content", "saveVoiceFeatures", "voiceFeatures", "Companion", "app_debug"})
public final class FileStorageManager {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "FileStorageManager";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String STORY_CONTENT_DIR = "story_contents";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String STORY_IMAGE_DIR = "story_images";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String AUDIO_DIR = "audio_files";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String VOICE_SAMPLE_DIR = "voice_samples";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String MFCC_DATA_DIR = "mfcc_data";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String MUSIC_DIR = "music_files";
    @org.jetbrains.annotations.NotNull()
    public static final com.toprunner.imagestory.util.FileStorageManager.Companion Companion = null;
    
    public FileStorageManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String saveTextFile(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.Nullable()
    java.lang.String fileName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String saveAudioFile(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    byte[] audioData, @org.jetbrains.annotations.Nullable()
    java.lang.String fileName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String saveImageFile(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.Nullable()
    java.lang.String fileName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String saveVoiceFeatures(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures voiceFeatures, @org.jetbrains.annotations.Nullable()
    java.lang.String fileName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String readTextFile(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] readAudioFile(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.toprunner.imagestory.model.VoiceFeatures readVoiceFeatures(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
        return null;
    }
    
    public final boolean deleteFile(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateUniqueFileName(@org.jetbrains.annotations.NotNull()
    java.lang.String prefix, @org.jetbrains.annotations.NotNull()
    java.lang.String extension) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.Bitmap optimizeImage(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap, int maxDimension, int quality) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] bitmapToByteArray(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap, int quality) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/toprunner/imagestory/util/FileStorageManager$Companion;", "", "()V", "AUDIO_DIR", "", "MFCC_DATA_DIR", "MUSIC_DIR", "STORY_CONTENT_DIR", "STORY_IMAGE_DIR", "VOICE_SAMPLE_DIR", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}