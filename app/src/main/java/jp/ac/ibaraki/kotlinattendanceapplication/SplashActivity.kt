package jp.ac.ibaraki.kotlinattendanceapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private var handler: Handler? = null
    private val SPLASH_DELAY: Long = 1500
    private val runnable = Runnable {
        // write code that you want to delay. for example,
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        finish() // 次画面から戻らないように
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    /**
     * 処理をonCreateからこちらに移動
     * アプリ停止時に画面遷移処理が止まってしまうため
     */
    override fun onResume() {
        super.onResume()

        handler = Handler()
        handler!!.postDelayed(runnable, SPLASH_DELAY)
    }

    override fun onStop() {
        super.onStop()
        handler!!.removeCallbacks(runnable) // スプラッシュ画面中にアプリを落とした際、再表示しない処理
    }
}