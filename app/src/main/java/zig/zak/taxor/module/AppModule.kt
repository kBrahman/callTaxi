package zig.zak.taxor.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import zig.zak.taxor.activity.DriverActivity
import zig.zak.taxor.activity.MainActivity
import zig.zak.taxor.activity.MapsActivity
import zig.zak.taxor.activity.PassengerActivity
import zig.zak.taxor.service.TaxiService

@Module
interface AppModule {

    @ContributesAndroidInjector
    fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    fun driverActivity(): DriverActivity

    @ContributesAndroidInjector
    fun mapsActivity(): MapsActivity

    @ContributesAndroidInjector(modules = [PassengerActivityModule::class])
    fun passengerActivity(): PassengerActivity

    @ContributesAndroidInjector
    fun service(): TaxiService
}