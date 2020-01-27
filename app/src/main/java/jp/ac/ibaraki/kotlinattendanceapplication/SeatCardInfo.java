package jp.ac.ibaraki.kotlinattendanceapplication;

import android.util.Log;
import jp.ac.ibaraki.kotlinattendanceapplication.nfclib.FelicaTag;
import jp.ac.ibaraki.kotlinattendanceapplication.nfclib.FelicaTag.ServiceCode;
import jp.ac.ibaraki.kotlinattendanceapplication.nfclib.FelicaTag.SystemCode;
import jp.ac.ibaraki.kotlinattendanceapplication.nfclib.NfcException;
import jp.ac.ibaraki.kotlinattendanceapplication.nfclib.NfcTag;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * 学生証読取関連のクラスを使いまわした座席カード読み取りクラス
 */
public class SeatCardInfo {

    public static String SYSTEM_CODE = "81F8";
    public static String SERVICE_CODE = "100B";
    public static boolean FULL_INFO = true;
    public static boolean PARTIAL_INFO = false;
    public static final int UNKNOWN_ERROR = -1;
    public static final int SUCCESSFUL_READING = 0;
    public static final int NON_FELICA = 1;
    public static final int LENGTH_ERROR = 2;
    public static final int AUTH_REQUIRED = 3;
    public static final int UNSUPPORTED_ENCODING = 4;

    public String name;
    public String kana;
    public String idm;
    public String studentNumber;
    public String birthDate;
    public String issueDate;
    public String expireDate;

    public boolean fullInfo = false;
    private NfcTag nfcTag;
    public FelicaTag felicaTag;

    public SeatCardInfo(NfcTag nTag) {
        this.setDefault();
        this.nfcTag = nTag;
    }

    public SeatCardInfo(NfcTag nTag, boolean fullInfo) {
        this.setDefault();
        this.fullInfo = fullInfo;
        this.nfcTag = nTag;
    }

    private void setDefault() {
        this.name = "";
        this.kana = "";
        this.studentNumber = "";
        this.birthDate = "";
        this.idm = "";
        this.issueDate = "";
        this.expireDate = "";
    }

    public int getInfo()// 座席情報で使用するIdmのみ読み出す
    {
        if (this.nfcTag == null || !this.nfcTag.getType().equals(NfcTag.TYPE_FELICA)) {
            return NON_FELICA;
        }
        this.felicaTag = (FelicaTag) this.nfcTag;
        SystemCode[] systemCodeList = null;
        List<ServiceCode> serviceCodeList = null;
        ServiceCode servCode = null;
        boolean cardValidity = false;
        int sysCodeIndex = 0;
        byte[] blockData = null;
        String tmpStr = "";

        try {
            systemCodeList = this.felicaTag.getSystemCodeList();
            for (int i = 0; i < systemCodeList.length && !cardValidity; i++) {
                if (systemCodeList[i].simpleToString().equalsIgnoreCase(SYSTEM_CODE)) {
                    cardValidity = true;
                    sysCodeIndex = i;
                }
            }
            cardValidity = (systemCodeList.length == 2);
            if (!cardValidity) {
                return LENGTH_ERROR;
            }
            this.felicaTag.polling(systemCodeList[sysCodeIndex]);
            serviceCodeList = this.felicaTag.getServiceCodeList();

            cardValidity = false;
            for (ServiceCode sc : serviceCodeList) {
                if (sc.simpleToString().equalsIgnoreCase(SERVICE_CODE)) {
                    cardValidity = true;
                    servCode = sc;
                    break;
                }
            }
            if (!cardValidity || servCode.encryptNeeded()) {
                return AUTH_REQUIRED;
            }

            //Idmの表示
            //Log.d("idm","idmは" + felicaTag.getIdm().toString());
            this.idm = felicaTag.getIdm().toString();

        } catch (NfcException e) {
            //Idmの表示
            Log.d("idm", "idmは" + felicaTag.getIdm().simpleToString());
            this.idm = felicaTag.getIdm().simpleToString();

            return UNKNOWN_ERROR;
        }
        return SUCCESSFUL_READING;
    }

    public String getIdm()// 座席情報で使用するIdmのみ読み出す
    {
        this.felicaTag = (FelicaTag) this.nfcTag;
        SystemCode[] systemCodeList = null;
        List<ServiceCode> serviceCodeList = null;
        ServiceCode servCode = null;
        boolean cardValidity = false;
        int sysCodeIndex = 0;
        byte[] blockData = null;
        String tmpStr = "";

        try {
            systemCodeList = this.felicaTag.getSystemCodeList();
            for (int i = 0; i < systemCodeList.length && !cardValidity; i++) {
                if (systemCodeList[i].simpleToString().equalsIgnoreCase(SYSTEM_CODE)) {
                    cardValidity = true;
                    sysCodeIndex = i;
                }
            }
            cardValidity = (systemCodeList.length == 2);
            this.felicaTag.polling(systemCodeList[sysCodeIndex]);
            serviceCodeList = this.felicaTag.getServiceCodeList();

            cardValidity = false;
            for (ServiceCode sc : serviceCodeList) {
                if (sc.simpleToString().equalsIgnoreCase(SERVICE_CODE)) {
                    cardValidity = true;
                    servCode = sc;
                    break;
                }
            }

            //Idmの表示
            Log.d("idm","idmは" + felicaTag.getIdm().toString());
            this.idm = felicaTag.getIdm().toString();
            return felicaTag.getIdm().toString();

        } catch (NfcException e) {
            //Idmの表示
            Log.d("idm", "idmは" + felicaTag.getIdm().simpleToString());
            this.idm = felicaTag.getIdm().simpleToString();

            return felicaTag.getIdm().toString();
        }
    }

    private boolean dataEnds(byte[] blockData) {
        byte endPos = blockData[blockData.length - 1];
        if (endPos == 32 || endPos == 0)
            return true;
        else
            return false;
    }
}