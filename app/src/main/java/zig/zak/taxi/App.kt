package zig.zak.taxi

import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector
import zig.zak.taxi.component.DaggerAppComponent

class App : DaggerApplication(), HasAndroidInjector {

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}