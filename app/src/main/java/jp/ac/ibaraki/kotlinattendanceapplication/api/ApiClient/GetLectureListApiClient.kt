package jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClient

import jp.ac.ibaraki.kotlinattendanceapplication.Constants
import jp.ac.ibaraki.kotlinattendanceapplication.api.response.LectureDataResponse
import retrofit2.http.GET
import rx.Observable

interface GetLectureListApiClient {
    @GET(Constants.GET_POSSIBLE_ATTEND_URL)
    fun getPossibleAttendList(): Observable<LectureDataResponse>
}