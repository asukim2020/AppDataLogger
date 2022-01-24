package kr.co.greentech.dataloggerapp.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.realm.copy.CopyGraphScale

open class RealmGraphScale: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var isOn: Boolean = true
    var min: Float = 0F
    var max: Float = 100F

    companion object {
        fun select(): RealmGraphScale? {
            val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
            realm.beginTransaction()
            val item = realm.where(RealmGraphScale::class.java).findFirst()
            realm.commitTransaction()
            return item
        }
    }

    fun getCopy(): CopyGraphScale {
        return CopyGraphScale(
                isOn,
                min,
                max
        )
    }

    fun updateIsOn(
            isOn: Boolean
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        this.isOn = isOn
        realm.commitTransaction()
    }

    fun update(
            isOn: Boolean,
            min: Float,
            max: Float
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()

        this.isOn = isOn

        if(min < max) {
            this.min = min
            this.max = max
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