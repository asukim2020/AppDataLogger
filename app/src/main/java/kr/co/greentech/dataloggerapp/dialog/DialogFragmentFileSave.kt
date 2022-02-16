package kr.co.greentech.dataloggerapp.dialog

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.util.*
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import org.greenrobot.eventbus.Subscribe
import java.util.regex.Pattern

class DialogFragmentFileSave: DialogFragment() {

    companion object {
        fun newInstance(): DialogFragmentFileSave {
            return DialogFragmentFileSave()
        }
    }

    lateinit var tvEdit: EditText
    lateinit var switchCompat: SwitchCompat
    private lateinit var folderLayout: LinearLayout
    private lateinit var tvFolder: TextView
    private lateinit var imgFolder: ImageButton
    private var path: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_file_save, container, false)

        switchCompat = view.findViewById<SwitchCompat>(R.id.button_switch)
        tvEdit = view.findViewById<EditText>(R.id.tv_edit)
        val cancel = view.findViewById<Button>(R.id.cancel)
        val start = view.findViewById<Button>(R.id.start)

        folderLayout = view.findViewById<LinearLayout>(R.id.folder_layout)
        tvFolder = view.findViewById(R.id.tv_folder)
        imgFolder = view.findViewById(R.id.img_folder)

        val text = PreferenceManager.getString(PreferenceKey.FILE_NAME_KEEP)
        if (text != null) {
            tvEdit.setText(text)
        }

        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            isCheckedSwitch(isChecked, true)
        }

        val isChecked = PreferenceManager.getBoolean(PreferenceKey.FILE_SAVE_STATUS)
        isCheckedSwitch(isChecked, false)

        cancel.setOnClickListener {
            dismiss()
        }

        start.setOnClickListener {

            val fileName = tvEdit.text.toString()

            val pattern = Pattern.compile("[ !@#$%^&*(),.?/\":{}|<>]")
            val specialCharacterFlag = pattern.matcher(fileName).find()

            val list = if (path != null) {
                FileUtil().getFileList(path!!)
            } else {
                FileUtil().getFileList()
            }

            var flag = false

            for (file in list) {
                if (file.name == "${fileName}.csv") {
                    flag = true
                }
            }

            when {
                specialCharacterFlag -> AlertUtil.alert(requireContext(), getString(R.string.file_char_exception_msg))
                tvEdit.text.toString().isEmpty() && switchCompat.isChecked -> alertInputFileName()
                flag && switchCompat.isChecked -> alertOverwriteFile()
                else -> measureStart()

            }
        }
        
        folderLayout.setOnClickListener {
            val df = DialogFragmentFolderList.newInstance()
            df.show(fragmentManager!!, "DialogFragmentFolderList")
        }

        setPath(FileUtil().getFilePath())
        return view
    }

    override fun onStart() {
        super.onStart()
        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        super.onStop()
        GlobalBus.getBus().unregister(this)
    }

    override fun onResume() {
        super.onResume()

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = CalculatorUtil.dpToPx(300.0F)
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    private fun setPath(path: String) {
        this.path = path
        val lastIdx = path.lastIndexOf("/")
        val folderName = path.substring(lastIdx, path.length)
        tvFolder.text = folderName
    }

    @Subscribe
    public fun onEvent(event: MapEvent) {
        val map = event.map
        if (map[DialogFragmentFolderList.toString()] != null) {
            val path: String? = map["path"] as? String
            if (path != null) {
                setPath(path)
            }
        }
    }

    private fun isCheckedSwitch(isChecked: Boolean, isSave: Boolean) {
        if (isChecked) {
            tvEdit.visibility = View.VISIBLE
            tvFolder.visibility = View.VISIBLE
            imgFolder.visibility = View.VISIBLE
            folderLayout.isEnabled = true
        } else {
            tvEdit.visibility = View.INVISIBLE
            tvFolder.visibility = View.INVISIBLE
            imgFolder.visibility = View.INVISIBLE
            folderLayout.isEnabled = false
        }

        if (isSave) {
            PreferenceManager.setBoolean(PreferenceKey.FILE_SAVE_STATUS, isChecked)
        } else {
            switchCompat.isChecked = isChecked
        }
    }

    private fun measureStart() {
        val fileName = tvEdit.text.toString()
        PreferenceManager.setString(PreferenceKey.FILE_NAME_KEEP, fileName)
        val event = MapEvent(HashMap())
        event.map[DialogFragmentFileSave.toString()] = DialogFragmentFileSave.toString()
        event.map["isOn"] = switchCompat.isChecked
        event.map["fileName"] = fileName
        if (path != null) {
            event.map["path"] = path!!
        }

        GlobalBus.getBus().post(event)

        tvEdit.clearFocus()
        dismiss()
    }

    private fun alertInputFileName() {
        AlertUtil.alert(requireContext(), getString(R.string.save_file_name_input_msg))
    }

    private fun alertOverwriteFile() {
        val fileName = tvEdit.text.toString()
        val str = getString(R.string.save_file_name_overwrite)
        val text = String.format(str, "[${fileName}.csv]")

        AlertUtil.alertOkAndCancel(
            requireContext(),
                text,
                getString(R.string.overwrite)
        ) { _, _ ->
            measureStart()
        }
    }
}