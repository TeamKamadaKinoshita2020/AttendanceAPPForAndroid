package jp.ac.ibaraki.kotlinattendanceapplication.data

data class LectureData(
    // 変数名をJSONのキー値に合わせる
    var text: String? = null, // Lecture name 講義名
    var rep_name: String? = null, // teacher name 教員名
    var room_name: String? = null, // 教室名
    var holding_id: String? = null // 講義の固有ID
)