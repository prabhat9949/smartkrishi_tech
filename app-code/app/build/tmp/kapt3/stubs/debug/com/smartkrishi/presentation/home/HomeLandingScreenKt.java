package com.smartkrishi.presentation.home;

import android.util.Log;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import coil.request.ImageRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.smartkrishi.R;
import com.smartkrishi.presentation.model.Farm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000j\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0005\u001a\u001e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001a\u0016\u0010\u000e\u001a\u00020\t2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001a&\u0010\u000f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001a&\u0010\u0012\u001a\u00020\t2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0018H\u0003\u001a\u0016\u0010\u0019\u001a\u00020\t2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001a@\u0010\u001b\u001a\u00020\t2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020\u00142\u0006\u0010 \u001a\u00020\u00142\b\b\u0002\u0010!\u001a\u00020\"2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001a8\u0010#\u001a\u00020\t2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001f\u001a\u00020\u00142\u0006\u0010$\u001a\u00020\u00142\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\r2\b\b\u0002\u0010%\u001a\u00020\u0011H\u0003\u001a:\u0010&\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\t0\r2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\t0\r2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001a&\u0010*\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001a\u001e\u0010+\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u0003\u001aj\u0010,\u001a\u00020\t2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\t0-2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\t0\r2\u0012\u0010(\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\t0-2\u0012\u0010.\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\t0-2\f\u0010/\u001a\b\u0012\u0004\u0012\u00020\t0\r2\b\b\u0002\u00100\u001a\u000201H\u0007\u001a\\\u00102\u001a\u00020\t2\u0006\u00103\u001a\u0002042\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00182\u0006\u00105\u001a\u00020\u00162\u0012\u00106\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0016072\f\u00108\u001a\b\u0012\u0004\u0012\u00020\t0\r2\u0012\u00109\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\t0-H\u0003\u001a\f\u0010:\u001a\u00020\u0014*\u00020\u0014H\u0002\u001a\f\u0010;\u001a\u00020\u0014*\u00020\u0014H\u0002\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0007\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u00a8\u0006<"}, d2 = {"AccentGreen", "Landroidx/compose/ui/graphics/Color;", "J", "CardBackground", "ErrorRed", "LightBackground", "PrimaryGreen", "SecondaryGreen", "AcresListItem", "", "farm", "Lcom/smartkrishi/presentation/model/Farm;", "onClick", "Lkotlin/Function0;", "AnimatedFAB", "AnimatedFarmCard", "isSelected", "", "CropDistributionItem", "cropType", "", "count", "", "farms", "", "EmptyFarmState", "onAddFarm", "EnhancedStatCard", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "value", "label", "subtitle", "modifier", "Landroidx/compose/ui/Modifier;", "FarmActionRow", "description", "isDestructive", "FarmActionSheet", "onOpenFarm", "onEditFarm", "onDeleteFarmClick", "FarmCardItem", "FarmListItem", "HomeLandingScreen", "Lkotlin/Function1;", "onDeleteFarm", "onSkip", "viewModel", "Lcom/smartkrishi/presentation/home/FarmViewModel;", "StatisticsDetailDialog", "statType", "Lcom/smartkrishi/presentation/home/StatType;", "totalAcres", "cropDistribution", "", "onDismiss", "onFarmClick", "capitalize", "cleanCropType", "app_debug"})
public final class HomeLandingScreenKt {
    private static final long PrimaryGreen = 0L;
    private static final long SecondaryGreen = 0L;
    private static final long AccentGreen = 0L;
    private static final long LightBackground = 0L;
    private static final long CardBackground = 0L;
    private static final long ErrorRed = 0L;
    
    private static final java.lang.String cleanCropType(java.lang.String $this$cleanCropType) {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void HomeLandingScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.smartkrishi.presentation.model.Farm, kotlin.Unit> onOpenFarm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAddFarm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.smartkrishi.presentation.model.Farm, kotlin.Unit> onEditFarm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.smartkrishi.presentation.model.Farm, kotlin.Unit> onDeleteFarm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSkip, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.home.FarmViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void EnhancedStatCard(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String value, java.lang.String label, java.lang.String subtitle, androidx.compose.ui.Modifier modifier, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatisticsDetailDialog(com.smartkrishi.presentation.home.StatType statType, java.util.List<com.smartkrishi.presentation.model.Farm> farms, int totalAcres, java.util.Map<java.lang.String, java.lang.Integer> cropDistribution, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super com.smartkrishi.presentation.model.Farm, kotlin.Unit> onFarmClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FarmListItem(com.smartkrishi.presentation.model.Farm farm, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AcresListItem(com.smartkrishi.presentation.model.Farm farm, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void CropDistributionItem(java.lang.String cropType, int count, java.util.List<com.smartkrishi.presentation.model.Farm> farms) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void EmptyFarmState(kotlin.jvm.functions.Function0<kotlin.Unit> onAddFarm) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FarmActionSheet(com.smartkrishi.presentation.model.Farm farm, kotlin.jvm.functions.Function0<kotlin.Unit> onOpenFarm, kotlin.jvm.functions.Function0<kotlin.Unit> onEditFarm, kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteFarmClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FarmActionRow(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, java.lang.String description, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, boolean isDestructive) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AnimatedFarmCard(com.smartkrishi.presentation.model.Farm farm, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FarmCardItem(com.smartkrishi.presentation.model.Farm farm, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AnimatedFAB(kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final java.lang.String capitalize(java.lang.String $this$capitalize) {
        return null;
    }
}