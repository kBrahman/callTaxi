package z.taxi.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

private const val TAG: String = "Utils"
const val TAXOR = "TAXOR"
const val REQUEST_CODE_LOCATION_PERMISSION = 1
const val FINISH_TAXI_DRIVER_ACTIVITY = "finish_taxi_driver_activity"
const val PHONE = "phone"
const val NAME = "name"
const val PERMISSION_REQUEST = 2

fun permissionsOk(ctx: Context, permissions: Array<String>) = !permissions.any { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_DENIED }
