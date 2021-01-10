package z.taxi

import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector
import z.taxi.component.DaggerAppComponent
import javax.inject.Singleton

class App : DaggerApplication(), HasAndroidInjector {

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}