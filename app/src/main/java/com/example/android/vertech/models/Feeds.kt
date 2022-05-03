package com.example.android.vertech.models


class Feeds(
    val senderid: String?,
    val sendername: String?,
    val imagecontent: String?,
    val textcontent: String?,
    val timestamp: Long?,
) {
    constructor() : this("", "", "", "", -1)
}