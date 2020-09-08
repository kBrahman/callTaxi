package zig.zak.taxor.activity

import android.os.Bundle
import android.provider.MediaStore.MediaColumns.TITLE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_passenger.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zig.zak.taxor.R
import zig.zak.taxor.fragment.TaxMapFragment
import zig.zak.taxor.fragment.TaxisFragment
import zig.zak.taxor.manager.ApiManager
import zig.zak.taxor.manager.LocationManager
import zig.zak.taxor.model.Taxist
import java.util.*
import javax.inject.Inject

class PassengerActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG: String = PassengerActivity::class.java.simpleName
    }

    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listTaxists()
        setTitle(R.string.taxists)
        init()
    }


    private fun init() {
        setContentView(R.layout.activity_passenger)
        tab.setupWithViewPager(pager)
    }

    private fun listTaxists() {
        locationManager.getMyCoords(OnSuccessListener {
            if (it == null) {
                getTaxists(locationManager.lastKnownLocation!!.latitude, locationManager.lastKnownLocation!!.longitude)
            } else {
                getTaxists(it.latitude, it.longitude)
                locationManager.lastKnownLocation = LatLng(it.latitude, it.longitude)
            }

        })
    }

    private fun getTaxists(latitude: Double, longitude: Double) {
        ApiManager().get(latitude, longitude, object : Callback<List<Taxist>> {
            override fun onFailure(call: Call<List<Taxist>>, t: Throwable) {
                if (t.message.toString().contains("Failed to connect to")) {
                    Toast.makeText(this@PassengerActivity, R.string.maintenance, Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            override fun onResponse(call: Call<List<Taxist>>, response: Response<List<Taxist>>) {
                Log.i(TAG, "taxists=>${response.body()}")
                passengerPrgrBar.visibility = View.GONE
                var bundle = Bundle()
                if (pager.adapter == null) {
                    val taxistFragment = TaxisFragment()
                    bundle.putString(TITLE, getString(R.string.closest))
                    bundle.putParcelableArrayList("data", response.body() as ArrayList<Taxist>)
                    bundle.putDouble("lat", latitude)
                    bundle.putDouble("lon", longitude)
                    taxistFragment.arguments = bundle
                    val mapFragment = TaxMapFragment()
                    bundle = Bundle()
                    bundle.putString(TITLE, getString(R.string.on_map))
                    bundle.putParcelableArrayList("data", response.body() as ArrayList<Taxist>)
                    bundle.putDouble("lat", latitude)
                    bundle.putDouble("lon", longitude)
                    mapFragment.arguments = bundle
                    pager.adapter = Adapter(listOf(taxistFragment, mapFragment), supportFragmentManager)
                } else {
                    bundle.putParcelableArrayList("data", response.body() as ArrayList<Taxist>)
                    bundle.putDouble("lat", latitude)
                    bundle.putDouble("lon", longitude)
                    for (fragment in (pager.adapter as Adapter).fragments) {
                        if (fragment is TaxisFragment) {
                            bundle.putString(TITLE, getString(R.string.closest))
                            fragment.arguments = bundle
                            fragment.updateList()
                        } else if (fragment is TaxMapFragment) {
                            bundle.putString(TITLE, getString(R.string.on_map))
                            fragment.arguments = bundle
                            fragment.updateMap()
                        }
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pass, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        passengerPrgrBar.visibility = VISIBLE
        listTaxists()
        return super.onOptionsItemSelected(item)
    }

    class Adapter(val fragments: List<Fragment>, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int) = fragments[position].arguments?.getString(TITLE)
    }
}
