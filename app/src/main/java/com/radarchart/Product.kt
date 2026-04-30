package com.radarchart

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val brand: String,
    val series: String,
    val name: String,
    val energy: Int,
    val grade: Int,
    val corrosion: Int,
    val lowTemp: Int,
    val highTemp: Int,
    val pressure: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(brand)
        parcel.writeString(series)
        parcel.writeString(name)
        parcel.writeInt(energy)
        parcel.writeInt(grade)
        parcel.writeInt(corrosion)
        parcel.writeInt(lowTemp)
        parcel.writeInt(highTemp)
        parcel.writeInt(pressure)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product = Product(parcel)
        override fun newArray(size: Int): Array<Product?> = arrayOfNulls(size)
    }
}
