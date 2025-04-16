package com.toprunner.imagestory;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010+\u001a\u00020,H\u0002J\b\u0010-\u001a\u00020,H\u0002J\n\u0010.\u001a\u0004\u0018\u00010/H\u0002J\b\u00100\u001a\u00020,H\u0002J\u0012\u00101\u001a\u00020,2\b\u00102\u001a\u0004\u0018\u000103H\u0014J\b\u00104\u001a\u00020,H\u0002J\b\u00105\u001a\u00020,H\u0002J\u0010\u00106\u001a\u00020,2\u0006\u00107\u001a\u000208H\u0002R/\u0010\u0005\u001a\u0004\u0018\u00010\u00042\b\u0010\u0003\u001a\u0004\u0018\u00010\u00048B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\n\u0010\u000b\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR/\u0010\r\u001a\u0004\u0018\u00010\f2\b\u0010\u0003\u001a\u0004\u0018\u00010\f8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0012\u0010\u000b\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R+\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0003\u001a\u00020\u00138B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0018\u0010\u000b\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R/\u0010\u001e\u001a\u0004\u0018\u00010\u001b2\b\u0010\u0003\u001a\u0004\u0018\u00010\u001b8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b#\u0010\u000b\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u001b\u0010$\u001a\u00020%8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b(\u0010)\u001a\u0004\b&\u0010\'R\u0014\u0010*\u001a\b\u0012\u0004\u0012\u00020\f0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00069"}, d2 = {"Lcom/toprunner/imagestory/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "<set-?>", "Landroid/graphics/Bitmap;", "capturedImageBitmap", "getCapturedImageBitmap", "()Landroid/graphics/Bitmap;", "setCapturedImageBitmap", "(Landroid/graphics/Bitmap;)V", "capturedImageBitmap$delegate", "Landroidx/compose/runtime/MutableState;", "Landroid/net/Uri;", "capturedImageUri", "getCapturedImageUri", "()Landroid/net/Uri;", "setCapturedImageUri", "(Landroid/net/Uri;)V", "capturedImageUri$delegate", "", "isLoading", "()Z", "setLoading", "(Z)V", "isLoading$delegate", "pickImageLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "requestCameraPermissionLauncher", "requestStoragePermissionLauncher", "selectedTheme", "getSelectedTheme", "()Ljava/lang/String;", "setSelectedTheme", "(Ljava/lang/String;)V", "selectedTheme$delegate", "storyCreationController", "Lcom/toprunner/imagestory/controller/StoryCreationController;", "getStoryCreationController", "()Lcom/toprunner/imagestory/controller/StoryCreationController;", "storyCreationController$delegate", "Lkotlin/Lazy;", "takePictureLauncher", "checkCameraPermissionAndOpenCameraNonCompose", "", "checkStoragePermissionAndOpenGallery", "createImageFile", "Ljava/io/File;", "initializeDefaultData", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "openCamera", "openGallery", "startStoryCreation", "navController", "Landroidx/navigation/NavController;", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState capturedImageUri$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState capturedImageBitmap$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState selectedTheme$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isLoading$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy storyCreationController$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestCameraPermissionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestStoragePermissionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> takePictureLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> pickImageLauncher = null;
    
    public MainActivity() {
        super(0);
    }
    
    private final android.net.Uri getCapturedImageUri() {
        return null;
    }
    
    private final void setCapturedImageUri(android.net.Uri p0) {
    }
    
    private final android.graphics.Bitmap getCapturedImageBitmap() {
        return null;
    }
    
    private final void setCapturedImageBitmap(android.graphics.Bitmap p0) {
    }
    
    private final java.lang.String getSelectedTheme() {
        return null;
    }
    
    private final void setSelectedTheme(java.lang.String p0) {
    }
    
    private final boolean isLoading() {
        return false;
    }
    
    private final void setLoading(boolean p0) {
    }
    
    private final com.toprunner.imagestory.controller.StoryCreationController getStoryCreationController() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initializeDefaultData() {
    }
    
    private final void checkCameraPermissionAndOpenCameraNonCompose() {
    }
    
    private final void checkStoragePermissionAndOpenGallery() {
    }
    
    private final void openCamera() {
    }
    
    private final void openGallery() {
    }
    
    private final java.io.File createImageFile() {
        return null;
    }
    
    private final void startStoryCreation(androidx.navigation.NavController navController) {
    }
}