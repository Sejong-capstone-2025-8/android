package com.toprunner.imagestory.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0018\u0010\u000f\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0010\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u0012\u00a8\u0006\u0014"}, d2 = {"Lcom/toprunner/imagestory/data/dao/MusicDao;", "", "deleteMusic", "", "musicId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllMusic", "", "Lcom/toprunner/imagestory/data/entity/MusicEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMusicByGenre", "genre", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMusicById", "insertMusic", "musicEntity", "(Lcom/toprunner/imagestory/data/entity/MusicEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateMusic", "app_debug"})
@androidx.room.Dao()
public abstract interface MusicDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMusic(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.data.entity.MusicEntity musicEntity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM musics WHERE music_id = :musicId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMusicById(long musicId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.toprunner.imagestory.data.entity.MusicEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM musics")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllMusic(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.toprunner.imagestory.data.entity.MusicEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM musics WHERE attribute = :genre")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMusicByGenre(@org.jetbrains.annotations.NotNull()
    java.lang.String genre, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.toprunner.imagestory.data.entity.MusicEntity>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM musics WHERE music_id = :musicId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMusic(long musicId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMusic(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.data.entity.MusicEntity musicEntity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
}