package qoi.myhealth.View.Activity.Setting.Fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_device_setting.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.CMD.C18_Hand_Wear
import qoi.myhealth.Ble.C18.CMD.C18_Theme_Type
import qoi.myhealth.Ble.C18.DeviceConnectData
import qoi.myhealth.Ble.C18.Model.C18_UserSettingInfo
import qoi.myhealth.Ble.C18.SettingDialog
import qoi.myhealth.Controller.Util.Progress
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.C18.*
import qoi.myhealth.View.Activity.Setting.MainActivity


class DeviceSettingFragment: Fragment(), BleDelegate {

    companion object {
        fun createInstance() : DeviceSettingFragment {
            val fragmentMain =
                DeviceSettingFragment()
            return fragmentMain
        }
    }

    private val handWearList: Array<String> = arrayOf("左手","右手")
    private val screenNameList: Array<String> = arrayOf("低め","普通","明るい")

    private var nowTheme:String = ""
    private var showAutoScanTime:String = ""
    private var screenBright:String = ""
    private var handWear : String = ""

    private var deviceConnectData: DeviceConnectData? = null

    private val bleManager: BleManager = BleManager

    private var group:ViewGroup? = null

    private var themevalue: TextView? = null
    private var longSiteValue: TextView? = null
    private var autoScreenValue: Switch? = null
    private var autoHeatBeatValue: TextView? = null
    private var wearValue: TextView? = null
    private var screenBrightValue: TextView? = null


