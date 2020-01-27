package jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClientManager

import com.google.gson.Gson
import jp.ac.ibaraki.kotlinattendanceapplication.Constants
import jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClient.CheckAlreadyAttendApiClient
import jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClient.CheckEmptySeatApiClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


open class CheckEmptySeatApiClientManager {
    companion object {
        private const val URL = Constants.BASE_URL
        //private val TAG = ApiClientManager::class.simpleName

        val checkEmptySeatApiClient: CheckEmptySeatApiClient
            get() = Retrofit.Builder()
                .client(getClient())
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(CheckEmptySeatApiClient::class.java)

        private fun getClient(): OkHttpClient {
            return OkHttpClient
                .Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        }
    }
}