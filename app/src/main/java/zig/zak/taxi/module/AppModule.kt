package zig.zak.taxi.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import zig.zak.taxi.activity.DriverActivity
import zig.zak.taxi.activity.MainActivity
import zig.zak.taxi.activity.MapActivity
import zig.zak.taxi.activity.PassengerActivity
import zig.zak.taxi.service.TaxiService

@Module
interface AppModule {

    @ContributesAndroidInjector
    fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    fun driverActivity(): DriverActivity

    @ContributesAndroidInjector
    fun mapsActivity(): MapActivity

    @ContributesAndroidInjector(modules = [PassengerActivityModule::class])
    fun passengerActivity(): PassengerActivity

    @ContributesAndroidInjector
    fun service(): TaxiService
}