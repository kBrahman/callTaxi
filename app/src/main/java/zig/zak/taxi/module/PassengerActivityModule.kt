package zig.zak.taxi.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import zig.zak.taxi.fragment.TaxMapFragment
import zig.zak.taxi.fragment.TaxisFragment

@Module
interface PassengerActivityModule {

    @ContributesAndroidInjector
    fun taxisFragment(): TaxisFragment

    @ContributesAndroidInjector
    fun taxMapFragment(): TaxMapFragment
}
