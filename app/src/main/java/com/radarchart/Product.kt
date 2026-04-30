package com.radarchart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val name: String,
    val brand: String,
    val group: String,
    val energy: Int,
    val grade: Int,
    val corrosion: Int,
    val lowTemp: Int,
    val highTemp: Int,
    val pressure: Int
) : Parcelable
