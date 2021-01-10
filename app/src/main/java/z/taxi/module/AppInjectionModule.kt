package z.taxi.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import z.taxi.activity.DriverActivity
import z.taxi.activity.MainActivity
import z.taxi.activity.MapActivity
import z.taxi.activity.PassengerActivity
import z.taxi.annotation.ActivityScope
import z.taxi.service.TaxiService

@Module
interface AppInjectionModule {

    @ContributesAndroidInjector
    fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [DriverActivityModule::class])
    fun driverActivity(): DriverActivity

    @ContributesAndroidInjector
    fun mapsActivity(): MapActivity

//    @ContributesAndroidInjector
//    fun callActivity(): CallActivity

    @ContributesAndroidInjector
    fun passengerActivity(): PassengerActivity

    @ContributesAndroidInjector
    fun service(): TaxiService
}