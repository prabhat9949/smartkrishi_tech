package com.smartkrishi.presentation.navigation;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.DrawerState;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.navigation.NavType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.smartkrishi.R;
import com.smartkrishi.presentation.disease.*;
import com.smartkrishi.presentation.home.FarmViewModel;
import com.smartkrishi.presentation.model.Farm;
import com.smartkrishi.utils.SessionManager;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000,\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u001a(\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u000e\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\tH\u0007\u001a \u0010\u000b\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000eH\u0002\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"TAG", "", "NavGraph", "", "navController", "Landroidx/navigation/NavHostController;", "drawerState", "Landroidx/compose/material3/DrawerState;", "selectedFarmState", "Landroidx/compose/runtime/MutableState;", "Lcom/smartkrishi/presentation/model/Farm;", "checkUserProfileAndNavigate", "email", "context", "Landroid/content/Context;", "app_debug"})
public final class NavGraphKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "NavGraph";
    
    @android.annotation.SuppressLint(value = {"StateFlowValueCalledInComposition"})
    @androidx.compose.runtime.Composable()
    public static final void NavGraph(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavHostController navController, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.DrawerState drawerState, @org.jetbrains.annotations.NotNull()
    androidx.compose.runtime.MutableState<com.smartkrishi.presentation.model.Farm> selectedFarmState) {
    }
    
    /**
     * Checks if the user has completed their farmer profile.
     * If yes -> Navigate to HomeLanding
     * If no -> Navigate to FarmerDetails
     */
    private static final void checkUserProfileAndNavigate(androidx.navigation.NavHostController navController, java.lang.String email, android.content.Context context) {
    }
}