package com.smartkrishi.presentation.home;

import android.util.Log;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartkrishi.R;
import com.smartkrishi.presentation.model.Farm;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000p\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J4\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\f2\u000e\b\u0002\u0010 \u001a\b\u0012\u0004\u0012\u00020\u001e0!2\u0014\b\u0002\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u001e0#J\u0006\u0010$\u001a\u00020\u001eJ\u0006\u0010%\u001a\u00020\u001eJ4\u0010&\u001a\u00020\u001e2\u0006\u0010\'\u001a\u00020\t2\u000e\b\u0002\u0010 \u001a\b\u0012\u0004\u0012\u00020\u001e0!2\u0014\b\u0002\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u001e0#J\b\u0010(\u001a\u0004\u0018\u00010\tJ\b\u0010)\u001a\u0004\u0018\u00010\tJ\u0010\u0010*\u001a\u0004\u0018\u00010\f2\u0006\u0010\'\u001a\u00020\tJ\u0006\u0010+\u001a\u00020,J\u0006\u0010-\u001a\u00020.J\u0006\u0010/\u001a\u00020\u000eJ\u0006\u00100\u001a\u00020\u001eJ\u0006\u00101\u001a\u00020\u001eJ\b\u00102\u001a\u00020\u001eH\u0014J\u0006\u00103\u001a\u00020\u001eJ\u000e\u00104\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\fJ4\u00105\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\f2\u000e\b\u0002\u0010 \u001a\b\u0012\u0004\u0012\u00020\u001e0!2\u0014\b\u0002\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u001e0#R\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001d\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0013R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0013\u00a8\u00066"}, d2 = {"Lcom/smartkrishi/presentation/home/FarmViewModel;", "Landroidx/lifecycle/ViewModel;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "firestore", "Lcom/google/firebase/firestore/FirebaseFirestore;", "(Lcom/google/firebase/auth/FirebaseAuth;Lcom/google/firebase/firestore/FirebaseFirestore;)V", "_errorMessage", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_farms", "", "Lcom/smartkrishi/presentation/model/Farm;", "_isLoading", "", "_selectedFarm", "errorMessage", "Lkotlinx/coroutines/flow/StateFlow;", "getErrorMessage", "()Lkotlinx/coroutines/flow/StateFlow;", "farms", "getFarms", "farmsListener", "Lcom/google/firebase/firestore/ListenerRegistration;", "isLoading", "realtimeDb", "Lcom/google/firebase/database/FirebaseDatabase;", "selectedFarm", "getSelectedFarm", "addFarm", "", "farm", "onSuccess", "Lkotlin/Function0;", "onFailure", "Lkotlin/Function1;", "clearError", "clearSelectedFarm", "deleteFarm", "farmId", "getCurrentUserEmail", "getCurrentUserId", "getFarmById", "getFarmCount", "", "getFarmsStatistics", "Lcom/smartkrishi/presentation/home/FarmStatistics;", "hasFarms", "loadFarms", "logout", "onCleared", "refreshFarms", "selectFarm", "updateFarm", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class FarmViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.firestore.FirebaseFirestore firestore = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.database.FirebaseDatabase realtimeDb = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.smartkrishi.presentation.model.Farm>> _farms = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.smartkrishi.presentation.model.Farm>> farms = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _errorMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> errorMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.smartkrishi.presentation.model.Farm> _selectedFarm = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.smartkrishi.presentation.model.Farm> selectedFarm = null;
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.firestore.ListenerRegistration farmsListener;
    
    @javax.inject.Inject()
    public FarmViewModel(@org.jetbrains.annotations.NotNull()
    com.google.firebase.auth.FirebaseAuth auth, @org.jetbrains.annotations.NotNull()
    com.google.firebase.firestore.FirebaseFirestore firestore) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.smartkrishi.presentation.model.Farm>> getFarms() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getErrorMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.smartkrishi.presentation.model.Farm> getSelectedFarm() {
        return null;
    }
    
    public final void loadFarms() {
    }
    
    public final void addFarm(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.model.Farm farm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onFailure) {
    }
    
    public final void updateFarm(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.model.Farm farm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onFailure) {
    }
    
    public final void deleteFarm(@org.jetbrains.annotations.NotNull()
    java.lang.String farmId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onFailure) {
    }
    
    public final void selectFarm(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.model.Farm farm) {
    }
    
    public final void clearSelectedFarm() {
    }
    
    public final void refreshFarms() {
    }
    
    public final void clearError() {
    }
    
    public final int getFarmCount() {
        return 0;
    }
    
    public final boolean hasFarms() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.smartkrishi.presentation.model.Farm getFarmById(@org.jetbrains.annotations.NotNull()
    java.lang.String farmId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCurrentUserEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCurrentUserId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.smartkrishi.presentation.home.FarmStatistics getFarmsStatistics() {
        return null;
    }
    
    public final void logout() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}