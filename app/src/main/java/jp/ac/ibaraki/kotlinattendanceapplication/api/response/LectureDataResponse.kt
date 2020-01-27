package jp.ac.ibaraki.kotlinattendanceapplication.api.response

import jp.ac.ibaraki.kotlinattendanceapplication.data.LectureData
import java.io.Serializable

data class LectureDataResponse (
    var count: String? = null,
    var results: ArrayList<LectureData> = ArrayList()
): Serializable