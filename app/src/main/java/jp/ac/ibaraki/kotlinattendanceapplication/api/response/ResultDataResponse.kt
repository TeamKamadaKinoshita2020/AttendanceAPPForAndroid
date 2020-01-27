package jp.ac.ibaraki.kotlinattendanceapplication.api.response

import jp.ac.ibaraki.kotlinattendanceapplication.data.ResultData
import java.io.Serializable

data class ResultDataResponse (
    var result: Boolean? = null
): Serializable