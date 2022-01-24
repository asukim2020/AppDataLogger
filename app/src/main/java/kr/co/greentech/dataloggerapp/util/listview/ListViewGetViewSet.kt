package kr.co.greentech.dataloggerapp.util.listview

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.DataLogSettingType
import kr.co.greentech.dataloggerapp.fragment.datalog.item.DataLogSettingItem
import kr.co.greentech.dataloggerapp.fragment.menu.enums.StartMenu
import kr.co.greentech.dataloggerapp.fragment.menu.item.MenuItem
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.EqualIntervalSaveSettingType
import kr.co.greentech.dataloggerapp.fragment.savesetting.item.SaveSettingItem
import kr.co.greentech.dataloggerapp.realm.RealmDataLog
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.util.AdapterUtil
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.extension.addSeparator

object ListViewGetViewSet {

    fun dataLogSettingGetView(
            position: Int,
            convertView: View?,
            parent: ViewGroup,
            list: ArrayList<DataLogSettingItem>,
            item: RealmDataLog
    ): View {
        val context = parent.context

        lateinit var view: View

        view = if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.list_item_spinner, parent, false)
        } else {
            convertView
        }

        if (convertView == null) {
            view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
        }

        val title = view.findViewById<TextView>(R.id.name)
        title.text = list[position].item.getTitle()

        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val adapter: ArrayAdapter<String> = AdapterUtil.getSpinnerAdapter(
                context,
                spinner,
                list[position].item.getItems()
        )
        spinner.adapter = adapter
        spinner.onItemSelectedListener = list[position].onItemSelectedListener!!

        when (list[position].item) {
            DataLogSettingType.STRAIN_GAGE_RANGE -> {
                spinner.setSelection(item.strainGageRange)
                list[position].selectItemPosition = item.strainGageRange
            }

            DataLogSettingType.SENSOR_GAGE_RANGE -> {
                spinner.setSelection(item.sensorGageRange)
                list[position].selectItemPosition = item.sensorGageRange
            }
        }

        return view
    }

    fun menuGetView(
            position: Int,
            convertView: View?,
            parent: ViewGroup,
            list: ArrayList<MenuItem>
    ): View {
        val context = parent.context

        lateinit var view: View

        view = if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.list_item_menu, parent, false)
        } else {
            convertView
        }

        val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)
        val ivIcon: ImageView = view.findViewById(R.id.icon) as ImageView
        val title = view.findViewById(R.id.title) as TextView
        val subTitle = view.findViewById(R.id.subtitle) as TextView

        val item = list[position]

        rootLayout.addSeparator(20.0F)

        ivIcon.setImageDrawable(item.icon)
        title.text = item.title
        subTitle.text = item.subtitle

        if (item.title == StartMenu.CHANNEL_SETTING.getTitle(context)) {
            val padding = CalculatorUtil.dpToPx(11.0f)
            ivIcon.setPadding(padding, padding, padding, padding)
        }

        return view
    }

    fun equalIntervalGetView(
            position: Int,
            convertView: View?,
            parent: ViewGroup,
            list: ArrayList<SaveSettingItem>,
            item: RealmSaveSetting
    ): View {
        val context = parent.context

        lateinit var view: View

        view = if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            when(list[position].item) {
                EqualIntervalSaveSettingType.INTERVAL -> inflater.inflate(R.layout.list_item_edit_text, parent, false)
            }
        } else {
            convertView
        }

        if (convertView == null) {
            view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
        }

        val title = view.findViewById<TextView>(R.id.name)
        title.text = list[position].item.getTitle(context)

        when(list[position].item) {
            EqualIntervalSaveSettingType.INTERVAL -> {
                val tvEdit = view.findViewById<EditText>(R.id.tv_edit)
                tvEdit.removeTextChangedListener(list[position].textWatcher)

                if (list[position].editText == "") {
                    list[position].editText = item.interval.toString()
                }

                tvEdit.setText(list[position].editText)
                tvEdit.addTextChangedListener(list[position].textWatcher)
                tvEdit.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }

        return view
    }
}