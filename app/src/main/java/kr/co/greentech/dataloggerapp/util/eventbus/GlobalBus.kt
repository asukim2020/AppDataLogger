package kr.co.greentech.dataloggerapp.util.eventbus

import org.greenrobot.eventbus.EventBus




class GlobalBus {
    companion object {
        var sBus: EventBus? = null
        fun getBus(): EventBus {
            if (sBus == null) sBus = EventBus.getDefault()
            return sBus!!
        }
    }
}