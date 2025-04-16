package com.toprunner.imagestory.service;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\b\b\u0007\u0018\u0000 ,2\u00020\u0001:\u0001,B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0006J\u0010\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u0006H\u0002J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\bH\u0002J\u001e\u0010\u0017\u001a\u00020\u00152\u0006\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\u0019H\u0086@\u00a2\u0006\u0002\u0010\u001aJ\u0006\u0010\u001b\u001a\u00020\bJ\u0010\u0010\u001c\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u0006\u0010\u001d\u001a\u00020\u001eJ\u0006\u0010\u001f\u001a\u00020\bJ\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020\"0!H\u0086@\u00a2\u0006\u0002\u0010#J\u0006\u0010$\u001a\u00020%J\u0006\u0010&\u001a\u00020%J\u000e\u0010\'\u001a\u00020%2\u0006\u0010\u0011\u001a\u00020\u0006J\u0006\u0010(\u001a\u00020%J\u000e\u0010)\u001a\u00020%2\u0006\u0010*\u001a\u00020\bJ\u0006\u0010+\u001a\u00020%R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/toprunner/imagestory/service/TTSService;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "currentAudioPath", "", "currentPosition", "", "mediaPlayer", "Landroid/media/MediaPlayer;", "networkUtil", "Lcom/toprunner/imagestory/util/NetworkUtil;", "totalDuration", "voiceRepository", "Lcom/toprunner/imagestory/repository/VoiceRepository;", "calculateAudioDuration", "audioPath", "createRequestBody", "text", "generateDummyAudio", "", "textLength", "generateVoice", "voiceId", "", "(Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCurrentPosition", "getElevenlabsVoiceId", "getPlaybackProgress", "", "getTotalDuration", "getVoiceList", "", "Lcom/toprunner/imagestory/data/entity/VoiceEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isPlaying", "", "pauseAudio", "playAudio", "resumeAudio", "seekTo", "positionMs", "stopAudio", "Companion", "app_debug"})
public final class TTSService {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.repository.VoiceRepository voiceRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.util.NetworkUtil networkUtil = null;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaPlayer mediaPlayer;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentAudioPath;
    private int currentPosition = 0;
    private int totalDuration = 0;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String API_URL = "https://api.elevenlabs.io/v1/text-to-speech";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String API_KEY = "null";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "TTSService";
    @org.jetbrains.annotations.NotNull()
    public static final com.toprunner.imagestory.service.TTSService.Companion Companion = null;
    
    public TTSService(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateVoice(@org.jetbrains.annotations.NotNull()
    java.lang.String text, long voiceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super byte[]> $completion) {
        return null;
    }
    
    private final byte[] generateDummyAudio(int textLength) {
        return null;
    }
    
    private final java.lang.String getElevenlabsVoiceId(long voiceId) {
        return null;
    }
    
    private final java.lang.String createRequestBody(java.lang.String text) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getVoiceList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.toprunner.imagestory.data.entity.VoiceEntity>> $completion) {
        return null;
    }
    
    public final boolean playAudio(@org.jetbrains.annotations.NotNull()
    java.lang.String audioPath) {
        return false;
    }
    
    public final boolean pauseAudio() {
        return false;
    }
    
    public final boolean resumeAudio() {
        return false;
    }
    
    public final boolean stopAudio() {
        return false;
    }
    
    public final int getCurrentPosition() {
        return 0;
    }
    
    public final int getTotalDuration() {
        return 0;
    }
    
    public final float getPlaybackProgress() {
        return 0.0F;
    }
    
    public final boolean isPlaying() {
        return false;
    }
    
    public final int calculateAudioDuration(@org.jetbrains.annotations.NotNull()
    java.lang.String audioPath) {
        return 0;
    }
    
    public final boolean seekTo(int positionMs) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/toprunner/imagestory/service/TTSService$Companion;", "", "()V", "API_KEY", "", "API_URL", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}