package qoi.myhealth.View.Activity.Setting.Fragment

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_access_key.*
import kotlinx.android.synthetic.main.fragment_appsetting.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import qoi.myhealth.API.APIBase
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.C18.SettingDialog.InputDialog
import qoi.myhealth.Ble.C18.SettingDialog.pickerDialog
import qoi.myhealth.Controller.Util.Progress
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.C18.LongSiteFragment
import qoi.myhealth.View.Activity.Setting.MainActivity
import qoi.myhealth.View.Activity.Setting.MyCaptureActivity
import qoi.myhealth.View.Activity.Setting.UserSettingActivity
import java.util.*

class AppSettingFragment:Fragment() {
    private var group: ViewGroup? = null

    private val laugaugeList: Array<String> = arrayOf("JS", "ENG", "CH")
    private var uuidText: TextView? = null
    private var uuidEditBUtton: Button? = null
    private var accessKeyImg: ImageView? = null
    private var userAuthInfo = ShareDataManager.getScanData()

    private var walkGoalValue: TextView? = null
    private var sleepGoalValue: TextView? = null
    private var languageValue: TextView? = null

    private var inflater: LayoutInflater? = null
    private var dialogView: View? = null

    companion object {
        // シングルトンインスタンスの宣言
        private var instance:  AppSettingFragment  =  AppSettingFragment ()
        // インスタンス取得
        fun createInstance() :  AppSettingFragment  {
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        group = container
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(qoi.myhealth.R.layout.fragment_appsetting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBar()
        init()
        setting()
    }

    private fun setToolBar() {
        val maActivity = activity as MainActivity?

        maActivity?.getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        maActivity?.getSupportActionBar()?.setCustomView(R.layout.my_toolbar)

        val title = activity!!.findViewById<View>(qoi.myhealth.R.id.head_title) as TextView
        title.text = getString(R.string.appSetting_Title)

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.setOnClickListener {
            maActivity!!.setNavi(R.id.device)
        }

        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.visibility = View.GONE

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            maActivity!!.setNavi(R.id.device)
        }
    }

    private fun init() {
        // UUID
        uuidText = view!!.findViewById<View>(R.id.uuid) as TextView
        uuidText!!.text = userAuthInfo.uuid

        // UUID編集ボタン
        uuidEditBUtton = view!!.findViewById<View>(R.id.uuid_edit_btn) as Button
        uuidEditBUtton!!.setText(getString(R.string.edit))

        // ACCESS_KEYの認証状態画像
        accessKeyImg = view!!.findViewById<View>(R.id.auth_img) as ImageView
        changeAccessState()

        // ACCESS_KEY状態
        val accessKeyStateText = view!!.findViewById<View>(R.id.access_key_text) as TextView
        accessKeyStateText!!.text = getString(R.string.appSetting_AccessKeyStatus)

        // ACCESS_KEY認証
        val accessKeyEditText = view!!.findViewById<View>(R.id.access_key_edit_text) as TextView
        accessKeyEditText!!.text = getString(R.string.appSetting_AccessKeyAuthe)

        // 個人情報
        val userSettingText = view!!.findViewById<View>(R.id.user_setting_text) as TextView
        userSettingText.text = getString(R.string.appSetting_PersonalInfo)

        // 歩数目標設定タイトル
        val walkGoalTitleText = view!!.findViewById<View>(R.id.walk_goal_text) as TextView
        walkGoalTitleText.text = getString(R.string.appSetting_StepGoal)


        // 歩数目標設定数値
        walkGoalValue = view!!.findViewById<View>(R.id.walk_goal_value) as TextView
        walkGoalValue!!.text = userAuthInfo.walkGoal.toString()


        // 睡眠目標設定タイトル
        val sleepGoalTitleText = view!!.findViewById<View>(R.id.sleep_goal_title) as TextView
        sleepGoalTitleText.text = getString(R.string.appSetteing_SleepGoal)

        // 睡眠目標設定値
        sleepGoalValue = view!!.findViewById<View>(R.id.sleep_goal_value) as TextView
        sleepGoalValue!!.text = userAuthInfo.sleepGoal.toString()

        // 言語設定
        val languageTitle = view!!.findViewById<View>(R.id.language_text) as TextView
        languageTitle.text = getString(R.string.appSetteing_Language)

        // 言語設定値
        languageValue = view!!.findViewById<View>(R.id.language_value) as TextView
        languageValue!!.text = laugaugeList[userAuthInfo.language]
    }

    private fun setting() {
        // uuid
        val uuidEditButton = view!!.findViewById<View>(R.id.uuid_edit_btn) as Button
        uuidEditButton.setOnClickListener {
            InputDialog(group!!.context, "ID編集", userAuthInfo.uuid, "String") {
                userAuthInfo.uuid = it
                ShareDataManager.saveScanData(userAuthInfo)
            }
        }

        // アクセスキー認証
        val accessKeyEditButton = view!!.findViewById<View>(R.id.access_key_btn) as Button
        accessKeyEditButton.setOnClickListener {
            accessKeyDialog("キー編集", userAuthInfo.key) { it ->
                APIBase.getInstance().getAuthToken(it){ code, token->
                    GlobalScope.launch(Dispatchers.Main){
                        if(code ==  APIBase.getInstance().OK){
                            userAuthInfo.key = it
                            userAuthInfo.auth = true
                            ShareDataManager.saveScanData(userAuthInfo)
                            Toast.makeText(group!!.context, "認証に成功しました", Toast.LENGTH_LONG).show()
                        }
                        else{
                            userAuthInfo.auth = false
                            ShareDataManager.saveScanData(userAuthInfo)
                            Toast.makeText(group!!.context, "認証に失敗しました", Toast.LENGTH_LONG).show()
                        }
                        changeAccessState()
                    }
                }
            }
        }

        // 個人設定
        val userSettingButton = view!!.findViewById<View>(R.id.user_setting_btn) as Button
        userSettingButton.setOnClickListener {
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, UserSettingFragment.createInstance(true))
                commit()
            }
        }

