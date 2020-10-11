package zig.zak.taxi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import zig.zak.taxi.R

class TaxiInfoWindowAdapter(private val context: Context?) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker?) = null

    override fun getInfoWindow(marker: Marker?): View {
        val v = LayoutInflater.from(context).inflate(R.layout.marker_window, null, false)
        val txtView = v.findViewById<TextView>(R.id.marker_name)
        txtView.text = marker?.title
        v.findViewById<TextView>(R.id.marker_phone).text = marker?.snippet
        return v
    }

}
