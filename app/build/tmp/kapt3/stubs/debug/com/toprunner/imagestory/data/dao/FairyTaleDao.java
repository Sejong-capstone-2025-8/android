package com.toprunner.imagestory.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u0010"}, d2 = {"Lcom/toprunner/imagestory/data/dao/FairyTaleDao;", "", "deleteFairyTale", "", "fairyTaleId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllFairyTales", "", "Lcom/toprunner/imagestory/data/entity/FairyTaleEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFairyTaleById", "insertFairyTale", "fairyTaleEntity", "(Lcom/toprunner/imagestory/data/entity/FairyTaleEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateFairyTale", "app_debug"})
@androidx.room.Dao()
public abstract interface FairyTaleDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertFairyTale(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.data.entity.FairyTaleEntity fairyTaleEntity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM fairy_tales WHERE fairy_tales_id = :fairyTaleId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFairyTaleById(long fairyTaleId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.toprunner.imagestory.data.entity.FairyTaleEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM fairy_tales ORDER BY created_at DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllFairyTales(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.toprunner.imagestory.data.entity.FairyTaleEntity>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM fairy_tales WHERE fairy_tales_id = :fairyTaleId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFairyTale(long fairyTaleId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateFairyTale(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.data.entity.FairyTaleEntity fairyTaleEntity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
}