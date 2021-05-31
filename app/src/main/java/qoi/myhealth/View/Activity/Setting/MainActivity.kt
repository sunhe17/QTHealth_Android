package qoi.myhealth.View.Activity.Setting


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.my_toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.CMD.C18_Theme_Type
import qoi.myhealth.Ble.C18.Model.*
import qoi.myhealth.Controller.Util.AppStatusCheck
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import java.util.*

class MainActivity: BaseActivity(){

    private val deviceDisconnectName:String = "デバイス未接続"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 仮で強制的に未接続にする　TODO テストのみ使用
        //ShareDataManager.saveConnectType(null)

        // 初回起動か確認
        if(AppStatusCheck.isFirstJudgment(this)){
           val intent = Intent(this,SetMyIDActivity::class.java)
           startActivity(intent)
        }
        else{
            println("not_first")
        }

        // 位置情報権限確認
        AppStatusCheck.checkPermission(this,this)

        createHome()

        // 自動接続実行
        //tryToConnectDevice()
    }

    override fun onStart() {
        super.onStart()
        selectNavi()
        head_title.text = "HOME"
        toolbtn.visibility = View.GONE
        backimg.visibility = View.GONE
    }

    override fun attachBaseContext(base: Context) {
        //val locale = Locale.ENGLISH
        val locale = Locale.getDefault()
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocales(LocaleList(locale))
        super.attachBaseContext(base.createConfigurationContext(config))
    }


    fun syncHealthDataTest() {
        bleManager.deviceDelegate!!.syncHealthData { bleResult, map ->  }
    }
    fun ECGModeTest(){
        (bleManager.deviceDelegate as C18Delegate).beginECGTest(true) { bleResult, info ->
        }
    }

    fun mainThemeTest(){
        (bleManager.deviceDelegate as C18Delegate).getDeviceTheme { bleResult, info ->
            val themeType = info!!.get(BleIdentificationKey.C18_MainThemeInfo) as C18_Theme_Type
            println(themeType)
        }
    }

    fun deviceMacInfoTest(){
        (bleManager.deviceDelegate as C18Delegate).getDeviceMac { bleResult, info ->
            val deviceName = info!!.get(BleIdentificationKey.C18_DeviceMacInfo) as String
            println(deviceName)
        }
    }

    fun deviceNameInfoTest(){
        (bleManager.deviceDelegate as C18Delegate).getDeviceName { bleResult, info ->
            GlobalScope.launch(Dispatchers.Main) {
                val deviceName = info!!.get(BleIdentificationKey.C18_DeviceNameInfo) as String
                println("デバイス名"+deviceName)
            }
        }
    }
    fun userSetInfoTest(){
        (bleManager.deviceDelegate as C18Delegate).getUserSetInfo { bleResult, info ->
            val userSetInfo:C18_UserSettingInfo = info!!.get(BleIdentificationKey.C18_UserSetInfo) as C18_UserSettingInfo
            ShareDataManager.saveUserSetInfo(userSetInfo)
            println("明るさ設定；"+ShareDataManager.getUserSetInfo()?.screenBright)
        }
    }
    fun alarmTimeAdd(){
        val alarmTime = C18_AlarmTime(C18_AlarmTime_Type.WakeUp,0x0au,0x05u,0x00u,0x01u)
        (bleManager.deviceDelegate as C18Delegate).settingAddAlarmTime(alarmTime) { bleResult, info ->
                    println(info)
        }
    }

    fun alarmTimeDel(alarmTime: C18_AlarmTime){

        (bleManager.deviceDelegate as C18Delegate).settingDelAlarmTime(alarmTime) { bleResult, info ->
            println(info)
        }
    }

    fun supportTest(){
        (bleManager.deviceDelegate as C18Delegate).getDevSupport  { bleResult, info ->
            val supportInfo = info!!.get(BleIdentificationKey.C18_SupportInfo) as C18_DeviceSupportInfo
            println(supportInfo.isSupport_findDev)
        }
    }

    fun switchStatusTest(){
        (bleManager.deviceDelegate as C18Delegate).getSwitchStatus { bleResult, info ->
            val supportInfo = info!!.get(BleIdentificationKey.C18_SwitchStatusInfo) as C18_SwitchStatusInfo
            println(supportInfo.heart_Switch)
        }
    }
    fun tryToConnectDevice(){
        val deviceMac = ShareDataManager.getConnectionDeviceMac()
        if (deviceMac != null){
            bleManager.retrieveDevice(this, deviceMac)
        }
        else{
            ShareDataManager.saveConnectType(null)
        }
    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }


    fun setNavi(id: Int){
        bottom_navigation.selectedItemId = id
    }

}