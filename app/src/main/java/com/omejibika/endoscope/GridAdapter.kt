package com.omejibika.endoscope

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.omejibika.endoscope.databinding.GridItemsBinding
import com.squareup.picasso.Picasso
import java.io.File
import java.net.URI

class GridAdapter(val dataList: ArrayList<GridDataList>): RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

    /**
     * 新たに作成した独自ViewHolderをインスタンス化し作成
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        return GridViewHolder(GridItemsBinding.inflate(LayoutInflater.from(parent.context), parent,false)).apply {        }
    }

    /**
     * 各セルの処理
     */
    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val data = dataList[position]
        holder.binding.data = data
        var bitmap = BitmapFactory.decodeFile(data.image)
        if (bitmap.byteCount > 10000000) {
            // 画像のファイルサイズが1MBを超える場合、画面に表示する画像をリサイズ
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
        }

        var uri = Common.getImageUri(holder.itemView.context, bitmap, 10)
        var rotate = Common.getCameraPhotoOrientation(holder.itemView.context, uri, data.image)
        Picasso.get().load(uri).resize(200, 200).noFade().rotate(rotate.toFloat()).centerCrop().into(holder.binding.image)
        holder.binding.filenameText.text = data.filename
        holder.binding.container.setBackgroundColor(if (dataList[position].isSelected) Color.CYAN else Color.WHITE)
        holder.binding.container.setOnClickListener {
            /*** Itemをクリックしたときの処理 ***/
            val context = holder.itemView.context
            val intent = Intent(context, DetailPagerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("data", dataList)
            context.startActivity(intent)
        }
        holder.binding.container.setOnLongClickListener {
            /*** Itemをロングクリックしたときの処理 ***/
            dataList[position].isSelected = !dataList[position].isSelected
            holder.binding.container.setBackgroundColor(if (dataList[position].isSelected) Color.CYAN else Color.WHITE)
            true
        }
    }

    /**
     * Adapterにセットされるのアイテムのデータの総数
     */
    override fun getItemCount(): Int {
        return dataList.size
    }

    class GridViewHolder(val binding: GridItemsBinding): RecyclerView.ViewHolder(binding.root)
}