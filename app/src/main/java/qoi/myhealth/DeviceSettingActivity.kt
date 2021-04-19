package qoi.myhealth

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_device_setting.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.Ble_Result
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.CMD.C18_Theme_Type
import qoi.myhealth.Ble.C18.Model.C18_UserSettingInfo
import qoi.myhealth.Manager.ShareDataManager
import java.util.*

class DeviceSettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_setting)

        // デバイスの設定情報を取得
        c18SettingValue()

        // デバイス情報設定
        clickDeviseSetting()
    }

    // TODO どのデバイスと接続しているか確認
    fun connectDeviceType(){
        // 接続していない、SP02と接続していれば接続画面のモーダルを表示する
    }

    // ユーザー設定からデータを取得して表示
    fun c18SettingValue() {

        // テーマ設定取得
        (bleManager.deviceDelegate as C18Delegate).getDeviceTheme { bleResult, info ->
            themeVal.setText(""+info!!.get(BleIdentificationKey.C18_MainThemeInfo) as C18_Theme_Type)
        }

        // デバイスの設定情報
        (bleManager.deviceDelegate as C18Delegate).getUserSetInfo { bleResult, info ->
            val userSetInfo: C18_UserSettingInfo = info!!.get(BleIdentificationKey.C18_UserSetInfo) as C18_UserSettingInfo
            ShareDataManager.saveUserSetInfo(userSetInfo)
            // 座り過ぎアラート間隔時間
            val sitInterval = userSetInfo?.longSiteInfo?.interval
            // スクリーンの自動点灯
            val raiseDisplay : Boolean = userSetInfo?.raiseDisplay!!
            // 自動計測時間(分)
            val autoScanTime : Int? = userSetInfo?.heartMon_AutoMode_interval
            // 着用位置の情報取得
            var handWear : String = "左手"
            if(userSetInfo?.handWear == 1){
                handWear = "右手"
            }
            // スクリーンの明るさ
            var screenBright : String= ""
            when(userSetInfo?.screenBright){
                1 -> screenBright ="普通"
                2 -> screenBright = "明るい"
                else -> screenBright = "低め"
            }

            GlobalScope.launch(Dispatchers.Main) {
                longSiteVal.setText(""+sitInterval+"Min")
                raiseDisplaySwitch.isChecked = raiseDisplay
                autoIntervalVal.setText(""+autoScanTime + "Min")
                handWearVal.setText(handWear)
                screenBrightVal.setText(screenBright)
            }

        }
    }

    // 各設定画面をクリックした時の処理
    fun clickDeviseSetting(){

        val settingInfo: C18Delegate? = (bleManager.deviceDelegate as C18Delegate)

        // テーマ設定
        val themeNameList: Array<String> = arrayOf("テーマ1","テーマ2","テーマ3")
        deviceTheme.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("テーマ設定")
                .setItems(themeNameList ,{dialog, which ->
                    var setTheme: C18_Theme_Type = C18_Theme_Type.Type1
                    when(which){
                        1->setTheme = C18_Theme_Type.Type2
                        2->setTheme = C18_Theme_Type.Type3
                        else->setTheme = C18_Theme_Type.Type1
                    }
                    settingInfo?.settingMainTheme(setTheme){bleResult, info ->
                        themeVal.setText(themeNameList[which])
                    }
                })
                .show()
        }

        // 自動計測間隔
        var heartScanTimeList: Array<String> = arrayOf("自動計測間隔")
        for(i in 1..6){
            heartScanTimeList += "" + i * 10
        }
        autoInterval.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("座り過ぎアラート通知")
                .setItems(heartScanTimeList ,{dialog, which ->
                    var setTime: Int = 0
                    var isHeartStart = false
                    if(which>0){
                        isHeartStart = true
                        setTime = heartScanTimeList[which].toInt()
                    }
                    settingInfo?.settingHeartMonitor(isHeartStart,setTime){ bleResult, info ->
                        autoIntervalVal.setText(heartScanTimeList[which] + "Min")
                    }
                })
                .show()
        }

        // 着用位置
        raiseDisplaySwitch.setOnCheckedChangeListener { _, isChecked ->
           settingInfo?.settingRaiseScreen(isChecked){bleResult, info ->
           }
        }

        // 画面の明るさを設定
        val screenNameList: Array<String> = arrayOf("低め","普通","明るい")
        screenBright.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("スクリーンの明るさ")
                .setItems(screenNameList ,{dialog, which ->
                    settingInfo?.settingDisplayBright(which){ bleResult, info ->
                        // 設定の表示を変更
                        screenBrightVal.setText(screenNameList[which])
                    }
                })
                .show()
        }



    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }


}