package jp.ac.ibaraki.kotlinattendanceapplication.views

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.ac.ibaraki.kotlinattendanceapplication.MainActivity
import jp.ac.ibaraki.kotlinattendanceapplication.R

/**
 * リストのアダプタークラス
 * クリックイベントを呼び出しフラグメントでオーバーライドするのでopenを記述している
 * データバインディングで実装したかったけど挫折(20190703)
 */
open class LectureListRecyclerAdapter(var dataListModel: ArrayList<LectureListDataModel>) : RecyclerView.Adapter<LectureListRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureListRecyclerViewHolder {
        val mView = LayoutInflater.from(parent.context).inflate(R.layout.item_lecture_list, parent, false)
        return LectureListRecyclerViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return dataListModel.count()
    }

    override fun onBindViewHolder(holder: LectureListRecyclerViewHolder, position: Int) {
        val activity: MainActivity = MainActivity()
        holder.lectureNameTextView.text = dataListModel[position].lectureName
        var text = holder.teacherNameTextView.text as String
        holder.teacherNameTextView.text =  text + dataListModel[position].teacherName
        text = holder.roomNameTextView.text as String
        holder.roomNameTextView.text  = text + dataListModel[position].roomName
        holder.itemView.setOnClickListener { recyclerOnClick(it, position) }
    }

    /**
     * リストのクリックイベント　呼び出しフラグメント(ResultFragment)でオーバーライドを行う？
     */
    open fun recyclerOnClick(view: View, position: Int) {
    }

}
