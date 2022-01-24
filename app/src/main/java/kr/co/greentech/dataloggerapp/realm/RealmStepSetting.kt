package kr.co.greentech.dataloggerapp.realm

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kr.co.greentech.dataloggerapp.application.DataLogApplication

open class


RealmStepSetting: RealmObject() {
    @PrimaryKey
    var key: Long = System.currentTimeMillis()
    var stepList = RealmList<Long>()

    companion object {
        fun select(): List<RealmStepSetting> {
            val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
            realm.beginTransaction()
            val item = realm.where(RealmStepSetting::class.java).findAll()
            realm.commitTransaction()
            return item.toList()
        }
    }

    fun updateStepList(
        list: ArrayList<Long>
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        stepList.clear()

        for(item in list) {
            stepList.add(item)
        }

        realm.commitTransaction()
    }

    fun insert() {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        realm.insertOrUpdate(this)
        realm.commitTransaction()
    }
}