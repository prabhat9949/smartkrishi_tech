package com.smartkrishi.presentation.market;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.TextFieldValue;
import androidx.compose.ui.text.style.TextAlign;
import androidx.navigation.NavController;
import com.smartkrishi.presentation.theme.ThemeState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004J\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u0004J\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0004J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r\u00a8\u0006\u000f"}, d2 = {"Lcom/smartkrishi/presentation/market/MarketRepositoryMock;", "", "()V", "getNearbyMandis", "", "Lcom/smartkrishi/presentation/market/NearbyMandiPrice;", "getPredictions", "Lcom/smartkrishi/presentation/market/PredictionDay;", "getPriceTrend7D", "Lcom/smartkrishi/presentation/market/PricePoint;", "getTodayPrice", "Lcom/smartkrishi/presentation/market/MarketPrice;", "commodity", "", "location", "app_debug"})
public final class MarketRepositoryMock {
    @org.jetbrains.annotations.NotNull()
    public static final com.smartkrishi.presentation.market.MarketRepositoryMock INSTANCE = null;
    
    private MarketRepositoryMock() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.smartkrishi.presentation.market.MarketPrice getTodayPrice(@org.jetbrains.annotations.NotNull()
    java.lang.String commodity, @org.jetbrains.annotations.NotNull()
    java.lang.String location) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.smartkrishi.presentation.market.PricePoint> getPriceTrend7D() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.smartkrishi.presentation.market.PredictionDay> getPredictions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.smartkrishi.presentation.market.NearbyMandiPrice> getNearbyMandis() {
        return null;
    }
}