package zig.zak.taxor.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zig.zak.taxor.R
import zig.zak.taxor.adapter.TaxiInfoWindowAdapter
import zig.zak.taxor.manager.ApiManager
import zig.zak.taxor.manager.LocationManager
import zig.zak.taxor.model.Taxist


class TaxMapFragment : SupportMapFragment() {

    companion object {
        private val TAG: String = TaxMapFragment::class.java.simpleName
    }

    private var map: GoogleMap? = null
    private var zoomLvl: Float? = 0f
    private var selectedMarker: Marker? = null

    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        Log.i(TAG, "onCreate")
        setHasOptionsMenu(true)
        getMapAsync { listTaxists(it) }
    }

//    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        inflater?.inflate(R.menu.menu_pass, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        this@TaxMapFragment.zoomLvl = map?.cameraPosition?.zoom
//        map?.clear()
//        getMapAsync { listTaxists(it) }
//        return true
//    }

    private fun listTaxists(map: GoogleMap) {
        LocationManager(activity!!).getMyCoords(OnSuccessListener {
            getTaxists(it.latitude, it.longitude, map)
        })
    }

    private fun getTaxists(latitude: Double, longitude: Double, map: GoogleMap) {
        ApiManager().get(latitude, longitude, object : Callback<List<Taxist>> {
            override fun onFailure(call: Call<List<Taxist>>, t: Throwable) {
                if (t.message.toString().contains("Failed to connect to")) {
                    Toast.makeText(activity, R.string.maintenance, Toast.LENGTH_LONG).show()
                    activity?.finish()
                }
            }

            override fun onResponse(call: Call<List<Taxist>>, response: Response<List<Taxist>>) {
                Log.i(TAG, "taxists=>${response.body()}")
                val options = MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.map_car))
                map.setInfoWindowAdapter(TaxiInfoWindowAdapter(context))
                response.body()?.forEach {
                    val latLng = LatLng(it.lat!!, it.lon!!)
                    map.addMarker(options.position(latLng)
                            .title(it.name)
                            .snippet(it.phone))
                }
                val latLng = LatLng(latitude, longitude)
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
                this@TaxMapFragment.map = map
            }
        })
    }
}