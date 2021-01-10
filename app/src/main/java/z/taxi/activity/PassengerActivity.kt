package z.taxi.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import dagger.android.support.DaggerAppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import z.taxi.BuildConfig
import z.taxi.R
import z.taxi.manager.ApiManager
import z.taxi.manager.LocationManager
import z.taxi.model.Taxist
import z.taxi.util.PERMISSION_REQUEST
import z.taxi.util.permissionsOk
import z.taxi.webrtc.CallActivity
import z.taxi.webrtc.util.Util
import java.util.*
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt


class PassengerActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG: String = PassengerActivity::class.java.simpleName
    }

    private lateinit var taxists: List<Taxist>
    private var mapView: MapView? = null
    private lateinit var phoneToCall: String
    private var zoomLvl: Float = 0f
    private var selectedMarker: Marker? = null
    private lateinit var dataFetched: MutableState<Boolean>
    private lateinit var showDialog: MutableState<Boolean>
    private var map: GoogleMap? = null

    @Inject
    lateinit var locationManager: LocationManager

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.taxists)
        listTaxists()
        mapView = MapView(this@PassengerActivity)
        mapView!!.onCreate(null)
        val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
        setContent {
            Column(Modifier.fillMaxHeight()) {
                val selectedTab = remember { mutableStateOf(0) }
                dataFetched = remember { mutableStateOf(false) }
                showDialog = remember { mutableStateOf(false) }
                val swipeableState = rememberSwipeableState("C")
                val colorPrimary = Color(getColor(R.color.colorPrimary))
                TabRow(selectedTabIndex = selectedTab.value, backgroundColor = colorPrimary,
                        contentColor = Color.Yellow) {
                    Tab(selected = false, onClick = {
                        swipeableState.snapTo("C")
                    }, text = { Text(text = getString(R.string.closest)) })
                    Tab(selected = false, onClick = { swipeableState.snapTo("A") },
                            text = { Text(text = getString(R.string.on_map)) })
                }
                if (dataFetched.value) {
                    val anchors = mapOf(0f to "A", width.toFloat() to "C")
                    Box {
                        LazyColumn(Modifier
                                .fillMaxHeight()
                                .swipeable(
                                        state = swipeableState,
                                        anchors = anchors,
                                        thresholds = { _, _ -> FractionalThreshold(0.5f) },
                                        orientation = Orientation.Horizontal
                                )
                                .offset {
                                    val v = swipeableState.offset.value.roundToInt()
                                    if (v == 0) {
                                        selectedTab.value = 1
                                    } else if (v == width) {
                                        selectedTab.value = 0
                                    }
                                    IntOffset(-width + v, 0)
                                }
                        ) {
                            items(items = taxists) {
                                Log.i(TAG, "t")
                                Row(Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = it.name.toString(), fontSize = 20.sp)
                                    Spacer(Modifier.weight(1F))
                                    Text(text = getDistance(it.lat, it.lon))
                                    Spacer(Modifier.width(16.dp))
                                    Button(onClick = { videoCall(it.phone) }, colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                                            modifier = Modifier.preferredWidth(42.dp), shape = RoundedCornerShape(16)) {
                                        Icon(imageVector = vectorResource(id = R.drawable.ic_call_video_24), tint = Color.Yellow,
                                                modifier = Modifier.width(36.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    Button(onClick = { standardCall(it.phone) }, colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                                            modifier = Modifier.preferredWidth(42.dp), shape = RoundedCornerShape(16)) {
                                        Icon(imageVector = vectorResource(id = R.drawable.ic_local_phone_24dp), tint = Color.Yellow,
                                                modifier = Modifier.width(36.dp))
                                    }
                                }
                            }
                        }
                        Column(Modifier
                                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }) {
                            AndroidView(viewBlock = {
                                mapView!!.getMapAsync { map ->
                                    arrangeTaxists(map, taxists)
                                }
                                mapView!!
                            })
                        }
                        if (showDialog.value) {
                            Dialog(onDismissRequest = { showDialog.value = false }) {
                                Column(Modifier
                                        .clip(RoundedCornerShape(15))
                                        .background(Color.White)
                                        .padding(all = 4.dp)) {
                                    Text(text = selectedMarker?.title.toString(), fontSize = 30.sp, modifier = Modifier.padding(bottom = 8.dp))
                                    Row(Modifier.align(Alignment.CenterHorizontally)) {
                                        Button(onClick = { standardCall(selectedMarker?.snippet) }, colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                                                shape = CircleShape, modifier = Modifier.preferredWidth(50.dp)) {
                                            Icon(imageVector = vectorResource(id = R.drawable.ic_local_phone_24dp), tint = Color.Yellow)
                                        }
                                        Spacer(modifier = Modifier.preferredWidth(16.dp))
                                        Button(onClick = { videoCall(selectedMarker?.snippet.toString()) }, colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                                                shape = CircleShape, modifier = Modifier.preferredWidth(50.dp)) {
                                            Icon(imageVector = vectorResource(id = R.drawable.ic_call_video_24), tint = Color.Yellow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = colorPrimary)
                    }
                }
            }
        }

    }

    override fun onStart() {
        mapView!!.onStart()
        super.onStart()
    }

    private fun listTaxists() {
        locationManager.getMyCoords {
            if (it == null) {
                getTaxists(locationManager.lastKnownLocation!!.latitude, locationManager.lastKnownLocation!!.longitude)
            } else {
                getTaxists(it.latitude, it.longitude)
                locationManager.lastKnownLocation = LatLng(it.latitude, it.longitude)
            }

        }
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
                taxists = response.body() ?: emptyList()
                dataFetched.value = true
            }
        })
    }

    private fun arrangeTaxists(map: GoogleMap, list: List<Taxist>) {
        val lat = locationManager.lastKnownLocation?.latitude
        val lon = locationManager.lastKnownLocation?.longitude
        val options = MarkerOptions().icon(BitmapDescriptorFactory
                .fromBitmap(getBitmapFromVectorDrawable(R.mipmap.ic_launcher_round)))
        map.clear()
        val myLatLng = LatLng(lat!!, lon!!)
        list.forEach {
            val latLng = LatLng(it.lat!!, it.lon!!)
            map.addMarker(options.position(latLng)).apply {
                title = it.name
                snippet = it.phone
            }
        }
        map.addMarker(MarkerOptions().position(myLatLng))
        map.moveCamera(CameraUpdateFactory.newLatLng(myLatLng))
        map.animateCamera(CameraUpdateFactory.zoomTo(if (zoomLvl == 0f) 13f else zoomLvl))
        map.setOnMarkerClickListener {
            if (it.position != myLatLng) {
                selectedMarker = it
                showDialog.value = true
            }
            true
        }
        if (list.isNotEmpty()) {
            val taxist = list.first()
            map.animateCamera(CameraUpdateFactory.zoomTo(getZoom(taxist)))
        }
        this.map = map
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        val bitmap = Bitmap.createBitmap(96,
                96, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, 96, 96)
        drawable?.draw(canvas)
        return bitmap
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(phoneToCall)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getZoom(taxist: Taxist): Float {
        val metrics: DisplayMetrics = resources.displayMetrics
        val mapWidth: Float = (mapView?.width ?: 0) / metrics.density
        val equator = 40075004
        val latitudinalAdjustment = cos(Math.PI * taxist.lat!! / 180.0)
        val arg: Double = equator * mapWidth * latitudinalAdjustment / (distanceInMeters(taxist.lat, taxist.lon) * 256.0)
        return (ln(arg) * .97 / ln(2.0)).toFloat()
    }

    private fun distanceInMeters(lat: Double?, lon: Double?): Float {
        val taxiLoc = Location("")
        taxiLoc.latitude = lat!!
        taxiLoc.longitude = lon!!
        val loc = Location("")
        loc.latitude = locationManager.lastKnownLocation?.latitude ?: 0.0
        loc.longitude = locationManager.lastKnownLocation?.longitude ?: 0.0
        return taxiLoc.distanceTo(loc)
    }

    private fun getDistance(taxiLat: Double?, taxiLon: Double?): String {
        val d = distanceInMeters(taxiLat, taxiLon)
        return if (d < 10000) "${d.roundToInt()} m" else "%.1f km".format(d / 1000)
    }

    private fun videoCall(phone: String) {
        if (!permissionsOk(this, CallActivity.MANDATORY_PERMISSIONS)) {
            phoneToCall = phone
            requestPermissions(CallActivity.MANDATORY_PERMISSIONS, PERMISSION_REQUEST)
        } else {
            onPermissionsGranted(phone)
        }
    }

    private fun onPermissionsGranted(phone: String) {
        val intent = Intent(this, CallActivity::class.java)
        intent.data = Uri.parse(BuildConfig.SERVER)
        intent.putExtra(CallActivity.EXTRA_ID_REMOTE, phone)
        intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, true)
        intent.putExtra(CallActivity.EXTRA_VIDEO_CODEC, "VP8")
        intent.putExtra(CallActivity.EXTRA_INITIATOR, true)
        intent.putExtra(Util.EXTRA_ID_LOCAL, generateId())
        startActivity(intent)
    }

    private fun generateId() = String.format("%06d", Random().nextInt(999999))

    private fun standardCall(phone: String?) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(getString(R.string.refresh))?.apply {
            icon = ContextCompat.getDrawable(this@PassengerActivity, R.drawable.ic_refresh_black_24dp)
            setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
            setOnMenuItemClickListener {
                dataFetched.value = false
                listTaxists()
                zoomLvl = map?.cameraPosition?.zoom ?: 0F
                Log.i(TAG, "zoom=>$zoomLvl")
                true
            }
        }
        return true
    }
}
