package com.smartkrishi.presentation.auth

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.smartkrishi.utils.SessionManager

private const val TAG = "LoginScreen"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (email: String) -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        Log.d(TAG, "LoginScreen loaded")
        Log.d(TAG, "Current user: ${auth.currentUser?.email}")
        Log.d(TAG, "Session logged in: ${SessionManager.isLoggedIn(context)}")
    }

    // State Management
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validation States
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val primaryGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF66BB6A)
    val veryLightGreen = Color(0xFFE8F5E9)
    val backgroundWhiteGreen = Color(0xFFF1F8E9)

    // Validation Functions
    fun validateEmail(email: String): Boolean {
        emailError = when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Invalid email format"
            else -> null
        }
        return emailError == null
    }

    fun validatePassword(password: String): Boolean {
        passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            !password.any { it.isLetter() } -> "Password must contain at least one letter"
            else -> null
        }
        return passwordError == null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
        return confirmPasswordError == null
    }

    fun validatePhoneNumber(phone: String): Boolean {
        phoneError = when {
            phone.isBlank() -> "Phone number is required"
            phone.length != 10 -> "Phone number must be 10 digits"
            !phone.all { it.isDigit() } -> "Phone number must contain only digits"
            else -> null
        }
        return phoneError == null
    }

    // Google Sign-In Setup
    val googleClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("511480315742-81o6mo2rg6d92kfd0shfjkg75cn4bms6.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            loading = true
            Log.d(TAG, "Google sign-in: Authenticating with Firebase")

            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    loading = false
                    val userEmail = authResult.user?.email
                    Log.d(TAG, "Google sign-in successful: $userEmail")

                    if (!userEmail.isNullOrBlank()) {
                        SessionManager.setLogin(context, true)
                        SessionManager.setUserEmail(context, userEmail)
                        Log.d(TAG, "✅ Email saved to SessionManager: $userEmail")
                        onLoginSuccess(userEmail)
                    } else {
                        Toast.makeText(
                            context,
                            "Email not found from Google account",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    loading = false
                    Log.e(TAG, "Google sign-in failed: ${exception.message}", exception)
                    Toast.makeText(
                        context,
                        "Google sign-in failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } catch (e: Exception) {
            loading = false
            Log.e(TAG, "Google sign-in error: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Login Function
    fun performLogin() {
        Log.d(TAG, "Attempting login for: ${email.trim()}")

        if (!validateEmail(email) || !validatePassword(password)) {
            Log.w(TAG, "Login validation failed")
            return
        }

        loading = true
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->
                loading = false
                val userEmail = authResult.user?.email
                Log.d(TAG, "Login successful: $userEmail")

                if (!userEmail.isNullOrBlank()) {
                    SessionManager.setLogin(context, true)
                    SessionManager.setUserEmail(context, userEmail)
                    Log.d(TAG, "✅ Email saved to SessionManager: $userEmail")
                    onLoginSuccess(userEmail)
                } else {
                    Log.e(TAG, "Login succeeded but email is null")
                    Toast.makeText(
                        context,
                        "Login succeeded but email is missing",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                loading = false
                Log.e(TAG, "Login failed: ${exception.message}", exception)
                Toast.makeText(
                    context,
                    "Login failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // Signup Function
    fun performSignup() {
        Log.d(TAG, "Attempting signup for: ${email.trim()}")

        if (!validateEmail(email) ||
            !validatePassword(password) ||
            !validateConfirmPassword(password, confirmPassword) ||
            !validatePhoneNumber(phoneNumber)
        ) {
            Log.w(TAG, "Signup validation failed")
            return
        }

        loading = true
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "Account created: ${authResult.user?.email}")

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(phoneNumber)
                    .build()

                authResult.user?.updateProfile(profileUpdates)
                    ?.addOnSuccessListener {
                        Log.d(TAG, "Profile updated with phone: $phoneNumber")

                        authResult.user?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                loading = false
                                Log.d(TAG, "Verification email sent")
                                SessionManager.setLogin(context, true)
                                SessionManager.setUserEmail(context, email.trim())
                                Log.d(TAG, "✅ Email saved to SessionManager: ${email.trim()}")
                                Toast.makeText(
                                    context,
                                    "Account created! Verification email sent.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoginSuccess(email.trim())
                            }
                            ?.addOnFailureListener { exception ->
                                loading = false
                                Log.e(TAG, "Verification email failed: ${exception.message}", exception)
                                SessionManager.setLogin(context, true)
                                SessionManager.setUserEmail(context, email.trim())
                                Log.d(TAG, "✅ Email saved to SessionManager: ${email.trim()}")
                                Toast.makeText(
                                    context,
                                    "Account created but email verification failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoginSuccess(email.trim())
                            }
                    }
                    ?.addOnFailureListener { exception ->
                        loading = false
                        Log.e(TAG, "Profile update failed: ${exception.message}", exception)
                        SessionManager.setLogin(context, true)
                        SessionManager.setUserEmail(context, email.trim())
                        Log.d(TAG, "✅ Email saved to SessionManager: ${email.trim()}")
                        Toast.makeText(
                            context,
                            "Account created but profile update failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        onLoginSuccess(email.trim())
                    }
            }
            .addOnFailureListener { exception ->
                loading = false
                Log.e(TAG, "Signup failed: ${exception.message}", exception)
                Toast.makeText(
                    context,
                    "Signup failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // UI - FULL SCREEN STRETCH WITH SCROLLABLE CONTENT
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White,
                        backgroundWhiteGreen.copy(0.4f),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(20.dp))

            // FULL WIDTH CARD WITH NO HORIZONTAL PADDING
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Icon with Green Gradient
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(primaryGreen, lightGreen)
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Smart Krishi",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryGreen,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = if (isLoginMode) "Welcome Back!" else "Create Account",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(24.dp))

                    // Toggle Login/Signup Tabs - FULL WIDTH
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                veryLightGreen,
                                RoundedCornerShape(30.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabButton(
                            text = "Login",
                            selected = isLoginMode,
                            onClick = {
                                isLoginMode = true
                                Log.d(TAG, "Switched to Login mode")
                                emailError = null
                                passwordError = null
                                confirmPasswordError = null
                                phoneError = null
                            },
                            primaryColor = primaryGreen
                        )

                        TabButton(
                            text = "Sign Up",
                            selected = !isLoginMode,
                            onClick = {
                                isLoginMode = false
                                Log.d(TAG, "Switched to Signup mode")
                                emailError = null
                                passwordError = null
                                confirmPasswordError = null
                                phoneError = null
                            },
                            primaryColor = primaryGreen
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Animated Content - FULL WIDTH FORMS
                    AnimatedContent(
                        targetState = isLoginMode,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with
                                    fadeOut(animationSpec = tween(300))
                        },
                        label = "auth_content"
                    ) { loginMode ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (loginMode) {
                                // LOGIN FORM - FULL WIDTH
                                AuthTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it.trim()
                                        emailError = null
                                    },
                                    label = "Email Address",
                                    placeholder = "Enter your email",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    errorMessage = emailError
                                )

                                Spacer(Modifier.height(14.dp))

                                PasswordTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordError = null
                                    },
                                    label = "Password",
                                    placeholder = "Enter your password",
                                    passwordVisible = passwordVisible,
                                    onVisibilityToggle = { passwordVisible = !passwordVisible },
                                    imeAction = ImeAction.Done,
                                    onDone = { performLogin() },
                                    errorMessage = passwordError
                                )

                                Spacer(Modifier.height(4.dp))

                                // Forgot Password
                                TextButton(
                                    onClick = {
                                        if (email.isNotBlank() && validateEmail(email)) {
                                            Log.d(TAG, "Sending password reset email to: $email")
                                            auth.sendPasswordResetEmail(email.trim())
                                                .addOnSuccessListener {
                                                    Log.d(TAG, "Password reset email sent")
                                                    Toast.makeText(
                                                        context,
                                                        "Password reset email sent to $email",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e(TAG, "Password reset failed: ${exception.message}", exception)
                                                    Toast.makeText(
                                                        context,
                                                        "Error: ${exception.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Please enter a valid email address",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        "Forgot Password?",
                                        color = primaryGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // Login Button - FULL WIDTH
                                PrimaryButton(
                                    text = "Login",
                                    onClick = { performLogin() },
                                    loading = loading,
                                    primaryColor = primaryGreen
                                )

                            } else {
                                // SIGNUP FORM - FULL WIDTH
                                AuthTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it.trim()
                                        emailError = null
                                    },
                                    label = "Email Address",
                                    placeholder = "your.email@example.com",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    errorMessage = emailError
                                )

                                Spacer(Modifier.height(14.dp))

                                AuthTextField(
                                    value = phoneNumber,
                                    onValueChange = {
                                        if (it.length <= 10) {
                                            phoneNumber = it.filter { char -> char.isDigit() }
                                            phoneError = null
                                        }
                                    },
                                    label = "Phone Number",
                                    placeholder = "10-digit mobile number",
                                    leadingIcon = Icons.Default.Phone,
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    errorMessage = phoneError,
                                    prefix = "+91 "
                                )

                                Spacer(Modifier.height(14.dp))

                                PasswordTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordError = null
                                    },
                                    label = "Password",
                                    placeholder = "Min 6 chars with letter & number",
                                    passwordVisible = passwordVisible,
                                    onVisibilityToggle = { passwordVisible = !passwordVisible },
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    errorMessage = passwordError
                                )

                                Spacer(Modifier.height(14.dp))

                                PasswordTextField(
                                    value = confirmPassword,
                                    onValueChange = {
                                        confirmPassword = it
                                        confirmPasswordError = null
                                    },
                                    label = "Confirm Password",
                                    placeholder = "Re-enter your password",
                                    passwordVisible = confirmPasswordVisible,
                                    onVisibilityToggle = {
                                        confirmPasswordVisible = !confirmPasswordVisible
                                    },
                                    imeAction = ImeAction.Done,
                                    onDone = { performSignup() },
                                    errorMessage = confirmPasswordError
                                )

                                Spacer(Modifier.height(20.dp))

                                // Signup Button - FULL WIDTH
                                PrimaryButton(
                                    text = "Create Account",
                                    onClick = { performSignup() },
                                    loading = loading,
                                    primaryColor = primaryGreen
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Divider - FULL WIDTH
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray
                        )
                        Text(
                            text = "  OR  ",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Google Sign-In Button - FULL WIDTH
                    SocialButton(
                        text = "Continue with Google",
                        icon = Icons.Default.AccountCircle,
                        onClick = {
                            Log.d(TAG, "Google sign-in button clicked")
                            googleLauncher.launch(googleClient.signInIntent)
                        },
                        enabled = !loading,
                        backgroundColor = veryLightGreen,
                        textColor = primaryGreen
                    )

                    Spacer(Modifier.height(12.dp))

                    // DigiLocker Button - FULL WIDTH
                    SocialButton(
                        text = "Continue with DigiLocker",
                        icon = Icons.Default.AccountBalance,
                        onClick = {
                            Toast.makeText(
                                context,
                                "DigiLocker integration coming soon!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = false,
                        backgroundColor = Color.LightGray.copy(0.3f),
                        textColor = Color.Gray
                    )

                    Spacer(Modifier.height(16.dp))

                    // Terms & Privacy
                    if (!isLoginMode) {
                        Text(
                            text = "By creating an account, you agree to our\nTerms of Service and Privacy Policy",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ============================================
// COMPOSABLE COMPONENTS
// ============================================

@Composable
private fun RowScope.TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(42.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) primaryColor else Color.Transparent,
            contentColor = if (selected) Color.White else Color.DarkGray
        ),
        elevation = if (selected) ButtonDefaults.buttonElevation(4.dp)
        else ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onNext: (() -> Unit)? = null,
    errorMessage: String? = null,
    prefix: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 14.sp) },
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color.Gray) },
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
            },
            prefix = prefix?.let { { Text(it, color = Color.Gray, fontSize = 14.sp) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() },
                onDone = { onNext?.invoke() }
            ),
            isError = errorMessage != null,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFD32F2F),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 14.dp, top = 3.dp)
            )
        }
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    imeAction: ImeAction = ImeAction.Done,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 14.sp) },
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color.Gray) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password"
                        else "Show password",
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() },
                onDone = { onDone?.invoke() }
            ),
            isError = errorMessage != null,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFD32F2F),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 14.dp, top = 3.dp)
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean,
    primaryColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
        enabled = !loading,
        elevation = ButtonDefaults.buttonElevation(6.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun SocialButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    backgroundColor: Color,
    textColor: Color
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = textColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) Color.LightGray else Color.LightGray.copy(0.3f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = textColor
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF2E7D32),
    unfocusedBorderColor = Color.LightGray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    cursorColor = Color(0xFF2E7D32),
    focusedContainerColor = Color(0xFFF1F8E9).copy(0.3f),
    unfocusedContainerColor = Color.White,
    focusedLabelColor = Color(0xFF2E7D32),
    unfocusedLabelColor = Color.Gray,
    errorBorderColor = Color(0xFFD32F2F),
    errorLabelColor = Color(0xFFD32F2F),
    errorTextColor = Color.Black,
    errorCursorColor = Color(0xFFD32F2F)
)
