package zig.zak.taxor.activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.support.DaggerAppCompatActivity
import zig.zak.taxor.R
import zig.zak.taxor.manager.LocationManager
import javax.inject.Inject

class MapsActivity : DaggerAppCompatActivity(), OnMapReadyCallback {

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
    }

    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun setLocation(item: MenuItem) {
//        val intent = Intent()
//        val bundle = Bundle()
//        bundle.putParcelable("location", location)
//        intent.putExtras(bundle)
        setResult(RESULT_OK)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener {
            locationManager.lastKnownLocation = it
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(it))
        }
        // Add a marker in Sydney and move the camera
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onDestroy() {
        Log.i(TAG, "Loc=>${locationManager.lastKnownLocation}")
        Log.i(TAG, "instance=>$locationManager")
        super.onDestroy()
    }
}