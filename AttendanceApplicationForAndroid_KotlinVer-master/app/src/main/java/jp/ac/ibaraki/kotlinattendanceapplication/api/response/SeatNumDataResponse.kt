package jp.ac.ibaraki.kotlinattendanceapplication.api.response

import java.io.Serializable

data class SeatNumDataResponse(
    var seatNum: String? = null
) : Serializable