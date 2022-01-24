package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentChangeGraphXAxis
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import java.util.concurrent.TimeUnit

class ChannelHolder(
        val context: Context,
        val view: View,
        val adapter: RecyclerViewAdapter,
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {

        when(position) {
            0 -> { }
            else -> {
                val title = view.findViewById<TextView>(R.id.title_channel)
                val titleName = view.findViewById<TextView>(R.id.title_channel_name)
                val switch = view.findViewById<SwitchCompat>(R.id.button_switch)
                val item = adapter.list[position - 1] as CopyChannel

                title.text = "CH${position}"
                titleName.text = item.name
                switch.isChecked = item.isOn

                switch.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (switch.isShown) {
                        val item = adapter.list[position - 1] as CopyChannel
                        item.isOn = !item.isOn
                        switch.isChecked = item.isOn
                    }
                }

                view.clicks()
                    .throttleFirst(DataLogApplication.THROTTLE, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val event = MapEvent(HashMap())
                        event.map[ChannelHolder.toString()] = ChannelHolder.toString()
                        event.map["position"] = position
                        GlobalBus.getBus().post(event)
                    }

            }
        }
    }

    companion object {
        override fun toString(): String {
            return "ChannelHolder"
        }
    }
}