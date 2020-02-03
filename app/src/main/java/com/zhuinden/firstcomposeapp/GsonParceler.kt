package com.zhuinden.firstcomposeapp

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.zhuinden.simplestack.KeyParceler

class GsonParceler : KeyParceler {
    private val gson = Gson()

    override fun fromParcelable(parcelable: Parcelable): Any {
        return gson.fromJson(
            (parcelable as Wrapper).serializedKey,
            Class.forName(parcelable.fullyQualifiedName)
        )
    }

    override fun toParcelable(key: Any): Parcelable {
        return Wrapper(gson.toJson(key), key.javaClass.name)
    }

    class Wrapper(val serializedKey: String, val fullyQualifiedName: String) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(serializedKey)
            parcel.writeString(fullyQualifiedName)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Wrapper> {
            override fun createFromParcel(parcel: Parcel): Wrapper = Wrapper(parcel)
            override fun newArray(size: Int): Array<Wrapper?> = arrayOfNulls(size)
        }
    }
}