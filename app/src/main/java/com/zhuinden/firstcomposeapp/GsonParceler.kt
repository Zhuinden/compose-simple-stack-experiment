package com.zhuinden.firstcomposeapp

import android.os.Parcelable
import com.google.gson.Gson
import com.zhuinden.simplestack.KeyParceler
import kotlinx.android.parcel.Parcelize

class GsonParceler: KeyParceler {
    @Parcelize
    class Wrapper(val serializedKey: String, val fullyQualifiedName: String):
        Parcelable

    private val gson = Gson()

    override fun fromParcelable(parcelable: Parcelable): Any {
        return gson.fromJson((parcelable as Wrapper).serializedKey, Class.forName(parcelable.fullyQualifiedName))
    }

    override fun toParcelable(key: Any): Parcelable {
        return Wrapper(gson.toJson(key), key.javaClass.name)
    }
}