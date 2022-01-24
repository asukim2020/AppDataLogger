package kr.co.greentech.dataloggerapp.fragment.review.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.res.AssetManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.review.item.DirectoryItem
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.FileUtil
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerItemClickListener
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.file.FileItem
import java.io.File
import java.io.InputStream


class FragmentGraphReviewFileList: Fragment() {

    companion object {
        fun newInstance(): FragmentGraphReviewFileList {
            return FragmentGraphReviewFileList()
        }
    }

    lateinit var adapter: RecyclerViewAdapter
    private lateinit var fileUtil: FileUtil

    private lateinit var rootLayout: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressLayout: FrameLayout
    private lateinit var directoryLayout: LinearLayout
    private lateinit var vwEmpty: TextView
    private lateinit var scrollview: HorizontalScrollView
    private var editLayout: LinearLayout? = null
    private var renameLayout: ConstraintLayout? = null
    private var renameButton: ImageButton? = null
    private var renameText: TextView? = null
    private var shareButton: ImageButton? = null
    private var shareText: TextView? = null
    private var pasteLayout: LinearLayout? = null
    private var currentPath: String? = null
    private var selectFileList = ArrayList<File>()
    private var selectFileItemList = ArrayList<FileItem>()
    private var scrollPositionMap = HashMap<String, Int>()

    private val directoryItemList = ArrayList<DirectoryItem>()

    var settingFlag = false
    private var pasteFlag = false

    private var pasteTypeFlag: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.fragment_review, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        progressLayout = view.findViewById(R.id.progress_layout)
        rootLayout = view.findViewById(R.id.root_layout)
        directoryLayout = view.findViewById(R.id.scrollLayout)
        scrollview = view.findViewById(R.id.scrollview)
        vwEmpty = view.findViewById<TextView>(R.id.tv_empty)

        val homeIcon = view.findViewById<ImageButton>(R.id.btn_home)
        homeIcon.setOnClickListener {
            clickDirectoryIcon(fileUtil.getRootFilePath())
        }

        val backIcon = view.findViewById<ImageButton>(R.id.btn_back)
        backIcon.setOnClickListener {
            if (currentPath != null) {
                if (settingFlag && !pasteFlag) {
                    clickBackButton()
                } else {
                    goSuperDirectory(currentPath!!)
                }
            }
        }

        fileUtil = FileUtil()
        var list = fileUtil.getFileList()
        if (list.isEmpty()) {
            val assetManager : AssetManager = resources.assets
            val inputStream: InputStream = assetManager.open("test.csv")
            val inputString = inputStream.bufferedReader().use { it.readText() }

            fileUtil.setFileName("test")
            fileUtil.saveFile(inputString)
            inputStream.close()
        }

