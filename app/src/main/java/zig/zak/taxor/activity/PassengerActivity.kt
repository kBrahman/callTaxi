package zig.zak.taxor.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore.MediaColumns.TITLE
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import kotlinx.android.synthetic.main.passenger.*
import zig.zak.taxor.R
import zig.zak.taxor.fragment.TaxMapFragment
import zig.zak.taxor.fragment.TaxistFragment
import zig.zak.taxor.util.REQUEST_CODE_LOCATION_PERMISSION
import zig.zak.taxor.util.permissionsOk

class PassengerActivity : AppCompatActivity() {
    companion object {
        private val TAG: String = PassengerActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        setTitle(R.string.taxists)
        init()
    }

    private fun init() {
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionsOk(this, permissions)) {
            setContentView(R.layout.passenger)
            tab.setupWithViewPager(pager)
            val taxistFragment = TaxistFragment()
            var bundle = Bundle()
            bundle.putString(TITLE, getString(R.string.closest))
            taxistFragment.arguments = bundle
            val mapFragment = TaxMapFragment()
            bundle = Bundle()
            bundle.putString(TITLE, getString(R.string.on_map))
            mapFragment.arguments = bundle
            pager.adapter = Adapter(listOf(taxistFragment, mapFragment), supportFragmentManager)

        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            init()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    inner class Adapter(private val fragments: List<Fragment>, fragmentManager: android.support.v4.app.FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int) = fragments[position].arguments?.getString(TITLE)
    }
}
