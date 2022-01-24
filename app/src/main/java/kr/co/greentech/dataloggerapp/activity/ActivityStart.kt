package kr.co.greentech.dataloggerapp.activity

import android.Manifest.permission.*
import android.content.pm.PackageManager.*
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.FragmentManager
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.channel.enums.GraphAxisType
import kr.co.greentech.dataloggerapp.fragment.menu.fragment.FragmentStartMenu
import kr.co.greentech.dataloggerapp.realm.*
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager


class ActivityStart : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    companion object {
        const val REQUEST_CODE: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        initRealm()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        supportFragmentManager.addOnBackStackChangedListener(this)
        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().add(R.id.fragment, FragmentStartMenu.newInstance(), "menu").commit()
        else
            onBackStackChanged()
    }

    override fun onBackStackChanged() {
        isVisibleActionBar(resources.configuration.orientation)
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // 회전 시 엑티비티 새로 생성하지 않기 위함
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isVisibleActionBar(newConfig.orientation)
    }

    private fun isVisibleActionBar(orientation: Int) {
        if (supportActionBar == null) return

        val tv = findViewById<TextView>(R.id.action_bar_title)

        if(tv.text == getString(R.string.graph_review)
            || tv.text == getString(R.string.measure)) {
            if (supportActionBar!!.isShowing) {
                supportActionBar!!.hide()
            }
        } else {
            if (!supportActionBar!!.isShowing) {
                supportActionBar!!.show()
            }
        }
    }

    private fun initRealm() {
        // TODO: - 추후에 마이그레이션 지원하기
//        val realmConfiguration = RealmConfiguration.Builder()
//                .deleteRealmIfMigrationNeeded()
//                .build()
//        Realm.deleteRealm(realmConfiguration)

        val channelList = RealmChannel.select()
        val colors = ArrayList<String>()

        colors.add("FF5252")
        colors.add("536DFE")
        colors.add("FFAB40")
        colors.add("B388FF")

        colors.add("00b07b")
        colors.add("424242")
        colors.add("FF6E40")
        colors.add("7C4DFF")

        if (channelList.isEmpty()) {
            for(i in 0 until 64) {
                Thread.sleep(1)
                val channel = RealmChannel()
                channel.name = "CH${i+1}"
                channel.isOn = true
                channel.lineColor = colors[i % colors.size]
                channel.insert()
            }
        }

        val dataLogItem = RealmDataLog.select()
        if (dataLogItem == null) {
            val dataLog = RealmDataLog()
            dataLog.insert()
        }

        val obj = RealmSaveSetting.select()
        if (obj == null) {
            val fileSetting = RealmSaveSetting()
            for (i in 0 until 5) {
                Thread.sleep(1)
                val stepSetting = RealmStepSetting()
                stepSetting.insert()
                fileSetting.addStepSetting(stepSetting)
                if (i == 0) {
                    fileSetting.selectedStep = stepSetting.key
                }
            }
            fileSetting.insert()
        }

        val graphAxis = RealmGraphScaleSet.select()
        if (graphAxis == null) {
            val set = RealmGraphScaleSet()
            val xAxis = ArrayList<RealmGraphScale>()
            xAxis.add(RealmGraphScale())

            val yAxis = ArrayList<RealmGraphScale>()
            for(type in GraphAxisType.values()) {
                Thread.sleep(1)
                yAxis.add(type.getGraphScale())
            }

            Thread.sleep(1)
            val xyXAxis = ArrayList<RealmGraphScale>()
            xyXAxis.add(RealmGraphScale())

            Thread.sleep(1)
            val xyYAxis = ArrayList<RealmGraphScale>()
            xyYAxis.add(RealmGraphScale())

            set.updateAxisList(
                xAxis,
                yAxis,
                xyXAxis,
                xyYAxis
            )

            set.insert()
        }
        val channelCount = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)
        if (channelCount == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.CHANNEL_COUNT, 8)
        }

        val measureSpeed = PreferenceManager.getFloat(PreferenceKey.MEASURE_SPEED)
        if (measureSpeed == PreferenceManager.DEFAULT_VALUE_FLOAT) {
            PreferenceManager.setFloat(PreferenceKey.MEASURE_SPEED, 1.0F)
        }

        val mode = PreferenceManager.getInt(PreferenceKey.MEASURE_MODE)
        if (mode == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.MEASURE_MODE, 0)
        }

        val zeroPoint = PreferenceManager.getInt(PreferenceKey.ZERO_POINT)
        if (zeroPoint == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.ZERO_POINT, 0)
        }

        val bufferSize = PreferenceManager.getInt(PreferenceKey.GRAPH_BUFFER_SIZE)
        if (bufferSize == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.GRAPH_BUFFER_SIZE, 1000)
        }

        val graphChannelCount = PreferenceManager.getInt(PreferenceKey.GRAPH_CHANNEL_COUNT)
        if (graphChannelCount == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.GRAPH_CHANNEL_COUNT, 4)
        }

        val selectChannelToMeasure = PreferenceManager.getString(PreferenceKey.SELECTED_CHANNEL_TO_MEASURE)
        if (selectChannelToMeasure == PreferenceManager.DEFAULT_VALUE_STRING) {
            PreferenceManager.setString(PreferenceKey.SELECTED_CHANNEL_TO_MEASURE, "0, 1, 2, 3")
        }

        val stepXyYAxis = PreferenceManager.getInt(PreferenceKey.STEP_XY_Y_AXIS)
        if (stepXyYAxis == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.STEP_XY_Y_AXIS, 0)
        }

        val graphLineType = PreferenceManager.getInt(PreferenceKey.GRAPH_LINE_TYPE)
        if (graphLineType == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.GRAPH_LINE_TYPE, 0)
        }

        val stepGraphInterval = PreferenceManager.getInt(PreferenceKey.STEP_GRAPH_INTERVAL)
        if (stepGraphInterval == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.STEP_GRAPH_INTERVAL, 0)
        }

        val visibleMeasureTimeText = PreferenceManager.getInt(PreferenceKey.VISIBLE_MEASURE_TIME_TEST)
        if (visibleMeasureTimeText == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.VISIBLE_MEASURE_TIME_TEST, 0)
        }
    }

    fun setTitle(text: String) {
        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.text = text
    }

    // 권한 요청
    private fun checkPermission() {
        if (
            checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_DENIED
            || checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_DENIED
            || checkSelfPermission(this, FOREGROUND_SERVICE) == PERMISSION_DENIED
            || checkSelfPermission(this, BLUETOOTH) == PERMISSION_DENIED
            || checkSelfPermission(this, BLUETOOTH_ADMIN) == PERMISSION_DENIED
            || checkSelfPermission(this, INTERNET) == PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(
                WRITE_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE,
                FOREGROUND_SERVICE,
                BLUETOOTH,
                BLUETOOTH_ADMIN,
                INTERNET
            ), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                permissions.forEachIndexed { i, s ->
                    val grantResult = grantResults[i]
                    if (
                        s == WRITE_EXTERNAL_STORAGE
                        || s == READ_EXTERNAL_STORAGE
                        || s == FOREGROUND_SERVICE
                        || s == BLUETOOTH
                        || s == BLUETOOTH_ADMIN
                        || s == INTERNET
                    ) {
                        if (grantResult == PERMISSION_DENIED) {
                            checkPermission()
                        }
                    }
                }
            }
        }
    }
}