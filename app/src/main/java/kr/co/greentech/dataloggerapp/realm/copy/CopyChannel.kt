package kr.co.greentech.dataloggerapp.realm.copy

import java.io.Serializable

data class CopyChannel(
        val key: Long,
        val name: String,
        var isOn: Boolean,
        val sensorType: Int,
        val decPoint: Int,
        val unit: Int,
        val unitInput: String,
        val capacity: Float,
        val ro: Float,
        val gf: Float,
        val graphAxis: Int,
        val filter: Int,
        var adjustA: Float,
        val adjustB: Float,
        val lineColor: String?
) : Serializable