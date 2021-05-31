package qoi.myhealth.Ble.C18.CMD

import qoi.myhealth.Ble.C18.Model.C18_AlarmTime
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime_Action
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime_Type
import qoi.myhealth.Ble.C18.Model.C18_LongSite
import qoi.myhealth.Ble.Extension.toUBytes
import java.util.*

enum class C18_Setting_Key(val vl: UByte) {
    Time(0x00u),
    Alarm(0x01u),
    Goal(0x02u),
    UserInfo(0x03u),
    Uint(0x04u),
    LongSite(0x05u),
    Antilose(0x06u),
    AntiloseArgs(0x07u),
    HandWear(0x08u),
    PhoneOS(0x09u),
    PushSwitch(0x0au),
    HeartAlarm(0x0bu),
    HeartAutoMode(0x0cu),
    ReSet(0x0eu),
    SleepMode(0x0fu),
    Language(0x12u),
    RaiseScreen(0x13u),
    DisplayBright(0x14u),
    SkinColor(0x15u),
    BloodRange(0x16u),
    MainTheme(0x19u),
    TEMP(0x20u);

    companion object {
        fun getKey(vl:UByte): C18_Setting_Key? = C18_Setting_Key.values().find { it.vl == vl }?: null
    }
}

enum class C18_OS_Language(val vl:UByte){
    EN_LAN(0x00u),
    JP_LAN(0x05u);

    companion object {
        fun getKey(vl:UByte): C18_OS_Language = C18_OS_Language.values().find { it.vl == vl }?: EN_LAN
    }
}

enum class C18_Blood_Range(val vl: UByte){
    Low(0x00u),         //低い
    Normally(0x01u),    //正常
    Minor(0x02u),       //軽微
    Medium(0x03u),      //中程度
    Height(0x04u)       //重度
}

enum class C18_Hand_Wear(val vl: UByte) {
    Left(0x00u),
    Right(0x01u)
}

enum class C18_Theme_Type(val vl: UByte) {
    Type1(0x00u),
    Type2(0x01u),
    Type3(0x02u);

    companion object {
        fun getKey(vl:UByte): C18_Theme_Type = C18_Theme_Type.values().find { it.vl == vl }?: C18_Theme_Type.Type1
    }
}

enum class C18_Skin_Color(val vl: UByte) {
    White(0x00u),
    WhiteAndYellow(0x01u),
    Yellow(0x02u),
    Brown(0x03u),
    DarkBrown(0x04u),
    Black(0x05u),
    Other(0x06u);

    companion object {
        fun getKey(vl:UByte): C18_Skin_Color = C18_Skin_Color.values().find { it.vl == vl }?: White
    }
}

object C18SettingCMD {
    /*
        時間設定
    */
    fun getTimeSettingModel():UByteArray{
        var data = ubyteArrayOf()
        val calendar = Calendar.getInstance()
        data += calendar.get(Calendar.YEAR).toUShort().toUBytes()   //2byte
        data += (calendar.get(Calendar.MONTH) + 1).toUByte()        //1byte
        data += calendar.get(Calendar.DATE).toUByte()               //1byte
        data += calendar.get(Calendar.HOUR_OF_DAY).toUByte()        //1byte
        data += calendar.get(Calendar.MINUTE).toUByte()             //1byte
        data += calendar.get(Calendar.SECOND).toUByte()             //1byte
        data += calendar.get(Calendar.DAY_OF_WEEK).toUByte()        //1byte
        return data
    }

