package kr.co.greentech.dataloggerapp.realm

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kr.co.greentech.dataloggerapp.application.DataLogApplication

open class RealmSaveSetting: RealmObject() {
    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    // 0: 등간격
    // 1: 스텝
    var interval: Long = 1
    var selectedStep: Long = 0
    var stepSettingList = RealmList<RealmStepSetting>()

    companion object {
        fun select(): RealmSaveSetting? {

            val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
            realm.beginTransaction()
            val item = realm.where(RealmSaveSetting::class.java).findFirst()
            realm.commitTransaction()
            return item
        }
    }

    fun updateInterval(
        interval: Long
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        if (0 <= interval) {
            this.interval = interval
        }
        realm.commitTransaction()
    }
    
    fun addStepSetting(
        item: RealmStepSetting
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        stepSettingList.add(item)
        realm.commitTransaction()
    }

    fun updateSelectedStep(
        key: Long
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        this.selectedStep = key
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