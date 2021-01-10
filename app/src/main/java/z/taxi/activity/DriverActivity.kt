package z.taxi.activity

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dagger.android.support.DaggerAppCompatActivity
import io.crossbar.autobahn.websocket.WebSocketConnection
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler
import io.crossbar.autobahn.websocket.exceptions.WebSocketException
import io.crossbar.autobahn.websocket.types.ConnectionResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import z.taxi.BuildConfig
import z.taxi.R
import z.taxi.manager.ApiManager
import z.taxi.manager.LocationManager
import z.taxi.model.Taxist
import z.taxi.service.TaxiService
import z.taxi.util.*
import z.taxi.webrtc.CallActivity
import z.taxi.webrtc.CallActivity.*
import z.taxi.webrtc.util.Util.EXTRA_ID_LOCAL
import javax.inject.Inject

class DriverActivity : DaggerAppCompatActivity() {
    companion object {
        private val TAG: String = DriverActivity::class.java.simpleName
    }

    private lateinit var remoteId: String
    private lateinit var ws: WebSocketConnection
    private lateinit var adView: AdView
    private var taxist: Taxist? = null

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var apiManager: ApiManager

    @Inject
    lateinit var player: MediaPlayer

    @Inject
    lateinit var sharedPrefs: SharedPreferences
    private var didExit = false
    private lateinit var uiState: MutableState<UIState>
    val iceCandidates = mutableListOf<String>()

