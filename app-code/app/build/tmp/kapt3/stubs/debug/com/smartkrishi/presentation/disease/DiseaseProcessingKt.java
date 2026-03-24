package com.smartkrishi.presentation.disease;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.navigation.NavController;
import com.smartkrishi.ml.PlantDiseaseClassifier;
import com.smartkrishi.presentation.navigation.Screen;
import com.smartkrishi.presentation.theme.ThemeState;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u0007\u001a\u001e\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0007\u001a\u0018\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0005H\u0002\u00a8\u0006\u0010"}, d2 = {"DiseaseProcessingScreen", "", "navController", "Landroidx/navigation/NavController;", "imageUri", "Landroid/net/Uri;", "ErrorDialog", "message", "", "onDismiss", "Lkotlin/Function0;", "loadBitmapFromUri", "Landroid/graphics/Bitmap;", "context", "Landroid/content/Context;", "uri", "app_debug"})
public final class DiseaseProcessingKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void DiseaseProcessingScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.Nullable()
    android.net.Uri imageUri) {
    }
    
    private static final android.graphics.Bitmap loadBitmapFromUri(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ErrorDialog(@org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}