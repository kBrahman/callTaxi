package zig.zak.taxor.adapter

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.taxist_item.view.*
import zig.zak.taxor.R
import zig.zak.taxor.model.Taxist
import java.util.*
import kotlin.math.roundToInt

class TaxistAdapter(var list: ArrayList<Taxist>?, var latitude: Double?, var longitude: Double?) : RecyclerView.Adapter<TaxistAdapter.VH>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) = VH(LayoutInflater.from(p0.context).inflate(R.layout.taxist_item, p0, false))

    override fun getItemCount() = list?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val taxist = list?.get(position)
        holder.itemView.name.text = taxist?.name
        holder.itemView.distance.text = getDistance(taxist?.lat, taxist?.lon)
        holder.phone = taxist?.phone.toString()
    }

    private fun getDistance(taxiLat: Double?, taxiLon: Double?): String {
        val taxiLoc = Location("")
        taxiLoc.latitude = taxiLat!!
        taxiLoc.longitude = taxiLon!!

        val loc = Location("")
        loc.latitude = latitude ?: 0.0
        loc.longitude = longitude ?: 0.0
        val d = taxiLoc.distanceTo(loc)
        return if (d < 10000) "${d.roundToInt()} m" else "%.1f km".format(d / 1000)
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        lateinit var phone: String

        init {
            view.phone.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$phone")
                it.context.startActivity(intent)
            }
        }
    }

}
