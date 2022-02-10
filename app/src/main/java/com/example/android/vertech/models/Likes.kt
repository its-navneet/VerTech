package com.example.android.vertech.models

import com.google.firebase.database.DatabaseReference

class Likes(
    val sendername: String?,
    val senderpic: String?
) {
    constructor() : this("","")
}