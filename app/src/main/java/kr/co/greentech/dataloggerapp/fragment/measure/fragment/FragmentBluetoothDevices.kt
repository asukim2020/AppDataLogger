package kr.co.greentech.dataloggerapp.fragment.measure.fragment

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.util.extension.addSeparator
import java.util.*

class FragmentBluetoothDevices : ListFragment() {

    companion object {
        fun newInstance(): FragmentBluetoothDevices {
            return FragmentBluetoothDevices()
        }
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val listItems = ArrayList<BluetoothDevice>()
    private var listAdapter: ArrayAdapter<BluetoothDevice>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        listAdapter = object : ArrayAdapter<BluetoothDevice>(requireActivity(), 0, listItems) {
            override fun getView(position: Int, view: View?, parent: ViewGroup): View {
                var v = view
                val device = listItems[position]
                if (v == null) {
                    v = requireActivity().layoutInflater.inflate(R.layout.list_item_device, parent, false)
                    v.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                }
                val title = v!!.findViewById<TextView>(R.id.title)
                val subTitle = v.findViewById<TextView>(R.id.subtitle)
                title.text = device.name
                subTitle.text = device.address
                return v
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as ActivityStart?)!!.setTitle(getString(R.string.bluetooth_devices))
        listView.divider = null
        setListAdapter(null)
        setEmptyText("")
        val tv = listView.emptyView as TextView
        tv.textSize = 16f
        tv.setTextColor(DataLogApplication.getColor(R.color.lightFont))
        setListAdapter(listAdapter)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_devices, menu)
        if (bluetoothAdapter == null) menu.findItem(R.id.bt_settings).isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothAdapter == null)
            setEmptyText(getString(R.string.bluetooth_not_available))
        else if (!bluetoothAdapter!!.isEnabled)
            setEmptyText(getString(R.string.bluetooth_not_connect))
        else
            setEmptyText(getString(R.string.bluetooth_devices_not_exist))
        refresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.bt_settings) {
            val intent = Intent()
            intent.action = Settings.ACTION_BLUETOOTH_SETTINGS
            startActivity(intent)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun refresh() {
        listItems.clear()
        if (bluetoothAdapter != null) {
            for (device in bluetoothAdapter!!.bondedDevices) if (device.type != BluetoothDevice.DEVICE_TYPE_LE) listItems.add(device)
        }
        listItems.sortWith(Comparator { a: BluetoothDevice, b: BluetoothDevice -> compareTo(a, b) })
        listAdapter!!.notifyDataSetChanged()
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val device = listItems[position]
        val args = Bundle()
        args.putString("device", device.address)
        val fragment: Fragment = FragmentBluetoothMeasure.newInstance()
        fragment.arguments = args
        fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment, "FragmentBluetoothDevices").addToBackStack("FragmentBluetoothDevices").commit()
    }

    private fun compareTo(a: BluetoothDevice, b: BluetoothDevice): Int {
        val aValid = a.name != null && a.name.isNotEmpty()
        val bValid = b.name != null && b.name.isNotEmpty()
        if (aValid && bValid) {
            val ret = a.name.compareTo(b.name)
            return if (ret != 0) ret else a.address.compareTo(b.address)
        }
        if (aValid) return -1
        return if (bValid) +1 else a.address.compareTo(b.address)
    }
}