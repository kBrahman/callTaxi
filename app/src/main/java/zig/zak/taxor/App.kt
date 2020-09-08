package zig.zak.taxor

import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector
import zig.zak.taxor.component.DaggerAppComponent

class App : DaggerApplication(), HasAndroidInjector {

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}