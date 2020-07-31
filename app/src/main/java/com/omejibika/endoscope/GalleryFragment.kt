package com.omejibika.endoscope

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GalleryFragment : Fragment() {

    /**
     * Fragment生成時にシステムが呼び出す
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    /**
     * Fragmentが初めてUIを描画する時にシステムが呼び出す
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        /** Inflate the layout for this fragment **/
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    /**
     * Viewの生成が完了した後に呼ばれる、UIパーツの設定などを行う
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = GridAdapter(createDataList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(
            activity, 2, GridLayoutManager.VERTICAL, false
        )
        recyclerView.addItemDecoration(
            GridItemDecoration(resources.getDimension(R.dimen.carousel_margin).toInt())
        )
        recyclerView.setHasFixedSize(true)
    }

    private fun createDataList(): ArrayList<GridDataList> {
        val gridDataList = mutableListOf<GridDataList>()

        /** 指定したフォルダ内のファイルを取得 **/
        val pref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        var storage = pref?.getString("storage", "1")
        var directoryPath = pref?.getString("directorypath", "")
        if (directoryPath.equals("")) {
            if (storage.equals("1")) {
                // SDカード
                directoryPath = Common.getSdCardFilesDirPathListForLollipop(requireActivity().applicationContext)[0]
            } else {
                // 内部ストレージ
                directoryPath = Environment.getExternalStorageDirectory().path
            }
            val editor = pref?.edit()
            editor?.putString("directorypath", directoryPath)
            editor?.apply()
        }
        var dir = File(directoryPath)
        var files = dir.listFiles()
        files.forEach {
            if (it.isFile && (it.name.toLowerCase().endsWith(".png") || it.name.toLowerCase().endsWith(".jpg") || it.name.toLowerCase().endsWith(".jpeg") )) {
                val gridData: GridDataList = GridDataList(it.absolutePath, it.name, it.lastModified())
                gridDataList.add(gridData)
            }
        }

        return gridDataList as ArrayList<GridDataList>
    }
}
