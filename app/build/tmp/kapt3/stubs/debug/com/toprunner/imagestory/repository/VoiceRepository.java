package com.toprunner.imagestory.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0012\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013H\u0086@\u00a2\u0006\u0002\u0010\u0015J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0006H\u0002J\u0018\u0010\u0019\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0018\u0010\u001a\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u001e\u0010\u001b\u001a\u00020\u00102\u0006\u0010\u001c\u001a\u00020\u00062\u0006\u0010\u001d\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010\u001eJ.\u0010\u001f\u001a\u00020\u00102\u0006\u0010 \u001a\u00020\u00062\u0006\u0010!\u001a\u00020\u00062\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010%R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/toprunner/imagestory/repository/VoiceRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "TAG", "", "fileStorageManager", "Lcom/toprunner/imagestory/util/FileStorageManager;", "voiceDao", "Lcom/toprunner/imagestory/data/dao/VoiceDao;", "voiceFeaturesUtil", "Lcom/toprunner/imagestory/util/VoiceFeaturesUtil;", "deleteVoice", "", "voiceId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllVoices", "", "Lcom/toprunner/imagestory/data/entity/VoiceEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDefaultVoiceFeatures", "Lcom/toprunner/imagestory/model/VoiceFeatures;", "voiceType", "getVoiceById", "getVoiceFeatures", "recommendVoice", "theme", "targetFeatures", "(Ljava/lang/String;Lcom/toprunner/imagestory/model/VoiceFeatures;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveVoice", "title", "attributeJson", "audioData", "", "voiceFeatures", "(Ljava/lang/String;Ljava/lang/String;[BLcom/toprunner/imagestory/model/VoiceFeatures;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class VoiceRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "VoiceRepository";
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.data.dao.VoiceDao voiceDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.util.FileStorageManager fileStorageManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.util.VoiceFeaturesUtil voiceFeaturesUtil = null;
    
    public VoiceRepository(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveVoice(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String attributeJson, @org.jetbrains.annotations.NotNull()
    byte[] audioData, @org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures voiceFeatures, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getVoiceById(long voiceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.toprunner.imagestory.data.entity.VoiceEntity> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getVoiceFeatures(long voiceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.toprunner.imagestory.model.VoiceFeatures> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllVoices(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.toprunner.imagestory.data.entity.VoiceEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteVoice(long voiceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object recommendVoice(@org.jetbrains.annotations.NotNull()
    java.lang.String theme, @org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures targetFeatures, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    private final com.toprunner.imagestory.model.VoiceFeatures getDefaultVoiceFeatures(java.lang.String voiceType) {
        return null;
    }
}