package com.toprunner.imagestory.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bJ(\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\b2\u0018\u0010\r\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\b0\u000f0\u000eJ\u000e\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0004J\u000e\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/toprunner/imagestory/util/VoiceFeaturesUtil;", "", "()V", "TAG", "", "calculateSimilarity", "", "features1", "Lcom/toprunner/imagestory/model/VoiceFeatures;", "features2", "findBestMatchingVoice", "", "targetFeatures", "voicesList", "", "Lkotlin/Pair;", "parseVoiceFeatures", "json", "voiceFeaturesToJson", "voiceFeatures", "app_debug"})
public final class VoiceFeaturesUtil {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "VoiceFeaturesUtil";
    
    public VoiceFeaturesUtil() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.toprunner.imagestory.model.VoiceFeatures parseVoiceFeatures(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String voiceFeaturesToJson(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures voiceFeatures) {
        return null;
    }
    
    public final double calculateSimilarity(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures features1, @org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures features2) {
        return 0.0;
    }
    
    public final long findBestMatchingVoice(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.model.VoiceFeatures targetFeatures, @org.jetbrains.annotations.NotNull()
    java.util.List<kotlin.Pair<java.lang.Long, com.toprunner.imagestory.model.VoiceFeatures>> voicesList) {
        return 0L;
    }
}