package jp.ac.ibaraki.kotlinattendanceapplication

import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.ac.ibaraki.kotlinattendanceapplication.MainActivity.Companion.TAG
import jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClientManager.CheckAlreadyAttendApiClientManager
import jp.ac.ibaraki.kotlinattendanceapplication.api.ApiClientManager.GetLectureListApiClientManager
import jp.ac.ibaraki.kotlinattendanceapplication.data.LectureData
import jp.ac.ibaraki.kotlinattendanceapplication.databinding.FragmentSecondViewBinding
import jp.ac.ibaraki.kotlinattendanceapplication.views.LectureListDataModel
import jp.ac.ibaraki.kotlinattendanceapplication.views.LectureListRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * 出席講義の選択を行う画面フラグメント RecyclerViewによりリスト表示をしている
 */
class SecondViewFragment : Fragment() {
    // 通信用変数
    private val compositeSubscription = CompositeSubscription()
    var dataArrayListModel: ArrayList<LectureListDataModel> = ArrayList()
    var lectureDataList: ArrayList<LectureData> = ArrayList()

    // 通信結果定数
    val HTTP_SUCCESS = "200"
    val HTTP_ERROR = "400"

    // ヘッダ表示用変数
    private var studentNumber: String? = null
    private var studentName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // リスト情報の更新(初期化)
        dataArrayListModel = ArrayList()
        lectureDataList = ArrayList()
        // 値渡しの受け取り
        val bundle = arguments
        bundle?.let {
            this.studentNumber = it.getString("StudentNumber")
            this.studentName = it.getString("StudentName")
        }

        // ActionBar変更処理
        val activity: MainActivity = activity as MainActivity
        activity.currentFragment = "SecondViewFragment"
        activity.setTitle("出席講義選択")

        // ヘッダ書き換え処理
        activity.header_container.textStudentInfo.text = this.studentNumber + ":" + this.studentName
        activity.header_container.textLectureInfo.text = ""

        // データバインディングの設定・宣言
        var binding = DataBindingUtil.inflate<FragmentSecondViewBinding>(
            inflater,
            R.layout.fragment_second_view,
            container,
            false
        )
        // メッセージをセット
        binding.messageText.text = getText(R.string.text_select_lecture)

        // アダプタの宣言とタッチイベントのオーバーライド処理
        val adapter = object : LectureListRecyclerAdapter(dataArrayListModel) {
            override fun recyclerOnClick(view: View, position: Int) {
                this@SecondViewFragment.recyclerOnClick(view, position)
                Log.d("onClick", dataArrayListModel.get(position).lectureName)
            }
        }
        // 通信で講義情報を取得
        compositeSubscription.clear()
        compositeSubscription.add(
            GetLectureListApiClientManager.getLectureListApiClient.getPossibleAttendList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d(TAG, "rx response=$it")
                    if (it.count.equals("0")) { // 集計中の講義がない(出席できない)
                        // メッセージ変更処理
                        binding.messageText.text = getText(R.string.text_no_lecture)

                    } else {// 正常処理
                        // リストに講義情報を保存
                        for (value in it.results) {
                            dataArrayListModel.add(
                                LectureListDataModel(
                                    value.text!!,
                                    value.rep_name!!,
                                    value.room_name!!
                                )
                            )
                            lectureDataList.add(
                                LectureData(
                                    value.text!!,
                                    value.rep_name!!,
                                    value.room_name!!,
                                    value.holding_id!!
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
                .doOnError {
                    Log.d("Error", it.message)
                }
                .doOnCompleted {
                }
                .subscribe(
                ))

        binding.lectureListRecyclerView.setHasFixedSize(true)
        binding.lectureListRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.lectureListRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        ) // リストアイテムの区切り線を追加
        binding.lectureListRecyclerView.adapter = adapter
        return binding.root
    }

    // リストがクリックされた場合の処理
    fun recyclerOnClick(view: View, position: Int) {
        var message = ""
        // 出席済みか確認
        compositeSubscription.clear()
        compositeSubscription.add(
            CheckAlreadyAttendApiClientManager.checkAlreadyAttendApiClient.checkAlreadyAttend(
                lectureDataList[position].holding_id!!,
                studentNumber!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d("check_already_attend", it.toString())
                    // 確認ダイアログ用のメッセージをセット
                    message = setDialogMessage(it.result!!)
                }
                .doOnError {
                    Log.d("Error", it.message)
                }
                .doOnCompleted {
                }
                .subscribe { // 上記の通信処理が終了後の処理(MainThread??)
                    // 講義選択確定　画面遷移
                    // OKをタップしたときの処理
                    // NFC読取情報のリセット
                    val activity: MainActivity = activity as MainActivity
                    activity.isScanIC = false
                    //画面遷移処理
                    transitionNextFragment(lectureDataList[position], message)
                }
        )

    }

    /**
     * 次画面への遷移を管理する関数
     */
    private fun transitionNextFragment(lectureInfo: LectureData, messageDialog: String) {
        // 確認ダイヤログ
        val message = messageDialog + "\n" + lectureInfo.text
        AlertDialog.Builder(context!!).apply {
            setTitle(R.string.text_confirm_lecture_title)
            setMessage(message)
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                // OKをタップしたときの処理
                // NFC読取情報のリセット
                val activity: MainActivity = activity as MainActivity
                activity.isScanIC = false
                //画面遷移処理
                // 値渡しの準備
                var bundle = Bundle()
                bundle.putString("StudentNumber", studentNumber)
                bundle.putString("StudentName", studentName)
                bundle.putString("LectureName", lectureInfo.text)
                bundle.putString("HoldingId", lectureInfo.holding_id)
                var nextFragment = ThirdViewFragment()
                nextFragment.setArguments(bundle)
                // フラグメント差し替え
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.main_container, nextFragment, "ThirdViewFragment")
                transaction?.addToBackStack(null)
                transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction?.commit()
            })
            setNegativeButton("Cancel", null)
            show()
        }
    }

    /**
     * ダイアログ用のメッセージをセットする関数
     * IDEくんが処理を分けたほうがいいと言うので作成
     */
    fun setDialogMessage(result: Boolean): String {
        when (result) {
            true -> return getString(R.string.text_confirm_already_attend_message)
            false -> return getString(R.string.text_confirm_attend_message)
        }
    }


    // リスト要素のクラス
    class LectureListData(var lectureName: String, var teacherName: String, var roomName: String, var holdingId: String)
}