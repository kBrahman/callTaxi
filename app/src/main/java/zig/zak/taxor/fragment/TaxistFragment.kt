package zig.zak.taxor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.*
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.fragment_list_taxists.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zig.zak.taxor.R
import zig.zak.taxor.adapter.TaxistAdapter
import zig.zak.taxor.manager.ApiManager
import zig.zak.taxor.manager.LocationManager
import zig.zak.taxor.model.Taxist

class TaxistFragment : Fragment() {
    companion object {
        private var TAG = TaxistFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater
            .inflate(R.layout.fragment_list_taxists, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated")
        listTaxists()
        rv.setHasFixedSize(true)
    }

//    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        inflater?.inflate(R.menu.menu_pass, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        passengerPrgrBar.visibility = View.VISIBLE
//        listTaxists()
//        return true
//    }

    private fun listTaxists() {
        LocationManager(activity!!).getMyCoords(OnSuccessListener {
            getTaxists(it.latitude, it.longitude)
        })
    }

    private fun getTaxists(latitude: Double, longitude: Double) {
        ApiManager().get(latitude, longitude, object : Callback<List<Taxist>> {
            override fun onFailure(call: Call<List<Taxist>>, t: Throwable) {
                if (t.message.toString().contains("Failed to connect to")) {
                    Toast.makeText(activity, R.string.maintenance, Toast.LENGTH_LONG).show()
                    activity?.finish()
                }
            }

            override fun onResponse(call: Call<List<Taxist>>, response: Response<List<Taxist>>) {
                Log.i(TAG, "taxists=>${response.body()}")
                passengerPrgrBar.visibility = View.GONE
                rv.adapter = response.body()?.let { TaxistAdapter(it, latitude, longitude) }
            }
        })
    }
}