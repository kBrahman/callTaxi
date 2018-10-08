package zig.zak.taxor.util

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

const val TAXOR = "TAXOR"
const val REQUEST_CODE_LOCATION_PERMISSION = 1
const val DID_EXIT = "did_exit"
const val FINISH_TAXI_DRIVER_ACTIVITY = "finish_taxi_driver_activity"

fun permissionsOk(ctx: Context, permissions: Array<String>) = !permissions.any { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_DENIED }