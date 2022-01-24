package kr.co.greentech.dataloggerapp.application

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.realm.Realm
import io.realm.RealmConfiguration
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager

class DataLogApplication: Application() {

    companion object {
        lateinit var instance: DataLogApplication
        const val THROTTLE = 1000L

        fun getContext(): Context {
            return instance.baseContext
        }

        fun getRealmConfig(): RealmConfiguration {
            return RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build()
        }

        fun getColor(id: Int): Int {
            return ContextCompat.getColor(getContext(), id)
        }

        fun getDrawable(id: Int): Drawable? {
            return ContextCompat.getDrawable(getContext(), id)
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        Realm.init(this)
        CalculatorUtil.setContext(this)
        PreferenceManager.setApplicationContext(this)
    }
}