package com.toprunner.imagestory.service;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0006H\u0002J\u000e\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u000bJ\u0010\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u0006H\u0002J\u001e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u001e\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/toprunner/imagestory/service/GPTService;", "", "()V", "networkUtil", "Lcom/toprunner/imagestory/util/NetworkUtil;", "createRequestBody", "", "base64Image", "theme", "encodeImageToBase64", "bitmap", "Landroid/graphics/Bitmap;", "generateErrorResponse", "errorMessage", "generateFairyTaleEntity", "Lcom/toprunner/imagestory/data/entity/FairyTaleEntity;", "image", "(Landroid/graphics/Bitmap;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateStory", "Companion", "app_debug"})
public final class GPTService {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String API_URL = "https://api.openai.com/v1/chat/completions";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String API_KEY = "null";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "GPTService";
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.util.NetworkUtil networkUtil = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.toprunner.imagestory.service.GPTService.Companion Companion = null;
    
    public GPTService() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateStory(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap image, @org.jetbrains.annotations.NotNull()
    java.lang.String theme, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateFairyTaleEntity(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap image, @org.jetbrains.annotations.NotNull()
    java.lang.String theme, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.toprunner.imagestory.data.entity.FairyTaleEntity> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String encodeImageToBase64(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap) {
        return null;
    }
    
    private final java.lang.String createRequestBody(java.lang.String base64Image, java.lang.String theme) {
        return null;
    }
    
    private final java.lang.String generateErrorResponse(java.lang.String errorMessage) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/toprunner/imagestory/service/GPTService$Companion;", "", "()V", "API_KEY", "", "API_URL", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}