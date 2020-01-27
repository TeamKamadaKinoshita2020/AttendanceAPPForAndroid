package jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClient

import jp.ac.ibaraki.kotlinattendanceapplication.Constants
import jp.ac.ibaraki.kotlinattendanceapplication.api.response.ResultDataResponse
import jp.ac.ibaraki.kotlinattendanceapplication.data.ResultData
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query
import rx.Observable

interface SendAttendApiClient {
    @FormUrlEncoded
    @POST(Constants.SEND_ATTEND_URL)
    fun sendAttend(@Field("h_id") h_id: String,
                   @Field("u_id") u_id: String,
                   @Field("c_id") c_id: String): Observable<ResultDataResponse>
}