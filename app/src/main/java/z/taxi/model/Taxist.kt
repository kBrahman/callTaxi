package z.taxi.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class Taxist(var name: String?, var phone: String) : Parcelable {

    var lat: Double? = null
    var lon: Double? = null

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString().toString()) {
        lat = parcel.readValue(Double::class.java.classLoader) as? Double
        lon = parcel.readValue(Double::class.java.classLoader) as? Double
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