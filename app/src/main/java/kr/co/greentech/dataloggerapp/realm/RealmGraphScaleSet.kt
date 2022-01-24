package kr.co.greentech.dataloggerapp.realm

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.channel.enums.GraphAxisType

open class RealmGraphScaleSet: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var timeLeftYAxisType: Int = 0
    var timeRightYAxisType: Int = 1

    var timeXAxis = RealmList<RealmGraphScale>()
    var timeYAxis = RealmList<RealmGraphScale>()

    var xyXAxisType: Int = 0
    var xyYAxisType: Int = 1

    var xyXAxis = RealmList<RealmGraphScale>()
    var xyYAxis = RealmList<RealmGraphScale>()

    companion object {
        fun select(): RealmGraphScaleSet? {
            val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
            realm.beginTransaction()
            val item = realm.where(RealmGraphScaleSet::class.java).findFirst()
            realm.commitTransaction()
            return item
        }
    }

    fun updateTimeChartAxisType(
            timeLeftYAxisType: Int,
            timeRightYAxisType: Int
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()

        if(0 <= timeLeftYAxisType && timeLeftYAxisType < GraphAxisType.values().size) {
            this.timeLeftYAxisType = timeLeftYAxisType
        }

        if(0 <= timeRightYAxisType && timeRightYAxisType < GraphAxisType.values().size) {
            this.timeRightYAxisType = timeRightYAxisType
        }

        realm.commitTransaction()
    }

    fun updateXYChartAxisType(
            xyXAxisType: Int,
            xyYAxisType: Int
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()

        if(0 <= xyXAxisType && xyXAxisType < GraphAxisType.values().size) {
            this.xyXAxisType = xyXAxisType
        }

        if(0 <= xyYAxisType && xyYAxisType < GraphAxisType.values().size) {
            this.xyYAxisType = xyYAxisType
        }

        realm.commitTransaction()
    }

    fun updateAxisList(
            xAxis: ArrayList<RealmGraphScale>,
            yAxis: ArrayList<RealmGraphScale>,
            xyXAxis: ArrayList<RealmGraphScale>,
            xyYAxis: ArrayList<RealmGraphScale>
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()

        for(item in xAxis) {
            this.timeXAxis.add(item)
        }

        for (item in yAxis) {
            this.timeYAxis.add(item)
        }

        for (item in xyXAxis) {
            this.xyXAxis.add(item)
        }

        for (item in xyYAxis) {
            this.xyYAxis.add(item)
        }

        realm.commitTransaction()
    }

    fun insert() {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        realm.insertOrUpdate(this)
        realm.commitTransaction()
    }

    fun delete() {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        this.deleteFromRealm()
        realm.commitTransaction()
    }
}