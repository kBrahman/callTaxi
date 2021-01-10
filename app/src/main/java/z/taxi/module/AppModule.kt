package z.taxi.module

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.android.DaggerApplication
import z.taxi.manager.ApiManager
import z.taxi.manager.LocationManager
import z.taxi.util.TAXOR
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun locProvider(ctx: DaggerApplication) = FusedLocationProviderClient(ctx)

    @Singleton
    @Provides
    fun provideLocationManager(provider: FusedLocationProviderClient, apiManager: ApiManager) = LocationManager(provider, apiManager)

    @Provides
    fun sharedPrefs(ctx: DaggerApplication) = ctx.getSharedPreferences("${ctx.packageName}.$TAXOR", Context.MODE_PRIVATE)
}