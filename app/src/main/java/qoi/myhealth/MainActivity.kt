package qoi.myhealth


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.CMD.C18_Theme_Type
import qoi.myhealth.Ble.C18.Model.*
import qoi.myhealth.Manager.ShareDataManager

class MainActivity : AppCompatActivity(),BleDelegate {

    private val TAG = this::class.java.simpleName

    val bleManager:BleManager = BleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(ShareDataManager.getUserSetInfo())
        toDeviceScanVC_btn.setOnClickListener {
            println("clicked")
            val intent = Intent(this,DeviceScanActivity::class.java)
            startActivity(intent)
        }

        toAction.setOnClickListener {
            println("action clicked")
            println(bleManager.deviceDelegate)
            if (bleManager.deviceDelegate != null){
                syncHealthDataTest()
            }
        }

        tryToConnectDevice()

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
            val deviceName = info!!.get(BleIdentificationKey.C18_DeviceNameInfo) as String
            println(deviceName)
        }
    }
    fun userSetInfoTest(){
        (bleManager.deviceDelegate as C18Delegate).getUserSetInfo { bleResult, info ->
            val userSetInfo:C18_UserSettingInfo = info!!.get(BleIdentificationKey.C18_UserSetInfo) as C18_UserSettingInfo
            ShareDataManager.saveUserSetInfo(userSetInfo)
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
            bleManager.retrieveDevice(this,deviceMac)
        }
    }

    override fun onBleDeviceConnection() {
        Log.d(TAG,"onBleDeviceConnection")
    }

    override fun onBleDeviceDisConnection() {
        Log.d(TAG,"onBleDeviceDisConnectio")
    }


}