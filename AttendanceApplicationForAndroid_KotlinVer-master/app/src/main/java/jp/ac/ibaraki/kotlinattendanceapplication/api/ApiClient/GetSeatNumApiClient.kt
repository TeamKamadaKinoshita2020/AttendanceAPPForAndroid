package jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClient

import jp.ac.ibaraki.kotlinattendanceapplication.Constants
import jp.ac.ibaraki.kotlinattendanceapplication.api.response.SeatNumDataResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

interface GetSeatNumApiClient {
    @FormUrlEncoded
    @POST(Constants.GET_SEAT_NUM_URL)
    fun getSeatNum(
        @Field("c_id") c_id: String,
        @Field("h_id") h_id: String
    ): Observable<SeatNumDataResponse>
}