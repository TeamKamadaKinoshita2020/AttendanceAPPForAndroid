package jp.ac.ibaraki.kotlinattendanceapplication

import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.ac.ibaraki.kotlinattendanceapplication.databinding.FragmentFirstViewBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

import com.bumptech.glide.Glide

/**
 * 学生証読取を行う最初の画面フラグメント
 */
class FirstViewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ActionBar変更処理
        val activity: MainActivity = activity as MainActivity
        activity.currentFragment = "FirstViewFragment"
        activity.setTitle("学生証読取")
        // ヘッダ書き換え処理
        activity.header_container.textStudentInfo.text = ""
        activity.header_container.textLectureInfo.text = ""

        // データバインディングの設定・宣言
        var binding =
            DataBindingUtil.inflate<FragmentFirstViewBinding>(inflater, R.layout.fragment_first_view, container, false)
        // gif画像をセット
        val gifMovie: Int = R.raw.studentcard_tutorial
        Glide.with(this).load(gifMovie).into(binding.gifView)
        // メッセージテキストをセット
        binding.textMessage.text =  getString(R.string.text_read_stucard)
        // rootでviewを返している？
        return binding.root
    }

    /**
     * 次画面への遷移を管理する関数
     */
    fun transitionNextFragment(studentNumber: String, studentName: String) {
        // 確認ダイヤログ
        val message = getString(R.string.text_confirm_student_message) + "\n" + studentNumber + ":" + studentName
        AlertDialog.Builder(context!!).apply {
            setTitle(R.string.text_confirm_student_title)
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
                var nextFragment = SecondViewFragment()
                nextFragment.setArguments(bundle)
                // フラグメント差し替え
                activity.currentFragment = "SecondViewFragment"
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.main_container, nextFragment, "SecondViewFragment")
                transaction?.addToBackStack(null)
                transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction?.commit()
            })
            setNegativeButton("Cancel", null)
            show()
        }
    }
}