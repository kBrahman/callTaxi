package zig.zak.taxor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_list_taxists.*
import zig.zak.taxor.R
import zig.zak.taxor.adapter.TaxistAdapter
import zig.zak.taxor.model.Taxist

class TaxisFragment : DaggerFragment() {
    companion object {
        private var TAG = TaxisFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater
            .inflate(R.layout.fragment_list_taxists, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv.setHasFixedSize(true)
        val list = arguments?.getParcelableArrayList<Taxist>("data")
        val lat = arguments?.getDouble("lat")
        val lon = arguments?.getDouble("lon")
        rv.adapter = TaxistAdapter(list, lat, lon)
    }

    fun updateList() {
        val list = arguments?.getParcelableArrayList<Taxist>("data")
        val lat = arguments?.getDouble("lat")
        val lon = arguments?.getDouble("lon")
        val taxistAdapter = rv.adapter as TaxistAdapter
        taxistAdapter.list = list
        taxistAdapter.latitude = lat
        taxistAdapter.longitude = lon
        taxistAdapter.notifyDataSetChanged()
    }
}