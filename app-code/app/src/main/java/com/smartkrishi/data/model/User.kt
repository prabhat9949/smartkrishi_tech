package com.smartkrishi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = ""
) : Parcelable
