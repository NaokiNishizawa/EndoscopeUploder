package com.omejibika.endoscope

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2

class DetailPagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pager)

        /*** ActionBarに戻るボタンを表示する ***/
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*** Intentされた情報を取得 ***/
        val intent = intent
        val index = intent.getIntExtra("index", 0)
        val dataList: ArrayList<GridDataList> = intent.getSerializableExtra("data") as ArrayList<GridDataList>

        /*** ViewPagerをセット ***/
        var viewPager2 = findViewById<ViewPager2>(R.id.DetailViewPager)
        viewPager2.adapter = DetailPagerRecyclerAdapter(dataList)
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        //viewPager2.currentItem = index
        viewPager2.setCurrentItem(index, false)
    }

    /**
     * ActionBar戻るボタンクリック
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