    private val stopDriverActivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorPrimary = Color(getColor(R.color.colorPrimary))
            uiState = remember { mutableStateOf(UIState.LOADING) }
            when (uiState.value) {
                UIState.LOADING -> Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(getColor(R.color.colorPrimary)))
                }
                UIState.MAIN_SCREEN -> MainScreen(adView)
                UIState.CALL_SCREEN -> CallScreen(colorPrimary)
                UIState.PROFILE_SCREEN -> ProfileScreen(colorPrimary)
            }
        }
        taxist = Taxist(sharedPrefs.getString(NAME, ""), sharedPrefs.getString(PHONE, "").toString())
        send()
        stopService(Intent(this, TaxiService::class.java))
        registerReceiver(stopDriverActivityReceiver, IntentFilter(FINISH_TAXI_DRIVER_ACTIVITY))
        player.isLooping = true
    }

    @Composable
    private fun ProfileScreen(colorPrimary: Color) {
        Column(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val name = remember { mutableStateOf(TextFieldValue(taxist?.name ?: "")) }
            val phone = remember { mutableStateOf(TextFieldValue(taxist?.phone ?: "")) }
            val nameValid = remember { mutableStateOf(true) }
            val phoneValid = remember { mutableStateOf(true) }
            TextField(value = name.value, onValueChange = { n -> name.value = n },
                    label = { Text(getString(R.string.enter_your_name), color = colorPrimary, modifier = Modifier.fillMaxWidth()) },
                    backgroundColor = Color.White, activeColor = colorPrimary)
            if (!nameValid.value) Text(text = getString(R.string.cant_be_empty), color = Color.Red, fontSize = 10.sp)
            TextField(value = phone.value, onValueChange = { n -> phone.value = n },
                    label = { Text(getString(R.string.enter_your_phone), color = colorPrimary, modifier = Modifier.fillMaxWidth()) },
                    backgroundColor = Color.White,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), activeColor = colorPrimary)
            if (!phoneValid.value) Text(text = getString(R.string.cant_be_empty), color = Color.Red, fontSize = 10.sp)
            Spacer(Modifier.weight(1F))
            Button(onClick = {
                val nameTxt = name.value.text
                val phoneTxt = phone.value.text
                when {
                    nameTxt.trim().isEmpty() -> {
                        nameValid.value = false
                    }
                    phoneTxt.trim().isEmpty() -> {
                        phoneValid.value = false
                    }
                    else -> {
                        if (nameTxt != taxist?.name && phoneTxt != taxist?.phone) {
                            sharedPrefs.edit().putString(NAME, nameTxt).putString(PHONE, phoneTxt).apply()
                            taxist?.phone?.let { apiManager.remove(it) }
                            taxist?.name = nameTxt
                            taxist?.phone = phoneTxt
                            apiManager.send(taxist)
                        }
                        uiState.value = UIState.MAIN_SCREEN
                    }
                }
            },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(getColor(R.color.colorPrimary)))) {
                Text(text = getString(R.string.save), color = Color.Yellow)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(getString(R.string.profile))?.apply {
            icon = ContextCompat.getDrawable(this@DriverActivity, R.drawable.ic_person_24)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            setOnMenuItemClickListener {
                Log.i(TAG, "menu click")
                uiState.value = UIState.PROFILE_SCREEN
                true
            }
        }
        return true
    }

    private fun connectWS() {
        ws = WebSocketConnection()
        val wsObserver = object : WebSocketConnectionHandler() {

            override fun onOpen() {
                ws.sendMessage("{\"type\":\"session\",\"id\":\"${taxist?.phone}\"}")
                if (!permissionsOk(this@DriverActivity, MANDATORY_PERMISSIONS)) {
                    requestPermissions(MANDATORY_PERMISSIONS, 3)
                } else {
                    initAd()
                }
            }

            override fun onConnect(response: ConnectionResponse?) {
                Log.i(TAG, "on connect=>${response?.protocol}")
            }

            override fun onClose(code: Int, reason: String?) {
                Log.i(TAG, "on close=>$reason, code=>$code")
            }

            override fun onMessage(payload: String) {
                Log.i(TAG, "onMessage=>$payload")
                val jsonObject = JSONObject(payload)
                if (jsonObject.has("type") && jsonObject.getString("type") == "hang_up") {
                    uiState.value = UIState.MAIN_SCREEN
                    player.stop()
                    player.prepare()
                    iceCandidates.clear()
                    return
                }
                if (jsonObject.has("from")) remoteId = jsonObject.getString("from")
                if (iceCandidates.isEmpty()) uiState.value = UIState.CALL_SCREEN
                iceCandidates.add(payload)
                player.start()
            }
        }
        try {
            ws.connect("ws://192.168.43.106:8081/ws", wsObserver)
        } catch (e: WebSocketException) {
            e.printStackTrace()
            exit()
        }
    }

    @Composable
    private fun CallScreen(colorPrimary: Color) {
        Column(modifier = Modifier
                .fillMaxSize()
                .background(colorPrimary),
                horizontalAlignment = Alignment.CenterHorizontally) {
            Text(getString(R.string.client_calling), color = Color.Yellow, textAlign = TextAlign.Center,
                    fontSize = TextUnit.Sp(40), modifier = Modifier.padding(top = 19.dp))
            Spacer(Modifier
                    .fillMaxHeight()
                    .weight(1F))
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 30.dp, start = 80.dp, end = 80.dp)) {
                Column {
                    IconButton(onClick = ::reject, modifier = Modifier.background(color = Color.Yellow, shape = CircleShape)) {
                        Icon(bitmap = imageResource(id = android.R.drawable.ic_menu_call), tint = Color.Red)
                    }
                    Text(text = getString(R.string.reject), color = Color.Yellow)
                }
                Spacer(Modifier.weight(1f))
                Column {
                    IconButton(onClick = {
                        startCallActivity()
                        player.stop()
                        player.prepare()

                    }, modifier = Modifier.background(color = Color.Yellow, shape = CircleShape)) {
                        Icon(bitmap = imageResource(id = android.R.drawable.ic_menu_call), tint = Color.Green)
                    }
                    Text(text = getString(R.string.answer), color = Color.Yellow)
                }
            }
        }
    }

    private fun reject() {
        Log.i(TAG, "stopAndHide")
        player.stop()
        player.prepare()
        uiState.value = UIState.MAIN_SCREEN
        ws.sendMessage("{\"type\":\"reject\",\"to\":\"$remoteId\"}")
        iceCandidates.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initAd()
        } else {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startCallActivity() {
        val intent = Intent(this, CallActivity::class.java)
        intent.data = Uri.parse(BuildConfig.SERVER)
        intent.putExtra(EXTRA_INITIATOR, false)
        intent.putExtra(EXTRA_VIDEO_CALL, true)
        intent.putExtra(EXTRA_VIDEO_CODEC, "VP8")
        intent.putExtra(EXTRA_ID_LOCAL, taxist?.phone)
        intent.putExtra(EXTRA_ID_REMOTE, remoteId)
        intent.putStringArrayListExtra(DATA_WEBRTC, ArrayList(iceCandidates))
        didExit = true
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        uiState.value = UIState.MAIN_SCREEN
        didExit = false
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        if (!didExit) {
            val intent = Intent(this, TaxiService::class.java)
            intent.putExtra(ContactsContract.Intents.Insert.NAME, taxist?.name)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, taxist?.phone)
            startService(intent)
        }
        val preferences = getSharedPreferences("$packageName.$TAXOR", Context.MODE_PRIVATE)
        preferences.edit().putString(ContactsContract.Intents.Insert.NAME, taxist?.name).putString(ContactsContract.Intents.Insert.PHONE, taxist?.phone).apply()
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        stopService(Intent(this, TaxiService::class.java))
        locationManager.stopSendingLocation = false
        locationManager.sendLocation()
    }

    private fun send() {
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionsOk(this, permissions)) {
            apiManager.send(taxist, object : Callback<Boolean> {
                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    if (t.message.toString().contains("Failed to connect to")) {
                        exit()
                    }
                    t.printStackTrace()
                }

                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.body()!!) {
                        connectWS()
                    } else {
                        Toast.makeText(this@DriverActivity, R.string.try_later, Toast.LENGTH_LONG).show()
                    }
                    locationManager.taxist = taxist
                    locationManager.sendLocation()
                }
            })
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION_PERMISSION)
        }

    }

    private fun exit() {
        Toast.makeText(this@DriverActivity, R.string.maintenance, LENGTH_LONG).show()
        didExit = true
        finish()
    }

    private fun initAd() {
        setTitle(R.string.working)
        adView = AdView(this)
        adView.adSize = AdSize.MEDIUM_RECTANGLE
        adView.adUnitId = getString(R.string.banner_id)
        adView.loadAd(AdRequest.Builder().build())
        adView.visibility = GONE
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = VISIBLE
            }
        }
        uiState.value = UIState.MAIN_SCREEN
    }

    @Composable
    fun MainScreen(adView: View) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = getString(R.string.waiting_for_orders), fontSize = 20.sp)
            Text(text = getString(R.string.sending_loc), fontSize = 10.sp)
            Spacer(modifier = Modifier.preferredHeight(15.dp))
            Button(onClick = ::haveRest,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(getColor(R.color.colorPrimary)))) {
                Text(text = getString(R.string.rest), color = Color.Yellow)
            }
            Spacer(modifier = Modifier.preferredHeight(32.dp))
            AndroidView(viewBlock = { adView })
        }
    }

    private fun haveRest() {
        val phone = taxist?.phone
        locationManager.stopSendingLocation = true
        phone?.let { apiManager.remove(it) }
        didExit = true
        ws.sendClose()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stopDriverActivityReceiver)
    }

    private enum class UIState {
        LOADING,
        MAIN_SCREEN,
        CALL_SCREEN,
        PROFILE_SCREEN
    }
}
