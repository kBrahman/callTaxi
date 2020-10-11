package zig.zak.taxi.module

import dagger.Module
import dagger.Provides
import dagger.android.DaggerApplication
import zig.zak.taxi.manager.ApiManager
import zig.zak.taxi.manager.LocationManager
import javax.inject.Singleton

@Module
class ActivityModule {

    @Singleton
    @Provides
    fun provideLocationManager(ctx: DaggerApplication, apiManager: ApiManager) = LocationManager(ctx, apiManager)
}