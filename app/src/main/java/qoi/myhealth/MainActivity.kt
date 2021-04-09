package qoi.myhealth


import android.accounts.OnAccountsUpdateListener
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.CMD.C18_Theme_Type
import qoi.myhealth.Ble.C18.Model.*
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.tool.AppStatusCheck

class MainActivity : AppCompatActivity(),BleDelegate {

    private val TAG = this::class.java.simpleName

    val bleManager:BleManager = BleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初期化
        bleManager.bleDelegate = this
        textView.setText("")

        // 初回起動か確認
        if(AppStatusCheck.isFirstJudgment(getApplicationContext())){
            println("first")
            AppStatusCheck.setFirstFlg(getApplicationContext(),false)
        }
        else{
            println("not_first")
        }

        //println(ShareDataManager.getUserSetInfo())
        toDeviceScanVC_btn.setOnClickListener {
            println("clicked")
            val intent = Intent(this,DeviceScanActivity::class.java)
            startActivity(intent)
        }

        // Status取得テストコマンド
        toAction.setOnClickListener {
            println("action clicked")
            println(bleManager.deviceDelegate)
            // デバイスと接続確認
            if (bleManager.deviceDelegate != null){
                userSetInfoTest()
            }
        }

        // Status設定コマンド
        testBtn.setOnClickListener {
            // デバイスと接続確認
            if (bleManager.deviceDelegate != null){
                //val userInfoJson = ShareDataManager.getUserSetInfo()
                (bleManager.deviceDelegate as C18Delegate).settingDisplayBright(0){bleResult, info ->
                    userSetInfoTest()
                }
            }
        }

        tryToConnectDevice()
    }

     fun setDeviceTolabel(){
        // 設定しているデバイスがあればデバイス名の表示
        (bleManager.deviceDelegate as C18Delegate).getDeviceName{bleResult, info ->
            val devideName = info!!.get(BleIdentificationKey.C18_DeviceNameInfo)as String
            GlobalScope.launch(Dispatchers.Main) {
                // ラベルに接続しているデバイス名を表示
                textView.setText(devideName)
            }
        }
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
    }

    override fun onBleDeviceConnection() {
        println("onBleDeviceConnection")
        println(ShareDataManager.getConnectionDeviceMac())

        // 現在接続しているデバイスの名前を取得
        if (bleManager.deviceDelegate != null) {
            setDeviceTolabel()
        }
    }

    override fun onBleDeviceDisConnection() {
        Log.d(TAG,"onBleDeviceDisConnectio")
    }


}