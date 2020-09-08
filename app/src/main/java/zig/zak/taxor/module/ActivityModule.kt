package zig.zak.taxor.module

import dagger.Module
import dagger.Provides
import dagger.android.DaggerApplication
import zig.zak.taxor.manager.ApiManager
import zig.zak.taxor.manager.LocationManager
import javax.inject.Singleton

@Module
class ActivityModule {

    @Singleton
    @Provides
    fun provideLocationManager(ctx: DaggerApplication, apiManager: ApiManager) = LocationManager(ctx, apiManager)
}