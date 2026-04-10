package com.smartkrishi.presentation.addNewFarm;

import android.net.Uri;
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
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import coil.request.ImageRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.smartkrishi.R;
import com.smartkrishi.presentation.home.FarmViewModel;
import com.smartkrishi.presentation.model.Farm;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000H\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0080\u0001\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00030\b2\u0006\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00030\b2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0013\u0010\u0014\u001a4\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00030\u001b2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00030\u001bH\u0007\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001d"}, d2 = {"TAG", "", "CustomDropdown", "", "value", "expanded", "", "onExpandedChange", "Lkotlin/Function1;", "label", "placeholder", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "options", "", "onSelect", "primaryGreen", "Landroidx/compose/ui/graphics/Color;", "darkText", "CustomDropdown-Samhc0A", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/util/List;Lkotlin/jvm/functions/Function1;JJ)V", "EditFarmScreen", "farm", "Lcom/smartkrishi/presentation/model/Farm;", "viewModel", "Lcom/smartkrishi/presentation/home/FarmViewModel;", "onFarmUpdated", "Lkotlin/Function0;", "onCancel", "app_debug"})
public final class EditFarmScreenKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "EditFarmScreen";
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void EditFarmScreen(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.model.Farm farm, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.home.FarmViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onFarmUpdated, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onCancel) {
    }
}