        // 歩数目標
        val walkGoalButton = view!!.findViewById<View>(R.id.walk_goal_btn) as Button
        walkGoalButton.setOnClickListener {
            InputDialog(
                group!!.context,
                "歩数目標",
                userAuthInfo.walkGoal.toString(),
                "Int"
            ) { inputname ->
                if (inputname.toIntOrNull() != null) {
                    userAuthInfo.walkGoal = inputname.toInt()
                    walkGoalValue!!.text = inputname
                }
            }
        }

        // 睡眠目標
        val sleepGoalButton = view!!.findViewById<View>(R.id.sleep_goal_btn) as Button
        sleepGoalButton.setOnClickListener {
            InputDialog(
                group!!.context,
                "睡眠目標",
                userAuthInfo.sleepGoal.toString(),
                "Double"
            ) { inputname ->
                if (inputname.toDoubleOrNull() != null) {
                    userAuthInfo.sleepGoal = inputname.toDouble()
                    sleepGoalValue!!.text = inputname
                }
            }
        }

        // 言語設定
        val langaugeButton = view!!.findViewById<View>(R.id.language_btn) as Button
        langaugeButton.setOnClickListener {
            pickerDialog(
                group!!.context,
                "言語設定",
                laugaugeList,
                laugaugeList[userAuthInfo.language]
            ) { item ->
                userAuthInfo.language = item
                languageValue!!.text = laugaugeList[item]
                ShareDataManager.saveScanData(userAuthInfo)

                val maActivity = activity as MainActivity?
                maActivity!!.reload()
            }
        }

    }

    private fun changeAccessState() {
        // 認証状態
        if (userAuthInfo.auth) {
            accessKeyImg!!.setImageResource(R.drawable.skin_white)
        }
        // 非認証状態
        else {
            accessKeyImg!!.setImageResource(R.drawable.skin_brown)
        }
    }

    private fun accessKeyDialog(title: String, data: String, method1: (String) -> Unit) {
        inflater = LayoutInflater.from(group!!.context)
        dialogView = inflater!!.inflate(qoi.myhealth.R.layout.layout_accesskey_dialog, null)
        val text = dialogView!!.findViewById<EditText>(R.id.access_key_value)
        text.setText(data)

        AlertDialog.Builder(group!!.context)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("確定") { dialog, which ->
                method1(text.text.toString())
            }
            .setNegativeButton("キャンセル", null)
            .show()

        val qrbtn = dialogView!!.findViewById<ImageButton>(qoi.myhealth.R.id.qr_btn)
        qrbtn.setOnClickListener {
            val intentIntegrator = IntentIntegrator(activity).apply {
                setPrompt("Scan a QR code")
                captureActivity = MyCaptureActivity::class.java
            }
            intentIntegrator.initiateScan()
        }
    }

    fun setKeyView(data: String){
        val text = dialogView!!.findViewById<EditText>(R.id.access_key_value)
        val replaceData = data.replace("QOI://?access=","")
        text.setText(replaceData)
    }

    fun getLangauge(): Int{
        return userAuthInfo.language
    }
}