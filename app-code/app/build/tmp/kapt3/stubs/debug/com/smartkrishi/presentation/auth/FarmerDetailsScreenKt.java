package com.smartkrishi.presentation.auth;

import com.smartkrishi.data.model.FarmerProfile;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.focus.FocusDirection;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.input.OffsetMapping;
import androidx.compose.ui.text.input.TransformedText;
import androidx.compose.ui.text.input.VisualTransformation;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.text.style.TextAlign;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000V\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u001al\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00012\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00030\u00062\u0006\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0012\u0010\u0013\u001a(\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0015\u001a\u00020\u00012\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00030\u00172\b\b\u0002\u0010\u0018\u001a\u00020\u0019H\u0007\u001a\"\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u001c\u001a\u00020\fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001aJ\u0010\u001f\u001a\u00020\u00032\u0006\u0010 \u001a\u00020!2$\u0010\"\u001a \u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00030#2\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00030\u0006H\u0002\u001a\u001a\u0010%\u001a\u00020&2\u0006\u0010\u000b\u001a\u00020\fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\'\u0010(\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006)"}, d2 = {"TAG", "", "CustomTextField", "", "value", "onValueChange", "Lkotlin/Function1;", "label", "placeholder", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "primaryGreen", "Landroidx/compose/ui/graphics/Color;", "keyboardType", "Landroidx/compose/ui/text/input/KeyboardType;", "focusManager", "Landroidx/compose/ui/focus/FocusManager;", "prefix", "CustomTextField-nGjWBgU", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JILandroidx/compose/ui/focus/FocusManager;Ljava/lang/String;)V", "FarmerDetailsScreen", "email", "onSubmitSuccess", "Lkotlin/Function0;", "viewModel", "Lcom/smartkrishi/presentation/auth/FarmerViewModel;", "SectionHeader", "text", "color", "SectionHeader-4WTKRHQ", "(Ljava/lang/String;J)V", "fetchCurrentLocation", "context", "Landroid/content/Context;", "onLocationFetched", "Lkotlin/Function4;", "onError", "textFieldColors", "Landroidx/compose/material3/TextFieldColors;", "textFieldColors-8_81llA", "(J)Landroidx/compose/material3/TextFieldColors;", "app_debug"})
public final class FarmerDetailsScreenKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "FarmerDetailsScreen";
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void FarmerDetailsScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSubmitSuccess, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.auth.FarmerViewModel viewModel) {
    }
    
    private static final void fetchCurrentLocation(android.content.Context context, kotlin.jvm.functions.Function4<? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onLocationFetched, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError) {
    }
}