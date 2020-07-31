package com.omejibika.endoscope

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class DetailPagerRecyclerAdapter(private val items: List<GridDataList>): RecyclerView.Adapter<PagerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder =
        PagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_detail, parent, false))

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class PagerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val detailImageView: ImageView = itemView.findViewById(R.id.DetailImageView)

    fun bind(item: GridDataList) {
        val dm = Resources.getSystem().displayMetrics
        val bmImg = BitmapFactory.decodeFile(item.image)
        var uri = Common.getImageUri(detailImageView.context, bmImg, 70)
        var rotate = Common.getCameraPhotoOrientation(detailImageView.context, uri, item.image)
        Picasso.get().load(uri).resize(dm.widthPixels,0).noFade().rotate(rotate.toFloat()).centerInside().into(detailImageView)
    }
}