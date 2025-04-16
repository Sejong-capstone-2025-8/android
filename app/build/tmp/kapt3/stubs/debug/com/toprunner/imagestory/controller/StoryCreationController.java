package com.toprunner.imagestory.controller;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u001aJ\u0010\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0006H\u0002J\u001e\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u00062\u0006\u0010!\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\"J\u0010\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020\u0006H\u0002J\u001c\u0010&\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\'2\u0006\u0010%\u001a\u00020\u0006H\u0002J\u0010\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020\u0018H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/toprunner/imagestory/controller/StoryCreationController;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "TAG", "", "fairyTaleRepository", "Lcom/toprunner/imagestory/repository/FairyTaleRepository;", "gptService", "Lcom/toprunner/imagestory/service/GPTService;", "imageRepository", "Lcom/toprunner/imagestory/repository/ImageRepository;", "textRepository", "Lcom/toprunner/imagestory/repository/TextRepository;", "ttsService", "Lcom/toprunner/imagestory/service/TTSService;", "voiceFeaturesUtil", "Lcom/toprunner/imagestory/util/VoiceFeaturesUtil;", "voiceRepository", "Lcom/toprunner/imagestory/repository/VoiceRepository;", "createStory", "", "image", "Landroid/graphics/Bitmap;", "theme", "(Landroid/graphics/Bitmap;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractVoiceFeatures", "Lcom/toprunner/imagestory/model/VoiceFeatures;", "gptResponse", "generateAudio", "", "storyText", "voiceId", "(Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "parseStoryResponse", "Lcom/toprunner/imagestory/model/Story;", "responseData", "processGPTResponse", "Lkotlin/Pair;", "validateImage", "", "bitmap", "app_debug"})
public final class StoryCreationController {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "StoryCreationController";
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.service.GPTService gptService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.service.TTSService ttsService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.repository.FairyTaleRepository fairyTaleRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.repository.ImageRepository imageRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.repository.TextRepository textRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.repository.VoiceRepository voiceRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.util.VoiceFeaturesUtil voiceFeaturesUtil = null;
    
    public StoryCreationController(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createStory(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap image, @org.jetbrains.annotations.NotNull()
    java.lang.String theme, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    private final boolean validateImage(android.graphics.Bitmap bitmap) {
        return false;
    }
    
    private final kotlin.Pair<java.lang.String, java.lang.String> processGPTResponse(java.lang.String responseData) {
        return null;
    }
    
    private final com.toprunner.imagestory.model.VoiceFeatures extractVoiceFeatures(java.lang.String gptResponse) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateAudio(@org.jetbrains.annotations.NotNull()
    java.lang.String storyText, long voiceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super byte[]> $completion) {
        return null;
    }
    
    private final com.toprunner.imagestory.model.Story parseStoryResponse(java.lang.String responseData) {
        return null;
    }
}