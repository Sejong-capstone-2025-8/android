package com.toprunner.imagestory.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0011J\"\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00140\u00132\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0018JN\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\f2\u0006\u0010\u001c\u001a\u00020\f2\u0006\u0010\u001d\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\f2\u0006\u0010\u001f\u001a\u00020\u00142\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0086@\u00a2\u0006\u0002\u0010$J.\u0010%\u001a\u00020\n2\u0006\u0010&\u001a\u00020\u00102\n\b\u0002\u0010\'\u001a\u0004\u0018\u00010\u00142\n\b\u0002\u0010(\u001a\u0004\u0018\u00010!H\u0086@\u00a2\u0006\u0002\u0010)R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/toprunner/imagestory/repository/FairyTaleRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "fairyTaleDao", "Lcom/toprunner/imagestory/data/dao/FairyTaleDao;", "fileStorageManager", "Lcom/toprunner/imagestory/util/FileStorageManager;", "deleteFairyTale", "", "fairyTaleId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllFairyTales", "", "Lcom/toprunner/imagestory/data/entity/FairyTaleEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFairyTaleById", "Lkotlin/Pair;", "", "insertFairyTale", "", "fairyTale", "(Lcom/toprunner/imagestory/data/entity/FairyTaleEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveFairyTale", "title", "voiceId", "imageId", "textId", "musicId", "theme", "audioData", "", "voiceFeatures", "Lcom/toprunner/imagestory/model/VoiceFeatures;", "(Ljava/lang/String;JJJJLjava/lang/String;[BLcom/toprunner/imagestory/model/VoiceFeatures;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateFairyTale", "fairyTaleEntity", "newContent", "newAudioData", "(Lcom/toprunner/imagestory/data/entity/FairyTaleEntity;Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class FairyTaleRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.data.dao.FairyTaleDao fairyTaleDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.util.FileStorageManager fileStorageManager = null;
    
    public FairyTaleRepository(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveFairyTale(@org.jetbrains.annotations.NotNull()
    java.lang.String title, long voiceId, long imageId, long textId, long musicId, @org.jetbrains.annotations.NotNull()
    java.lang.String theme, @org.jetbrains.annotations.NotNull()
    byte[] audioData, @org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures voiceFeatures, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertFairyTale(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.data.entity.FairyTaleEntity fairyTale, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getFairyTaleById(long fairyTaleId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Pair<com.toprunner.imagestory.data.entity.FairyTaleEntity, java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllFairyTales(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.toprunner.imagestory.data.entity.FairyTaleEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteFairyTale(long fairyTaleId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateFairyTale(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.data.entity.FairyTaleEntity fairyTaleEntity, @org.jetbrains.annotations.Nullable()
    java.lang.String newContent, @org.jetbrains.annotations.Nullable()
    byte[] newAudioData, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}