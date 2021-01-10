package z.taxi.module

import android.media.MediaPlayer
import android.provider.Settings
import dagger.Module
import dagger.Provides
import dagger.android.DaggerApplication
import z.taxi.annotation.ActivityScope

@Module
class DriverActivityModule {

    @Provides
    @ActivityScope
    fun provideMediaPlayer(ctx: DaggerApplication) = MediaPlayer.create(ctx, Settings.System.DEFAULT_RINGTONE_URI)
}