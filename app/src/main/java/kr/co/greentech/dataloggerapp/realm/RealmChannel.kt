package kr.co.greentech.dataloggerapp.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.channel.enums.*
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel

open class RealmChannel: RealmObject() {
    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""
    var isOn: Boolean = false
    var sensorType: Int = 0
    var decPoint: Int = 0
    var unit: Int = 0
    var unitInput: String = ""
    var capacity: Float = 1.0F
    var ro: Float = 1.0F
    var gf: Float = 1.0F
    var graphAxis: Int = 0
    var filter: Int = 0
    var adjustA: Float = 1.0F
    var adjustB: Float = 0.0F
    var lineColor: String? = null

    companion object {
        fun select(): List<RealmChannel> {
            val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
            realm.beginTransaction()
            val list = realm.where(RealmChannel::class.java).findAll()
            realm.commitTransaction()
            return list
        }

        fun getCopyChannelList(): List<CopyChannel> {
            val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
            realm.beginTransaction()
            val list = realm.where(RealmChannel::class.java).findAll()
            realm.commitTransaction()

            val copyList = ArrayList<CopyChannel>()

            for (channel in list) {
                val copyChannel = CopyChannel(
                        channel.key,
                        channel.name,
                        channel.isOn,
                        channel.sensorType,
                        channel.decPoint,
                        channel.unit,
                        channel.unitInput,
                        channel.capacity,
                        channel.ro,
                        channel.gf,
                        channel.graphAxis,
                        channel.filter,
                        channel.adjustA,
                        channel.adjustB,
                        channel.lineColor
                )
                copyList.add(copyChannel)
            }
            return copyList
        }
    }

    fun insert() {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        realm.insertOrUpdate(this)
        realm.commitTransaction()
    }

    fun updateIsOn(isOn: Boolean) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        this.isOn = isOn
        realm.commitTransaction()
    }

    fun update(
            name: String,
            sensorType: Int,
            decPoint: Int,
            unit: Int,
            unitInput: String,
            capacity: Float,
            ro: Float,
            gf: Float,
            graphAxis: Int,
            filter: Int,
            adjustA: Float,
            adjustB: Float,
            lineColor: String?
    ) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        if (name != "") {
            this.name = name
        }

        if (0 <= sensorType && sensorType < SensorType.values().size) {
            this.sensorType = sensorType
        }

        if (0 <= decPoint && decPoint <DecimalPointType.values().size) {
            this.decPoint = decPoint
        }

        for (i in UnitType.values()) {
            if (i.value == unit) {
                this.unit = unit
            }
        }

        if (unitInput != "") {
            this.unitInput = unitInput
        }

        if (capacity != 0.0F) {
            this.capacity = capacity
        }

        if (ro != 0.0F) {
            this.ro = ro
        }

        if (gf != 0.0F) {
            this.gf = gf
        }

        if (0 <= graphAxis && graphAxis < GraphAxisType.values().size) {
            this.graphAxis = graphAxis
        }

        if (0 <= filter && filter < FilterType.values().size) {
            this.filter = filter
        }

        if (adjustA != 0.0F) {
            this.adjustA = adjustA
        }

        this.adjustB = adjustB

        if (lineColor != null) {
            if (lineColor.length == 6 || lineColor.length == 8) {
                this.lineColor = lineColor
            }
        }

        realm.commitTransaction()
    }

    fun updateLineColor(lineColor: String) {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        if (lineColor.length == 6 || lineColor.length == 8) {
            this.lineColor = lineColor
        }
        realm.commitTransaction()
    }

    fun delete() {
        val realm = Realm.getInstance(DataLogApplication.getRealmConfig())
        realm.beginTransaction()
        this.deleteFromRealm()
        realm.commitTransaction()
    }
}