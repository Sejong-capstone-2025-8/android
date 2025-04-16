package com.toprunner.imagestory.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0011J\u000e\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\bR\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\f8F\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000e\u00a8\u0006\u0017"}, d2 = {"Lcom/toprunner/imagestory/viewmodel/FairyTaleViewModel;", "Landroidx/lifecycle/ViewModel;", "fairyTaleRepository", "Lcom/toprunner/imagestory/repository/FairyTaleRepository;", "(Lcom/toprunner/imagestory/repository/FairyTaleRepository;)V", "_fairyTales", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/toprunner/imagestory/data/entity/FairyTaleEntity;", "_isLoading", "", "fairyTales", "Lkotlinx/coroutines/flow/StateFlow;", "getFairyTales", "()Lkotlinx/coroutines/flow/StateFlow;", "isLoading", "deleteFairyTale", "", "fairyTaleId", "", "loadFairyTales", "saveFairyTale", "fairyTaleEntity", "app_debug"})
public final class FairyTaleViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.toprunner.imagestory.repository.FairyTaleRepository fairyTaleRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.toprunner.imagestory.data.entity.FairyTaleEntity>> _fairyTales = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    
    public FairyTaleViewModel(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.repository.FairyTaleRepository fairyTaleRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.toprunner.imagestory.data.entity.FairyTaleEntity>> getFairyTales() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    public final void loadFairyTales() {
    }
    
    public final void saveFairyTale(@org.jetbrains.annotations.NotNull()
    com.toprunner.imagestory.data.entity.FairyTaleEntity fairyTaleEntity) {
    }
    
    public final void deleteFairyTale(long fairyTaleId) {
    }
}