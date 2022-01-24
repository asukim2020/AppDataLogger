package kr.co.greentech.dataloggerapp.fragment.measure.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.fragment.measure.bluetooth.SerialListener
import kr.co.greentech.dataloggerapp.fragment.measure.bluetooth.SerialService
import kr.co.greentech.dataloggerapp.fragment.measure.bluetooth.SerialSocket
import kr.co.greentech.dataloggerapp.realm.*
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.*
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FragmentBluetoothMeasure: Fragment(), ServiceConnection, SerialListener {

    companion object {
        fun newInstance(): FragmentBluetoothMeasure {
            return FragmentBluetoothMeasure()
        }

        var service: SerialService? = null
        var connected = Connected.False
        const val MAX_RECONNECT_COUNT = 5
    }

    enum class Connected {
        False, Pending, True
    }

    private var deviceAddress: String? = null
    private var initialStart = true
    private var pendingNewline = false
    private val newline: String = TextUtil.newline_crlf

    private var receivedMsg = ""
    private var lastReceivedMsg = ""

    private lateinit var copyChannelList: List<CopyChannel>
    private lateinit var realmDataLog: RealmDataLog

    private val measureFragment = FragmentMeasure.newInstance()

    // UI
    private lateinit var progressLayout: FrameLayout

    private var reConnectCount = 0
    private var repeatFlag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Asu", "onCreate")
        retainInstance = true
        deviceAddress = arguments!!.getString("device")

        copyChannelList = RealmChannel.getCopyChannelList()
        realmDataLog = RealmDataLog.select()!!
    }

    override fun onResume() {
        super.onResume()
        Log.d("Asu", "onResume")
        if (initialStart && service != null) {
            reConnectCount = 0
            initialStart = false
            requireActivity().runOnUiThread { connect() }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d("Asu", "onCreateView")
        val context = requireContext()
        val view = inflater.inflate(R.layout.fragment_bluetooth_measure, container, false)

        progressLayout = view.findViewById(R.id.progress_layout)
        childFragmentManager.beginTransaction().add(R.id.fragment_measure, measureFragment, FragmentMeasure.toString()).commit()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d("Asu", "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        (activity as ActivityStart?)!!.setTitle(getString(R.string.measure))

        addBackButtonEvent()
    }

    override fun onDestroy() {
        Log.d("Asu", "onDestroy")
        if (connected != Connected.False) {
            send("*T$")
            disconnect()
        }
        requireActivity().stopService(Intent(activity, SerialService::class.java))
        super.onDestroy()
    }

    override fun onStart() {
        Log.d("Asu", "onStart")
        super.onStart()
        if (service != null)
            service!!.attach(this)
        else
            requireActivity().startService(Intent(activity, SerialService::class.java))

        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        Log.d("Asu", "onStop")
        if (service != null && !requireActivity().isChangingConfigurations) service!!.detach()
        GlobalBus.getBus().unregister(this)
        super.onStop()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("Asu", "onAttach")
        super.onAttach(context)
        requireActivity().bindService(
            Intent(activity, SerialService::class.java),
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        Log.d("Asu", "onServiceConnected")
        service = (binder as SerialService.SerialBinder).service
        service?.attach(this)
        if (initialStart && isResumed) {
            initialStart = false
            requireActivity().runOnUiThread { this.connect() }
        }
    }

    override fun onDetach() {
        Log.d("Asu", "onDetach")
        try {
            requireActivity().unbindService(this)
        } catch (ignored: java.lang.Exception) {
        }
        super.onDetach()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("Asu", "onServiceDisconnected")
        service = null
    }

    override fun onSerialConnect() {
        Log.d("Asu", "onSerialConnect")
        connected = Connected.True
        val sendString = measureFragment.getChannelSettingString()
        send(sendString)
        progressLayout.visibility = View.INVISIBLE
    }

    override fun onSerialConnectError(e: Exception?) {
        Log.d("Asu", "onSerialConnectError, e: ${e?.localizedMessage}")
        disconnect()
        reConnectRepeat()
    }

    override fun onSerialRead(data: ByteArray?) {
        if (data != null) {
            receive(data)
        }
    }

    override fun onSerialIoError(e: Exception?) {
        Log.d("Asu", "onSerialIoError: ${e?.localizedMessage}")
        disconnect()
        reConnectRepeat()
    }

    private fun connect() {
        Log.d("Asu", "connect")
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            connected = Connected.Pending
            val socket = SerialSocket(requireActivity().applicationContext, device)
            service?.connect(socket)
        } catch (e: java.lang.Exception) {
            onSerialConnectError(e)
        }
    }

    private fun disconnect() {
        Log.d("Asu", "disconnect")
        connected = Connected.False
        service?.disconnect()

        measureFragment.measureEnd()
    }

    private fun send(str: String) {
        if (connected != Connected.True) {
            return
        }
        try {
            val data: ByteArray = (str + newline).toByteArray()
            service!!.write(data)
        } catch (e: java.lang.Exception) {
            Log.d("Asu", "send error")
            onSerialIoError(e)
        }
    }

    private fun receive(data: ByteArray) {
        var msg = String(data)
        if (newline == TextUtil.newline_crlf && msg.isNotEmpty()) {
            msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf)
            pendingNewline = msg[msg.length - 1] == '\r'
        }
        receivedMsg += TextUtil.toCaretString(msg, newline.isNotEmpty())
        if (receivedMsg[receivedMsg.length - 1] != '\n') {
            return
        }

        val lastIdx = receivedMsg.indexOf("$") + 1
        receivedMsg = receivedMsg.substring(0, lastIdx)
        lastReceivedMsg = receivedMsg
        receivedMsg = ""

        measureFragment.setLastReceivedMsg(lastReceivedMsg)

        if (reConnectCount != 0) {
            reConnectCount = 0
        }
    }

    private fun alertConnectFail() {
        progressLayout.visibility = View.INVISIBLE
        if (!repeatFlag) return
        AlertUtil.alertOkAndCancel(
            requireContext(),
                getString(R.string.bluetooth_connect_fail),
                getString(R.string.retry),
                getString(R.string.close),
                { _, _ -> clearFragmentStack() }
        ) { _, _ ->
            reConnectCount = 0
            reConnectRepeat()
            repeatFlag = true
        }

        repeatFlag = false
    }

    private fun reConnectRepeat() {
        if (reConnectCount < MAX_RECONNECT_COUNT) {
            GlobalScope.async(Dispatchers.Main) {
                delay(100)
                reConnect()
            }
        } else {
            alertConnectFail()
        }
    }

    private fun reConnect() {
        if (service != null) {
            progressLayout.visibility = View.VISIBLE
            requireActivity().runOnUiThread { connect() }
        }
        reConnectCount++
    }


    private fun addBackButtonEvent() {
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.action == KeyEvent.ACTION_UP) {
                measureFragment.alertEnd(true)
                true
            } else false
        }
    }

    private fun clearFragmentStack() {
        fragmentManager!!.popBackStack(
                "FragmentBluetoothDevices",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        fragmentManager!!.popBackStack()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: MapEvent) {
        val map = event.map

        val fragmentMeasure = map.getOrDefault(FragmentMeasure.toString(), null)
        if (fragmentMeasure != null) {
            val clearFragmentStack = map.getOrDefault("clearFragmentStack", null)

            if (clearFragmentStack != null) {
                clearFragmentStack()
            }

            val notConnect = map.getOrDefault("notConnect", null)
            if (notConnect != null) {
                if (service != null) {
                    progressLayout.visibility = View.VISIBLE
                    requireActivity().runOnUiThread {
                        connect()
                    }
                }
            }

            val addBackButtonEvent = map.getOrDefault("addBackButtonEvent", null)
            if (addBackButtonEvent != null) {
                addBackButtonEvent()
            }
        }

    }
}