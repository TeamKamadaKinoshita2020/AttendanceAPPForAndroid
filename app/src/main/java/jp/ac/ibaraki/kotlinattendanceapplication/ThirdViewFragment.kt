package jp.ac.ibaraki.kotlinattendanceapplication

import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClientManager.CheckEmptySeatApiClientManager
import jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClientManager.GetSeatNumApiClientManager
import jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClientManager.SendAttendApiClientManager
import jp.ac.ibaraki.kotlinattendanceapplication.databinding.FragmentFirstViewBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import android.content.Intent



/**
 * 座席カード読取、出席処理を行うフラグメント
 */
class ThirdViewFragment : Fragment() {
    private val compositeSubscription = CompositeSubscription()

    private var studentNumber: String? = null
    private var studentName: String? = null
    private var lectureName: String? = null
    private var holdingId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 値渡しの受け取り
        val bundle = arguments
        bundle?.let {
            this.studentNumber = it.getString("StudentNumber")
            this.studentName = it.getString("StudentName")
            this.lectureName = it.getString("LectureName")
            this.holdingId = it.getString("HoldingId")
        }

        // ActionBar変更処理
        val activity: MainActivity = activity as MainActivity
        activity.currentFragment = "ThirdViewFragment"
        activity.setTitle("座席カード読取")
        // ヘッダ書き換え処理
        activity.header_container.textStudentInfo.text = this.studentNumber + ":" + this.studentName
        activity.header_container.textLectureInfo.text = this.lectureName

        // データバインディングの設定・宣言
        var binding =
            DataBindingUtil.inflate<FragmentFirstViewBinding>(inflater, R.layout.fragment_first_view, container, false)
        // gif画像をセット
        val gifMovie: Int = R.raw.seatcard_tutorial
        Glide.with(this).load(gifMovie).into(binding.gifView)
        //binding.gifView.setBackgroundResource(R.drawable.border)
        // メッセージテキストをセット
        binding.textMessage.text = getString(R.string.text_scan_seatcard)
        // rootでviewを返している？
        return binding.root
    }

    /**
     * 出席関連の処理を行う関数
     * 入れ子で通信を行い判定する(読みにくいけど)
     */
    fun processAttendance(idm: String) {
        var seatNum: String? = null

        // 存在する座席かチェック
        compositeSubscription.clear()
        compositeSubscription.add(
            GetSeatNumApiClientManager.getSeatNumApiClient.getSeatNum(idm, this.holdingId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d(MainActivity.TAG, "rx response=$it")
                    seatNum = it.seatNum!!
                }
                .doOnError {
                    Log.d("Error", it.message)
                }
                .doOnCompleted {
                }
                .subscribe {
                    if (it.seatNum.equals("0")) {
                        //　存在しない座席
                        showInvalidSeatCardDialog()
                    } else {
                        // 読み取った座席が空かどうかチェック
                        compositeSubscription.clear()
                        compositeSubscription.add(
                            CheckEmptySeatApiClientManager.checkEmptySeatApiClient.checkEmptySeat(
                                holdingId!!, idm
                            )
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext {
                                }
                                .doOnError {
                                    Log.d("Error", it.message)
                                }
                                .doOnCompleted {
                                }
                                .subscribe {
                                    if (it.result!!) {
                                        showNotEmptySeatDialog()
                                    } else {
                                        // 正常処理,出席確認
                                        // 番号提示、ダイヤログで確認
                                        val message =
                                            getString(R.string.text_confirm_attend_message) + "\n" + studentNumber + ":" + studentName + "\n" + lectureName + "\n" + getString(
                                                R.string.seat_num
                                            ) + ":" + seatNum
                                        AlertDialog.Builder(context!!).apply {
                                            setTitle(R.string.text_confirm_attend_title)
                                            setMessage(message)
                                            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                                                // OKをタップしたときの処理
                                                compositeSubscription.clear()
                                                compositeSubscription.add(
                                                    SendAttendApiClientManager.sendAttendApiClient.sendAttend(
                                                        holdingId!!, studentNumber!!, idm
                                                    )
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .doOnNext {
                                                        }
                                                        .doOnError {
                                                            Log.d("Error", it.message)
                                                        }
                                                        .doOnCompleted {
                                                        }
                                                        .subscribe {
                                                            // 正常に出席が完了していれば再起動
                                                            if (it.result!!) {
                                                                showSuccessAttendanceDialog()
                                                            } else {
                                                                showFailureAttendanceDialog()
                                                            }

                                                        })
                                            })
                                            setNegativeButton("Cancel", null)
                                            show()
                                        }
                                    }
                                }
                        )
                    }
                }
        )


    }

    /**
     * 以下ダイアログ生成形関数群
     */

    fun showInvalidSeatCardDialog() {
        AlertDialog.Builder(context!!).apply {
            setTitle(R.string.text_error)
            setMessage(R.string.error_invalid_seat_card)
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            })
            show()
        }
    }

    fun showNotEmptySeatDialog() {
        AlertDialog.Builder(context!!).apply {
            setTitle(R.string.text_error)
            setMessage(R.string.error_already_attend_seat)
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            })
            show()
        }
    }

    fun showSuccessAttendanceDialog() {
        AlertDialog.Builder(context!!).apply {
            setTitle("")
            setMessage(R.string.text_success_attendance)
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                // アプリ再起動処理　トップ画面に遷移
                // 履歴を消すことで戻れないようにしている
                val intent = Intent(context!!, SplashActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            })
            show()
        }
    }

    fun showFailureAttendanceDialog() {
        AlertDialog.Builder(context!!).apply {
            setTitle(R.string.text_error)
            setMessage(R.string.error_attendance)
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            })
            show()
        }
    }

}