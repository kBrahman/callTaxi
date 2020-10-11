package zig.zak.taxi.manager

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import zig.zak.taxi.BuildConfig
import zig.zak.taxi.model.Taxist
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class ApiManager @Inject constructor() {

    companion object {
        private val TAG: String = ApiManager::class.java.simpleName
        private const val SERVER = BuildConfig.SERVER
        private const val SEND = "send"
        private const val REMOVE = "remove"
        private const val SEND_COORDS = "send-coords"
        private const val GET_TAXISTS = "get"
    }

    private var apiService: APIService

    init {
        apiService = Retrofit.Builder()
                .client(OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS).build())
                .baseUrl(SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(APIService::class.java)
    }

    fun send(taxist: Taxist?, callback: Callback<Boolean>) {
        apiService.send(taxist).enqueue(callback)
    }

    fun remove(driverPhone: String) {
        Thread { apiService.remove(driverPhone).execute() }.start()
    }

    fun sendCoords(phone: String?, latitude: Double, longitude: Double) {
        Thread { apiService.sendCoords("$phone:$latitude:$longitude").execute() }.start()
    }

    fun get(lat: Double, lon: Double, callback: Callback<List<Taxist>>) {
        apiService.get("$lat:$lon").enqueue(callback)
    }

    interface APIService {
        @POST(SEND)
        fun send(@Body taxist: Taxist?): Call<Boolean>

        @POST(REMOVE)
        fun remove(@Body driverPhone: String): Call<Void>

        @POST(SEND_COORDS)
        fun sendCoords(@Body phoneLatLon: String): Call<Void>

        @POST(GET_TAXISTS)
        fun get(@Body s: String): Call<List<Taxist>>
    }
}