    /*
        心拍監視モード
        open:Boolean      スイッチ
        interval:Ubyte　　　 間隔時間
     */
    fun getHeartMonitorSettingModel(open:Boolean,interval:UByte) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 1 else 0
        data += openByte.toUByte()      //1byte
        data += interval                //1byte
        return data
    }

    /*
       紛失防ぐ機能
       open:Boolean      スイッチ
    */
    fun getAntiLostSettingModel(open:Boolean) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 2 else 0
        data += openByte.toUByte()      //1byte
        return data
    }

    /*
        個人情報
        height:UByte
        wight:UByte
        gender:UByte
        age:UByte
     */
    fun getUserInfoSettingModel(height:UByte,weight:UByte,gender:UByte,age:UByte) : UByteArray {
        var data = ubyteArrayOf()
        data += height                  //1byte
        data += weight                  //1byte
        data += gender                  //1byte
        data += age                     //1byte
        return data
    }

    /*
     画面の明るいさ
     brightLevel:UByte   3パターン(Low:0x00 Mid:0x01 Hight:0x02)
    */
    fun getDisplayBrightSettingModel(brightLevel:UByte) : UByteArray {
        var data = ubyteArrayOf()
        data += brightLevel             //1byte
        return data
    }

    /*
     腕を上げてスリープ解除するモード
     open:UByte
     */
    fun getRaiseScreenSettingModel(open: Boolean) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 1 else 0
        data += openByte.toUByte()      //1byte
        return data
    }

    /*
     心拍数アラート設定
     openSwitch:UByte
     hightHearth:UByte 最高心拍数アラート閾値 100-240
     lowHearth:UByte 最低心拍数アラート値閾値 30-60
     */
    fun getHeartAlarmSettingModel(open: Boolean,hightHearth:UByte,lowHearth:UByte) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 1 else 0
        data += openByte.toUByte()
        data += hightHearth
        data += lowHearth
        return data
    }

    /*
    座り過ぎアラート設定
    start_timeHour_1:
    start_timeMin_1 :
    end_timeHour_1  :
    end_timeMin_1   :
    start_timeHour_2:
    start_timeMin_2 :
    end_timeHour_2  :
    end_timeMin_2   :
    interval        :
    repeater        :
    isOpen          :
    week            :
    */
    fun getLongSiteSettingModel(longSite:C18_LongSite): UByteArray{

        var data = ubyteArrayOf()
        data += longSite.start_timeHour_1
        data += longSite.start_timeMin_1
        data += longSite.end_timeHour_1
        data += longSite.end_timeMin_1
        data += longSite.start_timeHour_2
        data += longSite.start_timeMin_2
        data += longSite.end_timeHour_2
        data += longSite.end_timeMin_2
        data += longSite.interval
        data += longSite.getRepeater()

        return data
    }

    /*
     出荷状態回復設定
     cmd1:UByte 固定値
     cmd2:UByte 固定値
     cmd3:UByte 固定値
     cmd4:UByte 固定値
    */
    fun getReSettingModel() : UByteArray {
        var data = ubyteArrayOf(0x52u,0x53u,0x59u,0x53u)
        return data
    }

    /*
     言語設定
     lang:UByte 言語(QOI_OS_Language参照)
     */
    fun getLanguageSettingModel(lan:C18_OS_Language) : UByteArray {
        var data = ubyteArrayOf()
        data += lan.vl
        return data
    }

    /*
     血圧範囲設定
     range:UByte 範囲(QOI_Blood_Range参照)
     */

    fun getBloodRanageSettingModel(range:C18_Blood_Range) : UByteArray {
        var data = ubyteArrayOf()
        data += range.vl
        return data
    }

    /*
     左右装着設定
     hand:UByte 左手OR右手(QOI_Hand_Wear参照)
     */
    fun getHandWearSettingModel(hand:C18_Hand_Wear) : UByteArray {
        var data = ubyteArrayOf()
        data += hand.vl
        return data
    }

    /*
     テーマ設定
     theme:UByte テーマタイプ(QOI_Theme_Type参照)
     */
    fun getThemeSettingModel(theme:C18_Theme_Type) : UByteArray {
        var data = ubyteArrayOf()
        data += theme.vl
        return data
    }

    /*
     皮膚色設定
     skinColor:UByte 色(QOI_Skin_Color参照)
     */
    fun getSkinColorSettingModel(skinColor:C18_Skin_Color) : UByteArray {
        var data = ubyteArrayOf()
        data += skinColor.vl
        return data
    }

    /*
     アラート設定 検索
     newAlarmModel:Object
     */
    fun selectAlarmTimeSetting() : UByteArray {
        var data = ubyteArrayOf()
        data += C18_AlarmTime_Action.Select.vl      //アラート設定の検索CMDの固定値
        return data
    }

    /*
     アラート設定 追加
     newAlarmModel:Object
     */
    fun addAlarmTimeSetting(newAlarmModel:C18_AlarmTime) : UByteArray {
        var data = ubyteArrayOf()
        data += C18_AlarmTime_Action.Add.vl         //アラート設定の追加CMDの固定値
        data += newAlarmModel.alarmtimeType
        data += newAlarmModel.hour
        data += newAlarmModel.minute
        data += newAlarmModel.repeater
        data += newAlarmModel.snoozeInterval
        return data
    }

    /*
     アラート設定 削除
     newAlarmModel:Object
     */
    fun delAlarmTimeSetting(oldAlarmTime: C18_AlarmTime) : UByteArray {
        var data = ubyteArrayOf()
        data += C18_AlarmTime_Action.Del.vl       //アラート設定の削除CMDの固定値
        data += oldAlarmTime.hour
        data += oldAlarmTime.minute
        return data
    }

    /*
     アラート設定 変更
     oldAlarmModel:Object
     newAlarmModel:Object
     */
    fun chanageAlarmTimeSetting(oldAlarmTime: C18_AlarmTime,newAlarmModel: C18_AlarmTime) : UByteArray {
        var data = ubyteArrayOf()
        data += C18_AlarmTime_Action.Change.vl       //アラート設定の変更CMDの固定値
        data += oldAlarmTime.hour
        data += oldAlarmTime.minute
        data += C18_AlarmTime_Type.WakeUp.vl
        data += newAlarmModel.hour
        data += newAlarmModel.minute
        data += newAlarmModel.repeater
        data += newAlarmModel.snoozeInterval
        return data
    }


    /*
     おやすみモード設定
     open:UByte        スイッチ
     start_timeHour:UByte    開始時刻:時
     start_timeHour:UByte    終了時刻:分
     end_timeHour:UByte    開始時刻:時
     end_timeHour:UByte    終了時刻:分
     */
    fun getSleepModeSettingModel(open: Boolean,startHour:UByte,startMin:UByte,endHour:UByte,endMin:UByte) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 1 else 0
        data += openByte.toUByte()
        data += startHour
        data += startMin
        data += endHour
        data += endMin
        return data
    }


}