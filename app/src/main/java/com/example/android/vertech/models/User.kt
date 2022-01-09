package com.example.android.vertech.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        val uid: String?,
        val email: String?,
        val name: String?,
        val profileImageUrl: String?,
        val graduation: String?,
        val domain: String?,
        val bio: String?

) : Parcelable {
        constructor() : this("", "","", "", "","", "")
}