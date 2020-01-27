package jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClient

import jp.ac.ibaraki.kotlinattendanceapplication.Constants
import jp.ac.ibaraki.kotlinattendanceapplication.api.response.ResultDataResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

interface CheckEmptySeatApiClient {
    @FormUrlEncoded
    @POST(Constants.CHECK_EMPTY_SEAT_URL)
    fun checkEmptySeat(
        @Field("h_id") h_id: String,
        @Field("c_id") c_id: String
    ): Observable<ResultDataResponse>
}