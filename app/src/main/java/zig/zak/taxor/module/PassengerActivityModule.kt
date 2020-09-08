package zig.zak.taxor.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import zig.zak.taxor.fragment.TaxMapFragment
import zig.zak.taxor.fragment.TaxisFragment

@Module
interface PassengerActivityModule {

    @ContributesAndroidInjector
    fun taxisFragment(): TaxisFragment

    @ContributesAndroidInjector
    fun taxMapFragment(): TaxMapFragment
}
