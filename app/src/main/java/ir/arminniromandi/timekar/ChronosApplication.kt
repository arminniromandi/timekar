package ir.arminniromandi.timekar

import android.app.Application
import ir.arminniromandi.timekar.di.AppContainer
import ir.arminniromandi.timekar.di.DefaultAppContainer

class ChronosApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

    }
}
