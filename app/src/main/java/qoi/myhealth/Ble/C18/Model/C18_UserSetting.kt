package qoi.myhealth.Ble.C18.Model

import qoi.myhealth.Ble.C18.CMD.C18_OS_Language
import qoi.myhealth.Ble.model.UserInfo

class C18_TagetInfo(_taget_Step:Int,_taget_Kcal:Int,_taget_Distance:Int,_taget_SleepTime_hour:Int,_taget_SleepTime_min:Int) {
    var taget_Step = _taget_Step
    var taget_Kcal = _taget_Kcal
    var taget_Distance = _taget_Distance
    var taget_SleepTime_hour = _taget_SleepTime_hour
    var taget_SleepTime_min = _taget_SleepTime_min
}

class C18_UnitInfo(_distance_unit:Int,_weight_unit:Int,_temp_unit:Int,_timeMode_unit:Int) {
    var distance_unit = _distance_unit
    var weight_unit = _weight_unit
    var temp_unit = _temp_unit
    var timeMode_unit = _timeMode_unit
}


object C18_UserSettingInfo {
    var sleepInfo:C18_SleepInfo? = null
    var longSiteInfo:C18_LongSite? = null
    var tagetInfo:C18_TagetInfo? = null
    var userInfo:UserInfo? = null
    var unitInfo:C18_UnitInfo? = null

    var handWear:Int = 0                        //左右装着  0x00左 0x01右
    var heartAlert_isOpen:Boolean = false       //心拍アラートオン・オフ
    var heartAlert_value:Int = 0                //心拍アラート閾値 100-240
    var heartMon_mode:Boolean = false           //心拍モニタリングモード 0x00 手動 0x01 自動
    var heartMon_AutoMode_interval:Int = 0      //自動モードのモニタリング間隔 1-60
    var os_lang:C18_OS_Language = C18_OS_Language.JP_LAN        //言語
    var raiseDisplay:Boolean = false            //腕を上げてスリープ解除スイッチ
    var screenBright:Int = 0                    //画面の明るさ
    var loss_switch = false                     // 紛失防止設定



}