        adapter = RecyclerViewAdapter(this, ArrayList())

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
                        val file = (adapter.list[position] as FileItem).file
                        onListItemLongClick(file, position)
                    }
                }
            )
        )

        clickDirectoryIcon(currentPath)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        (activity as ActivityStart?)!!.setTitle(getString(R.string.select_file))
        super.onActivityCreated(savedInstanceState)

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.action == KeyEvent.ACTION_UP) {
                clickBackButton()
                true
            } else false
        }
    }

    private fun clickBackButton() {
        if (settingFlag) {
            settingFlag = false
            pasteFlag = false
            val actionBar = (activity as ActivityStart).supportActionBar
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24)
                val color = DataLogApplication.getColor(R.color.colorPrimary)
                actionBar.setBackgroundDrawable(ColorDrawable(color))
            }
            changeEdit(false)
            selectFileItemUIClear()
            selectFileItemList.clear()
        } else {
            if (currentPath != null) {
                val removePosition = scrollPositionMap.remove(currentPath)
                Log.d("Asu", "remove position: $removePosition, path: ${currentPath!!}")
                if (settingFlag && !pasteFlag) {
                    clickBackButton()
                } else {
                    if (fileUtil.getRootFilePath() != currentPath
                            && fileUtil.getFilePath() != currentPath) {
                        goSuperDirectory(currentPath!!)
                    } else {
                        fragmentManager?.popBackStack()
                    }
                }
            }
        }
    }

    private fun selectFileItemUIClear() {
        val list = adapter.list as ArrayList<FileItem>
        for (item in list) {
            item.isOn = false
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_file_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                if (activity != null) {
                    clickBackButton()
                }
            }

            R.id.create_folder -> {
                if (currentPath != null) {
                    createFolderAlert(requireContext(), currentPath!!)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun onListItemClick(file: File, position: Int) {
        if (settingFlag && !pasteFlag) {
            itemSelect(position)
        } else {
            val name = file.name.toLowerCase()
            when {
                file.isDirectory -> clickDirectoryIcon(file.absolutePath)
                name.endsWith(".csv") -> {
                    val fragment: Fragment = FragmentGraphReview.newInstance(file)
                    fragmentManager!!.beginTransaction().replace(
                        R.id.fragment,
                        fragment,
                        "FragmentGraphReview"
                    ).addToBackStack(null).commit()
                }
                else -> {
                    fileUtil.openFile(requireContext(), file)
                }
            }
        }
    }

    fun onListItemLongClick(file: File, position: Int) {
        if (!settingFlag) {
            settingFlag = true
            val actionBar = (activity as ActivityStart).supportActionBar
            if(actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24)
                val color = DataLogApplication.getColor(R.color.edit)
                actionBar.setBackgroundDrawable(ColorDrawable(color))
            }
            changeEdit(true)
            itemSelect(position)
        }
    }

    private fun itemSelect(position: Int) {
        val item = (adapter.list[position] as FileItem)
        item.isOn = !item.isOn
        if (item.isOn) {
            if (!selectFileItemList.contains(item)) {
                selectFileItemList.add(item)
                if (selectFileItemList.size > 1) {
                    renameButton?.alpha = 0.3F
                    renameText?.alpha = 0.3F
                    renameLayout?.isClickable = false
                }

                if (item.file.isDirectory) {
                    shareButton?.alpha = 0.3F
                    shareText?.alpha = 0.3F
                }
            }
        } else {
            if (selectFileItemList.contains(item)) {
                selectFileItemList.remove(item)

                if (selectFileItemList.size <= 1) {
                    renameButton?.alpha = 1.0F
                    renameText?.alpha = 1.0F
                    renameLayout?.isClickable = true
                }

                var flag = true
                for (item in selectFileItemList) {
                    if (item.file.isDirectory) {
                        flag = false
                        break
                    }
                }

                if (flag) {
                    shareButton?.alpha = 1.0F
                    shareText?.alpha = 1.0F
                }
            }

            if (selectFileItemList.isEmpty()) {
                clickBackButton()
            }
        }

        adapter.notifyItemChanged(position)
    }

    private fun changeEdit(flag: Boolean) {
        if (flag) {
            val inflater = LayoutInflater.from(requireContext())
            val v = inflater.inflate(R.layout.fragment_review_edit, null, false)
            val editLayout = v.findViewById<LinearLayout>(R.id.root_layout)
            this.editLayout = editLayout
            rootLayout.addView(editLayout)

            val btnCopy = v.findViewById<ConstraintLayout>(R.id.btn_copy)
            val btnMove = v.findViewById<ConstraintLayout>(R.id.btn_cut)
            val btnRename = v.findViewById<ConstraintLayout>(R.id.btn_edit)
            renameLayout= btnRename
            renameButton = v.findViewById(R.id.ib_edit)
            renameText = v.findViewById(R.id.tv_edit)
            val btnShard = v.findViewById<ConstraintLayout>(R.id.btn_shard)
            shareButton = v.findViewById(R.id.ib_shard)
            shareText = v.findViewById(R.id.tv_shard)
            val btnDelete = v.findViewById<ConstraintLayout>(R.id.btn_delete)

            btnCopy.setOnClickListener {
                pasteTypeFlag = true
                getSelectFileList()
                changePaste()
            }

            btnMove.setOnClickListener {
                pasteTypeFlag = false
                getSelectFileList()
                changePaste()
            }

            btnRename.setOnClickListener {
                getSelectFileList()
                renameAlert(requireContext(), selectFileItemList.first().file)
            }

            btnShard.setOnClickListener {
                val selectFileList = getSelectFileList()
                fileUtil.shareFileList(requireContext(), selectFileList)
            }

            btnDelete.setOnClickListener {
                val selectFileList = getSelectFileList()
                deleteAlert(requireContext(), selectFileList)
                clickBackButton()
                clickDirectoryIcon(currentPath)
            }

            val editParam = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                CalculatorUtil.dpToPx(60.0F)
            )
            editLayout.layoutParams = editParam

            val set = ConstraintSet()
            set.clone(rootLayout)
            set.connect(editLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            set.connect(recyclerView.id, ConstraintSet.BOTTOM, editLayout.id, ConstraintSet.TOP)
            set.applyTo(rootLayout)
        } else {
            val set = ConstraintSet()
            set.clone(rootLayout)
            set.connect(recyclerView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            set.applyTo(rootLayout)

            if (editLayout != null) {
                rootLayout.removeView(editLayout)
                editLayout = null
                renameLayout = null
                renameButton = null
                renameText = null
                shareButton = null
                shareText = null
            }

            if (pasteLayout != null) {
                rootLayout.removeView(pasteLayout)
                pasteLayout = null
            }
        }
    }

    private fun changePaste() {
        pasteFlag = true
        val inflater = LayoutInflater.from(requireContext())
        val v = inflater.inflate(R.layout.fragment_review_paste, null, false)
        val pasteLayout = v.findViewById<LinearLayout>(R.id.root_layout)
        this.pasteLayout = pasteLayout
        rootLayout.addView(pasteLayout)

        val btnCancel = v.findViewById<ConstraintLayout>(R.id.btn_clear)
        val btnPaste = v.findViewById<ConstraintLayout>(R.id.btn_paste)

        btnCancel.setOnClickListener {
            clickBackButton()
        }

        btnPaste.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            if (pasteTypeFlag) {
                GlobalScope.async(Dispatchers.Main) {
                    if (currentPath != null) {
                        fileUtil.copyDir(requireContext(), selectFileList, currentPath!!)
                        clickDirectoryIcon(currentPath)
                    }
                    clickBackButton()
                    progressLayout.visibility = View.INVISIBLE
                }
            } else {
                GlobalScope.async(Dispatchers.Main) {
                    if (currentPath != null) {
                        fileUtil.moveDir(requireContext(), selectFileList, currentPath!!)
                        clickDirectoryIcon(currentPath)
                    }
                    clickBackButton()
                    progressLayout.visibility = View.INVISIBLE
                }
            }
        }

        val pasteParam = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                CalculatorUtil.dpToPx(60.0F)
        )
        pasteLayout.layoutParams = pasteParam

        val set = ConstraintSet()
        set.clone(rootLayout)
        set.connect(pasteLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        set.connect(recyclerView.id, ConstraintSet.BOTTOM, pasteLayout.id, ConstraintSet.TOP)
        set.applyTo(rootLayout)

        if (editLayout != null) {
            rootLayout.removeView(editLayout)
            editLayout = null
            renameLayout = null
            renameButton = null
            renameText = null
            shareButton = null
            shareText = null
        }

        selectFileItemUIClear()
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

    private fun clickDirectoryIcon(directory: String? = null) {
        val path = directory ?: fileUtil.getFilePath()

        if (adapter.list.isNotEmpty() && currentPath != null) {
            val position = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
            scrollPositionMap[currentPath!!] = position

            Log.d("Asu", "insert position: $position, path: ${currentPath!!}")
        }

        currentPath = path
        directoryChange(path)
        val list = fileUtil.getFileList(path)
        val fileItemList = getFileItemList(list)
        adapter.list = fileItemList as ArrayList<Any>
        adapter.notifyDataSetChanged()

        val position = scrollPositionMap[path]
        if (position != null) {
            recyclerView.scrollToPosition(position)
            Log.d("Asu", "get position: $position, path: ${currentPath!!}")
        } else {
            recyclerView.scrollToPosition(0)
        }

        scrollview.post {
            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }

        if (list.isEmpty()) {
        } else {
            vwEmpty.visibility = View.INVISIBLE
        }
    }

    private fun getSelectFileList(): ArrayList<File>{
        val selectFileList = ArrayList<File>()

        val fileItemList = adapter.list as ArrayList<FileItem>
        val selectFileItemList = fileItemList.filter { it.isOn }

        for (item in selectFileItemList) {
            selectFileList.add(item.file)
        }

        this.selectFileList = selectFileList
        return selectFileList
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

    private fun deleteAlert(context: Context, fileList: ArrayList<File>) {
        AlertUtil.alertOkAndCancelCancelable(
                context,
                context.getString(R.string.delete_msg),
                context.getString(R.string.delete)
        ) { _, _ ->
            progressLayout.visibility = View.VISIBLE
            GlobalScope.async(Dispatchers.Main) {
                (fileUtil.deleteFileList(context, fileList))
                clickDirectoryIcon(currentPath)
                progressLayout.visibility = View.INVISIBLE
            }
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
            if (settingFlag && !pasteFlag) {
                clickBackButton()
            }
            alert.cancel()
        }
        cancel.setOnClickListener { alert.cancel() }
    }

    private fun renameAlert(context: Context, file: File) {
        val bobTheBuilder = AlertDialog.Builder(context)
        bobTheBuilder.setView(R.layout.dialog_rename).setTitle(getString(R.string.edit_name))
        val alert = bobTheBuilder.create()
        alert.show()
        val editText = alert.findViewById<EditText>(R.id.renameText)
        val ok = alert.findViewById<Button>(R.id.ok)
        val cancel = alert.findViewById<Button>(R.id.cancel)
        editText.setText(file.name)

        ok.setOnClickListener {
            fileUtil.editName(context, file, editText.text.toString())
            clickDirectoryIcon(currentPath)
            if (settingFlag) {
                clickBackButton()
            }
            alert.cancel()
        }
        cancel.setOnClickListener { alert.cancel() }
    }
}