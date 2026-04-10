package com.smartkrishi.presentation.auth;

import android.util.Log;
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
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.text.input.PasswordVisualTransformation;
import androidx.compose.ui.text.input.VisualTransformation;
import androidx.compose.ui.text.style.TextAlign;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.smartkrishi.utils.SessionManager;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000X\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a\u0084\u0001\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00012\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00030\u00062\u0006\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000e2\u0010\b\u0002\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00102\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0013\u0010\u0014\u001a+\u0010\u0015\u001a\u00020\u00032!\u0010\u0016\u001a\u001d\u0012\u0013\u0012\u00110\u0001\u00a2\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020\u00030\u0006H\u0007\u001a\u008e\u0001\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00012\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00030\u00062\u0006\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\u00012\u0006\u0010\u001b\u001a\u00020\u001c2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00030\u00102\b\b\u0002\u0010\r\u001a\u00020\u000e2\u0010\b\u0002\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00102\u0010\b\u0002\u0010\u001e\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00102\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001f\u0010 \u001a8\u0010!\u001a\u00020\u00032\u0006\u0010\"\u001a\u00020\u00012\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00030\u00102\u0006\u0010$\u001a\u00020\u001c2\u0006\u0010%\u001a\u00020&H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\'\u0010(\u001aH\u0010)\u001a\u00020\u00032\u0006\u0010\"\u001a\u00020\u00012\u0006\u0010*\u001a\u00020\n2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00030\u00102\u0006\u0010+\u001a\u00020\u001c2\u0006\u0010,\u001a\u00020&2\u0006\u0010-\u001a\u00020&H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b.\u0010/\u001a\b\u00100\u001a\u000201H\u0003\u001a<\u00102\u001a\u00020\u0003*\u0002032\u0006\u0010\"\u001a\u00020\u00012\u0006\u00104\u001a\u00020\u001c2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00030\u00102\u0006\u0010%\u001a\u00020&H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b5\u00106\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00067"}, d2 = {"TAG", "", "AuthTextField", "", "value", "onValueChange", "Lkotlin/Function1;", "label", "placeholder", "leadingIcon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "keyboardType", "Landroidx/compose/ui/text/input/KeyboardType;", "imeAction", "Landroidx/compose/ui/text/input/ImeAction;", "onNext", "Lkotlin/Function0;", "errorMessage", "prefix", "AuthTextField-P2Qh934", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;IILkotlin/jvm/functions/Function0;Ljava/lang/String;Ljava/lang/String;)V", "LoginScreen", "onLoginSuccess", "Lkotlin/ParameterName;", "name", "email", "PasswordTextField", "passwordVisible", "", "onVisibilityToggle", "onDone", "PasswordTextField-UictSG0", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function0;ILkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Ljava/lang/String;)V", "PrimaryButton", "text", "onClick", "loading", "primaryColor", "Landroidx/compose/ui/graphics/Color;", "PrimaryButton-g2O1Hgs", "(Ljava/lang/String;Lkotlin/jvm/functions/Function0;ZJ)V", "SocialButton", "icon", "enabled", "backgroundColor", "textColor", "SocialButton-VT9Kpxs", "(Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;Lkotlin/jvm/functions/Function0;ZJJ)V", "textFieldColors", "Landroidx/compose/material3/TextFieldColors;", "TabButton", "Landroidx/compose/foundation/layout/RowScope;", "selected", "TabButton-xwkQ0AY", "(Landroidx/compose/foundation/layout/RowScope;Ljava/lang/String;ZLkotlin/jvm/functions/Function0;J)V", "app_debug"})
public final class LoginScreenKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "LoginScreen";
    
    @kotlin.OptIn(markerClass = {androidx.compose.animation.ExperimentalAnimationApi.class})
    @androidx.compose.runtime.Composable()
    public static final void LoginScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onLoginSuccess) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final androidx.compose.material3.TextFieldColors textFieldColors() {
        return null;
    }
}