    // Fragmentで表示するViewを作成するメソッド
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // 先ほどのレイアウトをここでViewとして作成します
        group = container
        return inflater.inflate(qoi.myhealth.R.layout.fragment_device_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bleManager.bleDelegate = this
        setToolBar()
        val maActivity = activity as MainActivity?
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            maActivity!!.setNavi(R.id.home)
        }
    }

    override fun onStart() {
        super.onStart()
        initConnect()
    }


    // 共通初期設定
    private fun initConnect(){

        // デバイス接続状況確認
       when{
            // c18
            ShareDataManager.getConnectType().equals("C18") ->{
                C18TitleInit()
                C18ValueInit()
                C18Setting()
                commonSetting()
            }
            // spo2
            ShareDataManager.getConnectType().equals("SPO2") ->{
                // SPO2Setting()
            }
            // 未接続
            else->{
                deviceConnectData = DeviceConnectData(bleManager,group!!.context)
                if (ContextCompat.checkSelfPermission(group!!.context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    deviceConnectData!!.deviceConnectDialog("接続デバイス一覧")
                }
                else{
                    val maActivity = activity as MainActivity?
                    maActivity!!.setNavi(R.id.home)
                }

            }
        }
    }

    private fun C18TitleInit(){
        // タイムチェック
        val timeTitle = view!!.findViewById<View>(R.id.time_title) as TextView
        timeTitle.text = "タイムチェック"

        // 同期データ
        val syncTitle = view!!.findViewById<View>(R.id.sync_title) as TextView
        syncTitle.text = "同期データ"

        // テーマタイトル
        val themeTitle = view!!.findViewById<View>(R.id.theme_title) as TextView
        themeTitle.text = "テーマ"
        // テーマ値
        themevalue = view!!.findViewById<View>(R.id.theme_value) as TextView

        // 座り過ぎアラート通知タイトル
        val longSiteTitle = view!!.findViewById<View>(R.id.long_site_title) as TextView
        longSiteTitle.text = "座り過ぎアラート通知"
        // 座り過ぎアラート通知値
        longSiteValue =  view!!.findViewById<View>(R.id.long_site_value) as TextView

        // アラームタイトル
        val alermTitle = view!!.findViewById<View>(R.id.alerm_title) as TextView
        alermTitle.text = "アラーム"

        // 自動心拍計測間隔タイトル
        val autoHeartBeatTitle = view!!.findViewById<View>(R.id.auto_heart_beat_title) as TextView
        autoHeartBeatTitle.text = "自動計測間隔時間"
        // 自動心拍計測間隔値
        autoHeatBeatValue =  view!!.findViewById<View>(R.id.auto_heart_beat_value) as TextView

        // 紛失防止設定タイトル
        val lostTitle = view!!.findViewById<View>(R.id.lost_title) as TextView
        lostTitle.text = "紛失防止設定"

        // 脈拍数アラートタイトル
        val pulseAlertTitle = view!!.findViewById<View>(R.id.pulse_alert_title) as TextView
        pulseAlertTitle.text = "脈拍数アラート通知"

        // スクリーン自動点灯タイトル
        val autoScreenTitle = view!!.findViewById<View>(R.id.auto_screen_title) as TextView
        autoScreenTitle.text = "スクリーン自動点灯"
        autoScreenValue = view!!.findViewById<View>(R.id.auto_screen_value) as Switch

        // 着用位置タイトル
        val wearTitle = view!!.findViewById<View>(R.id.wear_title) as TextView
        wearTitle.text = "着用位置"
        // 着用位置値
        wearValue = view!!.findViewById<View>(R.id.wear_value) as TextView

        // スクリーンの明るさタイトル
        val screenBright = view!!.findViewById<View>(R.id.screen_bright_title) as TextView
        screenBright.text = "スクリーンの明るさ"
        // スクリーンの明るさ値
        screenBrightValue = view!!.findViewById<View>(R.id.screen_bright_value) as TextView

        // お休みモード設定タイトル
        val sleepModeTitle = view!!.findViewById<View>(R.id.sleep_mode_title) as TextView
        sleepModeTitle.text = "お休みモード時間帯"

        // リセットタイトル
        val resetTitle = view!!.findViewById<View>(R.id.reset_title) as TextView
        resetTitle.text = "リセット"
    }

    // 表示値初期設定
    private fun C18ValueInit(){
        val device = bleManager.deviceDelegate as C18Delegate

        // デバイス名
        val deviceName = view!!.findViewById(qoi.myhealth.R.id.device_name) as TextView
        device.getDeviceName{ bleResult, info ->
            val devideName: String = info!!.get(BleIdentificationKey.C18_DeviceNameInfo)as String
            GlobalScope.launch(Dispatchers.Main) {
                deviceName.text = devideName
            }
        }

        // MACアドレス
        val macAddress = view!!.findViewById(qoi.myhealth.R.id.mac_address) as TextView
        macAddress.text = ShareDataManager.getConnectionDeviceMac()

        // テーマ設定取得
        (bleManager.deviceDelegate as C18Delegate).getDeviceTheme { bleResult, info ->
            GlobalScope.launch(Dispatchers.Main) {
                nowTheme = ""+info!!.get(BleIdentificationKey.C18_MainThemeInfo) as C18_Theme_Type
                nowTheme = nowTheme.replace("Type","テーマ")
                themevalue?.text = nowTheme
            }
        }

        // デバイスのデータ取得
        device.getUserSetInfo { bleResult, info ->
            // デバイスデータを取得しローカルに保存
            val userSetInfo: C18_UserSettingInfo = info!!.get(BleIdentificationKey.C18_UserSetInfo) as C18_UserSettingInfo
            ShareDataManager.saveUserSetInfo(userSetInfo)

            // 座り過ぎアラート
            val sitInterval = userSetInfo?.longSiteInfo?.interval

            // スクリーン自動点灯
            val raiseDisplay : Boolean = userSetInfo?.raiseDisplay

            // 自動計測時間(分)
            val autoScanTime = userSetInfo?.heartMon_AutoMode_interval

            // 着用位置の情報取得
            handWear = handWearList[userSetInfo?.handWear]

            // スクリーンの明るさ
            screenBright = screenNameList[userSetInfo?.screenBright]

            GlobalScope.launch(Dispatchers.Main) {
                longSiteValue!!.setText(""+sitInterval+"Min")
                autoScreenValue!!.isChecked = raiseDisplay
                showAutoScanTime = ""+autoScanTime + "Min"
                if(autoScanTime == 0){
                    showAutoScanTime = "自動計測なし"
                }
                autoHeatBeatValue!!.setText(showAutoScanTime)
                wearValue!!.setText(handWear)
                screenBrightValue!!.setText(screenBright)
            }
        }
    }

    private fun C18Setting(){
        val settingInfo: C18Delegate? = (bleManager.deviceDelegate as C18Delegate)

        // テーマ設定ダイアログ
        val themeNameList: Array<String> = arrayOf("テーマ1","テーマ2","テーマ3")
        val themeButton = view!!.findViewById<View>(R.id.themne_btn) as Button
        themeButton.setOnClickListener {
            SettingDialog.pickerDialog(group!!.context,"テーマ",themeNameList,nowTheme) { setnum ->
                GlobalScope.launch(Dispatchers.Main) {
                    var setTheme: C18_Theme_Type = C18_Theme_Type.Type1
                    when(setnum){
                        1->setTheme = C18_Theme_Type.Type2
                        2->setTheme = C18_Theme_Type.Type3
                        else->setTheme = C18_Theme_Type.Type1
                    }
                    settingInfo?.settingMainTheme(setTheme){bleResult, info ->
                        themevalue?.setText(themeNameList[setnum])
                        nowTheme = themeNameList[setnum]
                    }
                }
            }
        }

        // 座り過ぎアラート
        val longSiteButton = view!!.findViewById<View>(R.id.long_site_btn) as Button
        longSiteButton.setOnClickListener {
            fragmentManager!!.beginTransaction().remove(this).commit()
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, LongSiteFragment.createInstance())
                commit()
            }
        }

        // アラーム
        val alertmButton = view!!.findViewById<View>(R.id.alerm_btn) as Button
        alertmButton.setOnClickListener {
            fragmentManager!!.beginTransaction().remove(this).commit()
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, TimeAlertFragment.createInstance())
                commit()
            }
        }

        // 自動計測間隔時間
        var heartScanTimeList: Array<String> = arrayOf("自動計測なし")
        for(i in 1..6) {
            heartScanTimeList += "" + i * 10
        }
        val autoHeartBeatButton  = view!!.findViewById<View>(R.id.auto_heart_beat_btn) as Button
        autoHeartBeatButton.setOnClickListener {
            val nowData = showAutoScanTime.replace("Min", "")
            SettingDialog.pickerDialog(
                group!!.context,
                "自動心拍計測間隔",
                heartScanTimeList,
                nowData
            ) { setname ->
                GlobalScope.launch(Dispatchers.Main) {
                    var isHeartStart = false
                    if (setname > 0) {
                        isHeartStart = true
                    }
                    settingInfo?.settingHeartMonitor(
                        isHeartStart,
                        (setname * 10)
                    ) { bleResult, info ->
                        showAutoScanTime = "" + (setname * 10) + "Min"
                        if (setname == 0) {
                            showAutoScanTime = "自動計測なし"
                        }
                        autoHeatBeatValue!!.text = showAutoScanTime
                    }
                }
            }
        }

        // 防止設定
        val lostButton = view!!.findViewById<View>(R.id.lost_btn) as Button
        lostButton.setOnClickListener {
            fragmentManager!!.beginTransaction().remove(this).commit()
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, LoseFragment.createInstance())
                commit()
            }
        }

        // 脈拍通知アラート
        val puseAlertButton = view!!.findViewById<View>(R.id.pulse_alert_btn) as Button
        puseAlertButton.setOnClickListener {
            fragmentManager!!.beginTransaction().remove(this).commit()
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, PulseAlertFragment.createInstance())
                commit()
            }
        }

        // 自動点灯
        val autoScreenButton = view!!.findViewById<View>(R.id.auto_screen_btn) as Button
        autoScreenButton.setOnClickListener {
            autoScreenValue!!.isChecked = !autoScreenValue!!.isChecked
            settingInfo?.settingRaiseScreen(autoScreenValue!!.isChecked){bleResult, info ->}
        }

        // 着用位置
        val wearButton = view!!.findViewById<View>(R.id.wear_btn) as Button
        wearButton.setOnClickListener {
            SettingDialog.pickerDialog(group!!.context,"着用位置",handWearList,handWear){ setnum->
                var setVal: C18_Hand_Wear = C18_Hand_Wear.Left
                if(setnum == 1) setVal =  C18_Hand_Wear.Right
                settingInfo?.settingHandWear(setVal){ bleResult, info ->
                    handWear = handWearList[setnum]
                    wearValue!!.setText(handWearList[setnum])
                }
            }
        }

        // スクリーンの明るさ
        val screenBrightButton = view!!.findViewById<View>(R.id.screen_bright_btn) as Button
        screenBrightButton.setOnClickListener {
            SettingDialog.pickerDialog(group!!.context,"画面の明るさ",screenNameList,screenBright){ setnum->
                settingInfo?.settingDisplayBright(setnum){ bleResult, info ->
                    screenBright = screenNameList[setnum]
                    screenBrightValue!!.setText(screenNameList[setnum])
                }
            }
        }

        // お休みモード設定
        val sleepModeButton = view!!.findViewById<View>(R.id.sleep_mode_btn) as Button
        sleepModeButton.setOnClickListener {
            fragmentManager!!.beginTransaction().remove(this).commit()
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, SleepFragment.createInstance())
                commit()
            }
        }

        // デバイス設定のリセット
        val resetButton = view!!.findViewById<View>(R.id.reset_btn) as Button
        resetButton.setOnClickListener {
            SettingDialog.normalDialog(group!!.context,"メッセージ","デバイスを出荷時の設置に戻しますか"){
                settingInfo?.settingReSet(){bleResult, info ->
                    GlobalScope.launch(Dispatchers.Main){
                        bleManager.disconnectToDevice()
                        val maActivity = activity as MainActivity?
                        maActivity!!.setNavi(R.id.home)
                    }
                }
            }
        }
    }


    // 共通設定
    fun commonSetting(){
        val disconnectView: Button =  view!!.findViewById<View>(qoi.myhealth.R.id.disconnectBtn) as Button
        disconnectView.setOnClickListener{
            SettingDialog.normalDialog(group!!.context,"メッセージ","デバイスとの接続を切断"){
                bleManager.disconnectToDevice()
                val maActivity = activity as MainActivity?
                maActivity!!.setNavi(R.id.home)
            }
        }
    }

    private fun setToolBar(){
        val maActivity = activity as MainActivity?

        maActivity?.getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        maActivity?.getSupportActionBar()?.setCustomView(R.layout.my_toolbar)

        val title = activity!!.findViewById<View>(qoi.myhealth.R.id.head_title) as TextView
        title.text = "デバイス設定"

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.visibility = View.GONE

        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.visibility = View.GONE

    }

    override fun onBleDeviceConnection() {
        Progress.getInstance().closeDialog()
        val maActivity = activity as MainActivity?
        fragmentManager!!.beginTransaction().remove(this).commit()
        maActivity!!.createDeveiceSetting()
        deviceConnectData!!.closeDialog()
    }

    override fun onBleDeviceDisConnection() {
    }


}

