package kr.co.greentech.dataloggerapp.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.SensorGageRangeType
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.StrainGageRangeType

open class RealmDataLog: RealmObject() {
    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var strainGageRange: Int = 0
    var sensorGageRange: Int = 0


    companion object {
        fun select(): RealmDataLog? {
            val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
            realm.beginTransaction()
            val item = realm.where(RealmDataLog::class.java).findFirst()
            realm.commitTransaction()
            return item
        }
    }

    fun update(
            strainGageRange: Int,
            sensorGageRange: Int
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()

        if (0 <= strainGageRange && strainGageRange < StrainGageRangeType.values().size) {
            this.strainGageRange = strainGageRange
        }

        if (0 <= sensorGageRange && sensorGageRange < SensorGageRangeType.values().size) {
            this.sensorGageRange = sensorGageRange
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