package zig.zak.taxor.model

import android.support.annotation.Keep

@Keep
class Taxist(val name: String?, val phone: String?) {

    var lat: Double? = null
    var lon: Double? = null

    override fun toString(): String {
        return "Taxist(name=$name, phone=$phone, lat=$lat, lon=$lon)"
    }
}