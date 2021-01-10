package z.taxi.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import dagger.android.support.DaggerAppCompatActivity
import z.taxi.R
import z.taxi.manager.LocationManager
import z.taxi.util.REQUEST_CODE_LOCATION_PERMISSION
import z.taxi.util.TAXOR
import z.taxi.util.permissionsOk
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {


    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private const val TAXI_DRIVER = "taxi_driver"
        private const val WHO = "who"
        private const val PASSENGER = "activity_passenger"
        private const val REQUEST_CODE_GET_LOCATION = 1
    }

    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNetworkConnected()) {
            setTitle(R.string.network_error)
            setContent {
                Column(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                    Text(text = getString(R.string.network_not_available), fontSize = 20.sp)
                    Spacer(modifier = Modifier.preferredHeight(12.dp))
                    Button(onClick = ::refresh,
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(getColor(R.color.colorPrimary)))) {
                        Text(text = getString(R.string.refresh), color = Color.Yellow)
                    }
                }
            }
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionsOk(this, permissions)) {
            detectLocation()
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION_PERMISSION)
        }
    }

    private fun detectLocation() {
        setContent {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(getColor(R.color.colorPrimary)))
                Text(text = getString(R.string.detection))
            }
        }
        locationManager.detect({
            startActivityForResult(Intent(this, MapActivity::class.java), REQUEST_CODE_GET_LOCATION)
        }, ::init)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "picked loc=>${locationManager.lastKnownLocation}")
            init()
        } else {
            finish()
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation()
            } else {
                AlertDialog.Builder(this).setMessage(R.string.need_loc_perm)
                        .setPositiveButton(R.string.grant_permission) { _, _ ->
                            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION_PERMISSION)
                        }.setNegativeButton(R.string.exit) { _, _ ->
                            finish()
                        }.show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun refresh() {
        if (isNetworkConnected()) {
            requestPermission()
        }
    }

    private fun init() {
        Log.i(TAG, "init")
        val sharedPreferences = getSharedPreferences("$packageName.$TAXOR", Context.MODE_PRIVATE)
        when (sharedPreferences.getString(WHO, null)) {
            null ->
                setContent {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val btn = createRef()
                        val sState = rememberScrollState(0f)
                        val visibility = remember { mutableStateOf(VISIBLE) }
                        val isPassenger = remember { mutableStateOf<Boolean?>(null) }
                        val name = remember { mutableStateOf(TextFieldValue()) }
                        val phone = remember { mutableStateOf(TextFieldValue()) }
                        val nameValid = remember { mutableStateOf(true) }
                        val phoneValid = remember { mutableStateOf(true) }
                        ScrollableColumn(modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp),
                                scrollState = sState) {
                            Text(text = getString(R.string.select_mode), fontSize = 25.sp)
                            Text(text = getString(R.string.who_are_you), fontSize = 25.sp)
                            Spacer(modifier = Modifier.preferredHeight(16.dp))
                            CheckTextRow(sState, visibility, name, phone, isPassenger, nameValid, phoneValid)
                        }
                        if (visibility.value == VISIBLE) {
                            Button({
                                when {
                                    isPassenger.value == true -> {
                                        sharedPreferences.edit().putString(WHO, PASSENGER).apply()
                                        init()
                                    }
                                    name.value.text.trim().isEmpty() -> {
                                        nameValid.value = false
                                    }
                                    phone.value.text.trim().isEmpty() -> {
                                        phoneValid.value = false
                                    }
                                    else -> {
                                        sharedPreferences.edit().putString(WHO, TAXI_DRIVER)
                                                .putString(ContactsContract.Intents.Insert.NAME, name.value.text)
                                                .putString(ContactsContract.Intents.Insert.PHONE, phone.value.text)
                                                .apply()
                                        init()
                                    }
                                }
                            }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(getColor(R.color.colorPrimary))),
                                    modifier = Modifier.constrainAs(btn) {
                                        bottom.linkTo(parent.bottom, margin = 16.dp)
                                        centerHorizontallyTo(parent)
                                    }) {
                                Text(text = getString(R.string.start), color = Color.Yellow)
                            }
                        }
                    }
                }
            TAXI_DRIVER -> {
                startActivity(Intent(this, DriverActivity::class.java))
                finish()
            }
            else -> {
                startActivity(Intent(this, PassengerActivity::class.java))
                finish()
            }
        }
    }

    @Composable
    private fun CheckTextRow(sState: ScrollState, visibility: MutableState<Int>, name: MutableState<TextFieldValue>,
                             phone: MutableState<TextFieldValue>, isPassenger: MutableState<Boolean?>,
                             nameValid: MutableState<Boolean>, phoneValid: MutableState<Boolean>) {

        val colorPrimary = Color(getColor(R.color.colorPrimary))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isPassenger.value == true, { isPassenger.value = true },
                    colors = CheckboxDefaults.colors(checkedColor = colorPrimary, checkmarkColor = Color.Yellow))
            Text(text = getString(R.string.passenger), fontSize = 24.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.preferredHeight(15.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isPassenger.value == false, { isPassenger.value = false },
                    colors = CheckboxDefaults.colors(checkedColor = colorPrimary, checkmarkColor = Color.Yellow))
            Text(text = getString(R.string.taxi_driver), fontSize = 24.sp, modifier = Modifier.padding(start = 8.dp))
        }
        if (isPassenger.value == false) {
            TextFields(name, colorPrimary, phone, sState, visibility, nameValid, phoneValid)
        }
    }

    @Composable
    private fun TextFields(name: MutableState<TextFieldValue>, colorPrimary: Color, phone: MutableState<TextFieldValue>,
                           sState: ScrollState, visibility: MutableState<Int>, nameValid: MutableState<Boolean>,
                           phoneValid: MutableState<Boolean>) {
        var scrolled = false
        val onFocusChanged = Modifier.onFocusEvent {
            if (it == FocusState.Active && !scrolled && sState.maxValue > 0F) {
                sState.scrollBy(sState.maxValue)
                visibility.value = GONE
                scrolled = true
            } else if (sState.maxValue == 0F) {
                visibility.value = VISIBLE
            }
        }
        TextField(value = name.value, onValueChange = { n -> name.value = n },
                label = { Text(getString(R.string.enter_your_name), color = colorPrimary, modifier = Modifier.fillMaxWidth()) },
                backgroundColor = Color.White, modifier = onFocusChanged, activeColor = colorPrimary)
        if (!nameValid.value) Text(text = getString(R.string.cant_be_empty), color = Color.Red, fontSize = 10.sp)
        TextField(value = phone.value, onValueChange = { n -> phone.value = n },
                label = { Text(getString(R.string.enter_your_phone), color = colorPrimary, modifier = Modifier.fillMaxWidth()) },
                backgroundColor = Color.White, modifier = onFocusChanged,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), activeColor = colorPrimary)
        if (!phoneValid.value) Text(text = getString(R.string.cant_be_empty), color = Color.Red, fontSize = 10.sp)
    }

    private fun isNetworkConnected() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo != null

}
