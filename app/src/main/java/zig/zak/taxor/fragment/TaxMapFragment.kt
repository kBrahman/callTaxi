package zig.zak.taxor.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import zig.zak.taxor.R
import zig.zak.taxor.adapter.TaxiInfoWindowAdapter
import zig.zak.taxor.model.Taxist
import javax.inject.Inject


class TaxMapFragment : SupportMapFragment(), HasAndroidInjector {

    companion object {
        private val TAG: String = TaxMapFragment::class.java.simpleName
    }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>
    private var zoomLvl: Float? = 0f
    private var selectedMarker: Marker? = null
    lateinit var map: GoogleMap
    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        setHasOptionsMenu(true)
        Log.i(TAG, "on create")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        getMapAsync { listTaxists(it) }
        super.onAttach(context)
    }

    override fun androidInjector(): AndroidInjector<Any?>? {
        return androidInjector
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "on view created")
        super.onViewCreated(view, savedInstanceState)
    }

    private fun listTaxists(map: GoogleMap) {
        Log.i(TAG, "map is ready")
        val list = arguments?.getParcelableArrayList<Taxist>("data")
        Log.i(TAG, "list=>$list")
        val lat = arguments?.getDouble("lat")
        val lon = arguments?.getDouble("lon")
        Log.i(TAG, "lat=>$lat")
        Log.i(TAG, "lon=>$lon")
        val options = MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.drawable.map_car))
        map.clear()
        map.setInfoWindowAdapter(TaxiInfoWindowAdapter(context))
        list?.forEach {
            val latLng = LatLng(it.lat!!, it.lon!!)
            map.addMarker(options.position(latLng)
                    .title(it.name)
                    .snippet(it.phone))
        }
        val latLng = LatLng(lat!!, lon!!)
        map.addMarker(MarkerOptions().position(latLng))
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        map.animateCamera(CameraUpdateFactory.zoomTo(if (zoomLvl == 0f) 13f else zoomLvl!!))
        map.setOnInfoWindowClickListener {
            val intent = Intent(Intent.ACTION_DIAL);
            intent.data = Uri.parse("tel:${it.snippet}")
            startActivity(intent)
        }
        map.setOnMarkerClickListener {
            if (it == selectedMarker) {
                it.hideInfoWindow()
                selectedMarker = null
            } else {
                it.showInfoWindow()
                this@TaxMapFragment.selectedMarker = it
            }
            true
        }
        this.map = map
    }

    fun updateMap() {
        listTaxists(map)
    }

}