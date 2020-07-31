package com.omejibika.endoscope

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class InfiniteScrollListener(gridLayoutManager: GridLayoutManager?, listener: OnLoadMoreListener): RecyclerView.OnScrollListener() {
    private val VISIBLE_THRESHOLD = 2
    private var gridLayoutManager: GridLayoutManager? = null
    private var loading // LOAD MORE Progress dialog
            = false
    private var listener: OnLoadMoreListener? = null
    private var pauseListening = false


    private var END_OF_FEED_ADDED = false
    private val NUM_LOAD_ITEMS = 10

    init {
        this.gridLayoutManager = gridLayoutManager
        this.listener = listener
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dx == 0 && dy == 0) return
        val totalItemCount = gridLayoutManager!!.itemCount
        val lastVisibleItem = gridLayoutManager!!.findLastVisibleItemPosition()
        if (!loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD && totalItemCount != 0 && !END_OF_FEED_ADDED && !pauseListening) {
            if (listener != null) {
                listener!!.onLoadMore()
            }
            loading = true
        }
    }

    fun setLoaded() {
        loading = false
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    fun addEndOfRequests() {
        END_OF_FEED_ADDED = true
    }

    fun pauseScrollListener(pauseListening: Boolean) {
        this.pauseListening = pauseListening
    }
}