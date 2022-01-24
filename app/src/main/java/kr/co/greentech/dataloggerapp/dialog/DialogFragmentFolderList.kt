package kr.co.greentech.dataloggerapp.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.review.item.DirectoryItem
import kr.co.greentech.dataloggerapp.util.FileUtil
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerItemClickListener
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.file.FileItem
import java.io.File
import java.io.InputStream

class DialogFragmentFolderList: DialogFragment() {

    companion object {
        fun newInstance(): DialogFragmentFolderList {
            return DialogFragmentFolderList()
        }
    }

    lateinit var adapter: RecyclerViewAdapter
    private lateinit var fileUtil: FileUtil
    
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var directoryLayout: LinearLayout
    private lateinit var vwEmpty: TextView
    private lateinit var scrollview: HorizontalScrollView
    private lateinit var okButton: Button
    private lateinit var floatingBtn: FloatingActionButton

    private var currentPath: String? = null
    private val directoryItemList = ArrayList<DirectoryItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_folder_list, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        rootLayout = view.findViewById(R.id.root_layout)
        directoryLayout = view.findViewById(R.id.scrollLayout)
        scrollview = view.findViewById(R.id.scrollview)
        vwEmpty = view.findViewById<TextView>(R.id.tv_empty)
        okButton = view.findViewById(R.id.btn_select)
        floatingBtn = view.findViewById(R.id.btn_float)

        val homeIcon = view.findViewById<ImageButton>(R.id.btn_home)
        homeIcon.setOnClickListener {
            clickDirectoryIcon(fileUtil.getRootFilePath())
        }

        val backIcon = view.findViewById<ImageButton>(R.id.btn_back)
        backIcon.setOnClickListener {
            if (currentPath != null) {
                goSuperDirectory(currentPath!!)
            }
        }

        fileUtil = FileUtil()
        val list = fileUtil.getFileList()
        if (list.isEmpty()) {
            val assetManager : AssetManager = resources.assets
            val inputStream: InputStream = assetManager.open("test.csv")
            val inputString = inputStream.bufferedReader().use { it.readText() }

            fileUtil.setFileName("test")
            fileUtil.saveFile(inputString)
            inputStream.close()
        }

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        activity,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                val file = (adapter.list[position] as FileItem).file
                                onListItemClick(file, position)
                            }

                            override fun onItemLongClick(view: View?, position: Int) {

                            }
                        }
                )
        )

        adapter = RecyclerViewAdapter(this, ArrayList())
        clickDirectoryIcon(currentPath)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        okButton.setOnClickListener {
            if(currentPath != null) {
                val map = MapEvent(HashMap())
                map.map[DialogFragmentFolderList.toString()] = DialogFragmentFolderList.toString()
                map.map["path"] = currentPath!!
                GlobalBus.getBus().post(map)
            }
            dismiss()
        }

        floatingBtn.setOnClickListener {
            createFolderAlert(requireContext(), currentPath!!)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (size.x * 0.9).toInt()
        params?.height = (size.y * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    private fun clickDirectoryIcon(directory: String? = null) {
        val path = directory ?: fileUtil.getFilePath()
        currentPath = path
        directoryChange(path)
        val list = fileUtil.getFileList(path)
        val fileItemList = getFileItemList(list).filter { it.file.isDirectory }
        adapter.list = fileItemList as ArrayList<Any>
        adapter.notifyDataSetChanged()

        scrollview.post {
            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }

        if (list.isEmpty()) {
        } else {
            vwEmpty.visibility = View.INVISIBLE
        }
    }

    private fun directoryChange(directory: String) {
        for (item in directoryItemList) {
            directoryLayout.removeView(item.layout)
        }

        directoryItemList.clear()

        var path = directory
        val rootPath = fileUtil.getRootFilePath()

        path = path.replace(rootPath, "")
        if (path.isNotEmpty()) {
            if (path[0] == '/') {
                path = path.substring(1, path.length)
            }
        } else {
            return
        }

        val list = path.split("/")

        var pathTotal = fileUtil.getRootFilePath()

        for (idx in list.indices) {
            pathTotal += "/${list[idx]}"

            val inflater = LayoutInflater.from(requireContext())
            val v = inflater.inflate(R.layout.list_directory, null, false)
            val rootLayout = v.findViewById<LinearLayout>(R.id.root_layout)

            if (idx + 1 < list.size) {
                val p = pathTotal
                v.setOnClickListener {
                    clickDirectoryIcon(p)
                }
            }

            val tv = v.findViewById<TextView>(R.id.tv)
            tv.text = list[idx]

            val linearParams = LinearLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            v.layoutParams = linearParams

            directoryLayout.addView(v)
            directoryItemList.add(DirectoryItem(rootLayout, pathTotal))
        }
    }

    private fun getFileItemList(fileList: ArrayList<File>): ArrayList<FileItem> {
        val list = ArrayList<FileItem>()

        for(file in fileList) {
            list.add(FileItem(file, false))
        }

        return list
    }

    private fun goSuperDirectory(path: String) {
        val rootPath = fileUtil.getRootFilePath()
        val lastIdx = path.lastIndexOf("/")
        val superPath = path.substring(0, lastIdx)

        if (superPath.contains(rootPath)) {
            clickDirectoryIcon(superPath)
        }
    }

    fun onListItemClick(file: File, position: Int) {
        when {
            file.isDirectory -> clickDirectoryIcon(file.absolutePath)
            else -> { }
        }
    }

    private fun createFolderAlert(context: Context, path: String) {
        val bobTheBuilder = AlertDialog.Builder(context)
        bobTheBuilder.setView(R.layout.dialog_rename).setTitle(getString(R.string.create_folder))
        val alert = bobTheBuilder.create()
        alert.show()
        val editText = alert.findViewById<EditText>(R.id.renameText)
        val ok = alert.findViewById<Button>(R.id.ok)
        val cancel = alert.findViewById<Button>(R.id.cancel)
        ok.setOnClickListener {
            fileUtil.createDir(context, "${path}/${editText.text}")
            clickDirectoryIcon(path)
            alert.cancel()
        }
        cancel.setOnClickListener { alert.cancel() }
    }
}