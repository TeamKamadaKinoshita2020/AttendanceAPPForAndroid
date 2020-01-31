package jp.ac.ibaraki.kotlinattendanceapplication

/**
 * アプリケーションで使用する定数群をまとめたクラスファイル
 */
class Constants {
    /**
     * 通信関連の定数
     */
    companion object {
        const val BASE_URL = "hoge" // "http:/IP address/attendancesystem/android/"
        const val GET_POSSIBLE_ATTEND_URL = "get-possible-attend-list.php/"
        const val CHECK_ALREADY_ATTEND_URL = "check-already-attend.php/"
        const val GET_SEAT_NUM_URL = "get-seat-num.php/"
        const val CHECK_EMPTY_SEAT_URL = "check-empty-seat.php/"
        const val SEND_ATTEND_URL = "send-attendance.php/"
    }
}
