package jp.ac.ibaraki.kotlinattendanceapplication


import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.nfc.NfcAdapter
import android.nfc.tech.*
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import jp.ac.ibaraki.kotlinattendanceapplication.databinding.ActivityMainBinding
import jp.ac.ibaraki.kotlinattendanceapplication.nfclib.NfcTag
import jp.ac.ibaraki.kotlinattendanceapplication.nfclib.TagFactory
import rx.subscriptions.CompositeSubscription


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
    }
    private val compositeSubscription = CompositeSubscription()
    private var count = 0
    var currentFragment: String? = null


    // NFC関連の変数
    private val TAG: String = "Debug"
    private var nfcTag: NfcTag? = null
    private var mAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null
    private var mFilters: Array<IntentFilter>? = null
    private var mTechLists: Array<Array<String>>? = null
    private var isDisp: Boolean? = null
    private var si: StudentInfo? = null
    private var sci: SeatCardInfo? = null
    // private val webConnection: WebConnection? = null
    var isScanIC: Boolean = false
    private var isReturnActivity: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // NFC関連の記述
        this.mAdapter = NfcAdapter.getDefaultAdapter(this)
        /*this.mPendingIntent = PendingIntent.getActivities(context, 0, Intent(context, javaClass)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)*/
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        this.mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        var ndef: IntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            this.addDataType("*/*")
        }
        this.mFilters = arrayOf(ndef)

        this.mTechLists = arrayOf(
            arrayOf(IsoDep::class.java.name), arrayOf(MifareClassic::class.java.name), arrayOf(
                MifareUltralight::class.java.name
            ), arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name), arrayOf(
                NfcA::class.java.name
            ), arrayOf(NfcB::class.java.name), arrayOf(NfcF::class.java.name), arrayOf(NfcV::class.java.name)
        )

        //NFCがOFFならONにさせるウィンドウ表示
        if (mAdapter!!.isEnabled === false) {
            val alertDialog = AlertDialog.Builder(this)

            alertDialog.setTitle(R.string.noticeNFCdisable1)
            alertDialog.setMessage(R.string.noticeNFCdisable2)
            alertDialog.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                startActivity(intent)
                dialog.dismiss()
            })
            alertDialog.show()
        }

        isScanIC = false
        isReturnActivity = false
        isDisp = true

        // コードからフラグメントを追加
        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.main_container, FirstViewFragment(), "FirstViewFragment")
            transaction.commit()
            // this.currentFragment = "FirstViewFragment"
        }
    }

    override fun onDestroy() {
        compositeSubscription.clear()
        super.onDestroy()
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName!!
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onRusume()")
        mAdapter?.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
        mAdapter?.disableForegroundDispatch(this)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.nfcTag = TagFactory.create(intent)
        Log.d("currenrFragment", currentFragment)
        // 出席完了画面から戻ってきたときに読取動作が起こらないようにする
        if (isReturnActivity) {
            isReturnActivity = false
        } else if (currentFragment.equals("FirstViewFragment")) { // 学生証読取の場合
            scanStudentCard() //学生証のスキャン
            // 学生証読取成功　画面遷移
            if (isScanIC) {
                supportFragmentManager.findFragmentById(R.id.main_container)?.let {
                    (it as FirstViewFragment).transitionNextFragment(this.si!!.studentNumber, this.si!!.name)
                }
            }
        } else if (currentFragment.equals("ThirdViewFragment")) { // 座席カード読み取りの場合
            scanSeatCard()
            var idm = this.sci!!.idm
            idm = idm.replace(" ","")// システムで扱う形に整形
            idm =  idm.toLowerCase()
            idm = idm.substring(1)
            Log.d("resultReadIdm", idm)
            if (!idm.equals("")) {
                // 出席処理の呼び出し
                supportFragmentManager.findFragmentById(R.id.main_container)?.let {
                    (it as ThirdViewFragment).processAttendance(idm)
                }
            }
        }
    }


    //学生証内容の読み取り
    private fun scanStudentCard() {
        this.si = StudentInfo(this.nfcTag, StudentInfo.FULL_INFO)
        val scanStudentInfo = StudentInfo(this.nfcTag, StudentInfo.FULL_INFO) // 一時データ
        if (scanStudentInfo.getInfo() == StudentInfo.SUCCESSFUL_READING) { //読み込み成功
            // nfcmid.setText(this.si.studentNumber + "\n" + this.si.name +  "\n")
            //Toast.makeText(this, scanStudentInfo.studentNumber + "\n" + scanStudentInfo.name, Toast.LENGTH_SHORT) .show()
            //Toast.makeText(this, scanStudentInfo.kana + "\n" + scanStudentInfo.birthDate, Toast.LENGTH_SHORT).show()
            this.si?.let {
                this.si!!.studentNumber = scanStudentInfo.studentNumber
                this.si!!.name = scanStudentInfo.name
            }
            isScanIC = true
        } else { //読み込み失敗
            // Kotlinの条件分岐はswitch文ではなくwhen文を使う
            val value = scanStudentInfo.getInfo()
            when {
                value == StudentInfo.UNKNOWN_ERROR -> Toast.makeText(
                    this,
                    R.string.error_unknown,
                    Toast.LENGTH_SHORT
                ).show()
                value == StudentInfo.NON_FELICA -> Toast.makeText(
                    this,
                    R.string.error_nonfelica,
                    Toast.LENGTH_SHORT
                ).show()
                value == StudentInfo.LENGTH_ERROR -> Toast.makeText(
                    this,
                    R.string.error_length,
                    Toast.LENGTH_SHORT
                ).show()
                value == StudentInfo.AUTH_REQUIRED -> Toast.makeText(
                    this,
                    R.string.error_auth,
                    Toast.LENGTH_SHORT
                ).show()
                value == StudentInfo.UNSUPPORTED_ENCODING -> Toast.makeText(
                    this,
                    R.string.error_unsupport,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 座席カードの読み取り(学生証読取処理の魔改造、簡易版)
    private fun scanSeatCard() {
        this.sci = SeatCardInfo(this.nfcTag, StudentInfo.FULL_INFO)
        var scanSeatCardInfo = SeatCardInfo(this.nfcTag, StudentInfo.FULL_INFO) // 一時データ
        this.sci!!.getIdm()
    }
}