package com.zhuinden.firstcomposeapp

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.Composable

// @Parcelize // java.lang.AbstractMethodError: abstract method "void android.os.Parcelable.writeToParcel(android.os.Parcel, int)"
data class FirstKey(val name: String) : ComposeKey() {
    constructor(parcel: Parcel) : this(parcel.readString()!!)

    @Composable
    override fun realComposable() {
        Greeting(name = name)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR :
        Parcelable.Creator<FirstKey> {
        override fun createFromParcel(parcel: Parcel): FirstKey {
            return FirstKey(parcel)
        }

        override fun newArray(size: Int): Array<FirstKey?> {
            return arrayOfNulls(size)
        }
    }
}