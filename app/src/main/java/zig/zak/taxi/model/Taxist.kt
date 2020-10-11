package zig.zak.taxi.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
class Taxist(val name: String?, val phone: String?) : Parcelable {

    var lat: Double? = null
    var lon: Double? = null

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
        lat = parcel.readValue(Double::class.java.classLoader) as? Double
        lon = parcel.readValue(Double::class.java.classLoader) as? Double
    }

    override fun toString(): String {
        return "Taxist(name=$name, phone=$phone, lat=$lat, lon=$lon)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeValue(lat)
        parcel.writeValue(lon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Taxist> {
        override fun createFromParcel(parcel: Parcel): Taxist {
            return Taxist(parcel)
        }

        override fun newArray(size: Int): Array<Taxist?> {
            return arrayOfNulls(size)
        }
    }
}