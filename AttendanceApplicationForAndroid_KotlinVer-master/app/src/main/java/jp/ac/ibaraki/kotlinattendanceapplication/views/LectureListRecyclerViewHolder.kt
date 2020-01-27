package jp.ac.ibaraki.kotlinattendanceapplication.views

import kotlinx.android.synthetic.main.item_lecture_list.view.*

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_lecture_list.view.*

/**
 * レイアウトのidと繋がってるっぽい？
 */
class LectureListRecyclerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val lectureNameTextView: TextView = view.text_lecture_name
    val teacherNameTextView: TextView = view.text_teacher_name
    val roomNameTextView: TextView = view.text_room_name
}