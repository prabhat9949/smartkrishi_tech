package com.smartkrishi.data.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) {

    var verificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    /** 🔥 Test Mode Numbers + OTP **/
    private val testNumbers = mapOf(
        "+919876543210" to "123456",
        "+919999888888" to "654321"
    )

    // ============================================================
    // 📨 SEND OTP
    // ============================================================
    fun sendOtp(
        phone: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {

        // ⭐ TEST MODE → skip Firebase billing
        if (testNumbers.containsKey(phone)) {
            verificationId = "TEST_VERIFICATION_ID"
            callbacks.onCodeSent(
                "TEST_VERIFICATION_ID",
                PhoneAuthProvider.ForceResendingToken.zza() // fallback for test
            )
            return
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ============================================================
    // 🔁 RESEND OTP
    // ============================================================
    fun resendOtp(
        phone: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {

        if (testNumbers.containsKey(phone)) {
            verificationId = "TEST_VERIFICATION_ID"
            callbacks.onCodeSent(
                "TEST_VERIFICATION_ID",
                PhoneAuthProvider.ForceResendingToken.zza()
            )
            return
        }

        val token = resendToken ?: return sendOtp(phone, activity, callbacks)

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ============================================================
    // 🔐 GOOGLE LOGIN
    // ============================================================
    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google Login Error: ${e.message}")
            null
        }
    }

    // ============================================================
    // 🔐 VERIFY OTP (Supports Test Mode Also)
    // ============================================================
    suspend fun verifyOtp(phone: String, code: String): FirebaseUser? {

        // ⭐ TEST OTP
        if (testNumbers[phone] == code) {
            val credential = PhoneAuthProvider.getCredential("TEST_VERIFICATION_ID", code)
            return signInWithCredential(credential)
        }

        // ⭐ REAL OTP
        val id = verificationId ?: return null
        val credential = PhoneAuthProvider.getCredential(id, code)
        return signInWithCredential(credential)
    }

    // ============================================================
    // 🚪 SIGN-IN USING CREDENTIAL
    // ============================================================
    suspend fun signInWithCredential(credential: PhoneAuthCredential): FirebaseUser? {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign-in Failed: ${e.message}")
            null
        }
    }

    // ============================================================
    // 💾 SAVE USER DATA (Only for NEW users)
    // ============================================================
    suspend fun saveUserData(uid: String, phone: String, role: String) {
        val userData = mapOf(
            "uid" to uid,
            "phone" to phone,
            "role" to role,
            "name" to "",
            "photo" to "",
            "createdAt" to System.currentTimeMillis()
        )
        database.getReference("users").child(uid).setValue(userData).await()
    }

    // ============================================================
    // 📌 CHECK IF USER PROFILE EXISTS (For Google / OTP)
    // ============================================================
    fun getUserData(uid: String, callback: (Map<String, Any>?) -> Unit) {
        database.getReference("users").child(uid)
            .get()
            .addOnSuccessListener { snap ->
                callback(if (snap.exists()) snap.value as? Map<String, Any> else null)
            }
            .addOnFailureListener { callback(null) }
    }
}
