package z.taxi.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.support.DaggerAppCompatActivity
import z.taxi.R
import z.taxi.manager.LocationManager
import javax.inject.Inject

class MapActivity : DaggerAppCompatActivity(), OnMapReadyCallback {

    companion object {
        private val TAG = MapActivity::class.java.simpleName
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
        setResult(RESULT_OK)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener {
            locationManager.lastKnownLocation = it
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(it))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }
}