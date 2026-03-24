package com.smartkrishi.presentation.addNewFarm;

import android.Manifest;
import android.location.Geocoder;
import android.net.Uri;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.focus.FocusDirection;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.input.KeyboardType;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.smartkrishi.R;
import com.smartkrishi.presentation.model.Farm;
import java.util.Locale;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000f\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u0016\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a\u0084\u0001\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\n2\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00060\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\n2\u0006\u0010\u0012\u001a\u00020\u00132\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0006H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0015\u0010\u0016\u001al\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\n2\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u001b\u001a\u00020\u001cH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001a\"\u0010\u001f\u001a\u00020\u00012\u0006\u0010 \u001a\u00020\u00062\u0006\u0010!\u001a\u00020\u0013H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\"\u0010#\u001aB\u0010$\u001a\u00020\u00012\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2(\u0010)\u001a$\u0012\u0004\u0012\u00020+\u0012\u0004\u0012\u00020+\u0012\u0006\u0012\u0004\u0018\u00010\u0006\u0012\u0006\u0012\u0004\u0018\u00010\u0006\u0012\u0004\u0012\u00020\u00010*H\u0002\u001a\u001a\u0010,\u001a\u00020-2\u0006\u0010\u0012\u001a\u00020\u0013H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b.\u0010/\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00060"}, d2 = {"AddNewFarm", "", "onFarmSaved", "Lkotlin/Function0;", "CustomDropdown", "value", "", "expanded", "", "onExpandedChange", "Lkotlin/Function1;", "label", "placeholder", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "options", "", "onSelect", "primaryGreen", "Landroidx/compose/ui/graphics/Color;", "error", "CustomDropdown-uyGnus8", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/util/List;Lkotlin/jvm/functions/Function1;JLjava/lang/String;)V", "CustomTextField", "onValueChange", "keyboardType", "Landroidx/compose/ui/text/input/KeyboardType;", "focusManager", "Landroidx/compose/ui/focus/FocusManager;", "CustomTextField-nGjWBgU", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JILjava/lang/String;Landroidx/compose/ui/focus/FocusManager;)V", "SectionHeader", "text", "color", "SectionHeader-4WTKRHQ", "(Ljava/lang/String;J)V", "fetchLocationAndSoil", "context", "Landroid/content/Context;", "fused", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "onLoc", "Lkotlin/Function4;", "", "textFieldColors", "Landroidx/compose/material3/TextFieldColors;", "textFieldColors-8_81llA", "(J)Landroidx/compose/material3/TextFieldColors;", "app_debug"})
public final class AddNewFarmKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AddNewFarm(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onFarmSaved) {
    }
    
    private static final void fetchLocationAndSoil(android.content.Context context, com.google.android.gms.location.FusedLocationProviderClient fused, kotlin.jvm.functions.Function4<? super java.lang.Double, ? super java.lang.Double, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onLoc) {
    }
}