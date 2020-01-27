package jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClient

import jp.ac.ibaraki.kotlinattendanceapplication.Constants
import jp.ac.ibaraki.kotlinattendanceapplication.api.response.ResultDataResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

interface CheckAlreadyAttendApiClient {
    @FormUrlEncoded
    @POST(Constants.CHECK_ALREADY_ATTEND_URL)
    fun checkAlreadyAttend(
        @Field("h_id") h_id: String,
        @Field("u_id") u_id: String
    ): Observable<ResultDataResponse>
}