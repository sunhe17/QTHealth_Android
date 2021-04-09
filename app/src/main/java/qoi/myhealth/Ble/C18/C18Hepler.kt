package qoi.myhealth.Ble.C18

import android.nfc.Tag
import android.util.Base64
import android.util.Log
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.C18.CMD.C18_HealthData_Key
import qoi.myhealth.Ble.C18.CMD.C18_OS_Language
import qoi.myhealth.Ble.C18.CMD.C18_Skin_Color
import qoi.myhealth.Ble.C18.CMD.C18_Theme_Type
import qoi.myhealth.Ble.C18.Model.*
import qoi.myhealth.Ble.Extension.get3Byte
import qoi.myhealth.Ble.Extension.toBool
import qoi.myhealth.Ble.Extension.toBoolByBit
import qoi.myhealth.Ble.Extension.toString
import qoi.myhealth.Ble.Extension.toTimeStampString
import qoi.myhealth.Ble.model.UserInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object C18Hepler {

    const val SecFrom30Year = 946684800
    private val TAG = "データ分析"


    // 接続要求の確認
    fun dealAckCode(ackData:ByteArray) : Boolean{
        var result = false
        var tRetCode = 0
        var offset = 0

        val buff = ByteBuffer.wrap(ackData)
        tRetCode = buff.get(offset).toInt()
        if (tRetCode == 0){
            result = true
        }
        return result
    }

    // 接続要求コードナンバー取得
    fun dealAckCodeNumber(ackData: ByteArray) : Int {
        var tRetCode:UByte = 0u

        val buffData = ByteBuffer.wrap(ackData)
        var offset = 0
        tRetCode = buffData.get(offset).toUByte()

        return tRetCode.toInt()
    }

    // アラーム情報を取得する
    fun dealAlarm(alarmData:ByteArray) : Map<BleIdentificationKey,Any>? {
        var offset = 0

        val buff = ByteBuffer.wrap(alarmData)
        val tOptType = buff.get(offset).toUByte()
        offset += 1

        val alarmAction = C18_AlarmTime_Action.getKey(tOptType)
        when (alarmAction) {
            C18_AlarmTime_Action.Add,C18_AlarmTime_Action.Del,C18_AlarmTime_Action.Change -> {
                val result:UByte = buff.get(offset).toUByte()
                return mapOf(BleIdentificationKey.C18_Alarm_Setting to result.toInt())
        }
            C18_AlarmTime_Action.Select -> {
                var tSupportAlarmNum:UByte = 0u
                var tSettedAlarmNum:UByte = 0u
                tSupportAlarmNum = buff.get(offset).toUByte()
                offset += 1
                tSettedAlarmNum = buff.get(offset).toUByte()
                offset += 1
                var alarmTimeArr = arrayListOf<C18_AlarmTime>()
                // 設定している情報があれば詳細情報もデバイスから取得する
                if (tSettedAlarmNum.toInt() > 0) {
                    var tAlarmType:UByte = 0u
                    var tAlarmHour:UByte = 0u
                    var tAlarmMin:UByte  = 0u
                    var tAlarmRepeat:UByte = 0u
                    var tAlarmDelayTime:UByte = 0u
                    // 設定分データを取得
                    repeat(tSettedAlarmNum.toInt()) {
                        tAlarmType = buff.get(offset).toUByte()
                        offset += 1
                        tAlarmHour = buff.get(offset).toUByte()
                        offset += 1
                        tAlarmMin  = buff.get(offset).toUByte()
                        offset += 1
                        tAlarmRepeat = buff.get(offset).toUByte()
                        offset += 1
                        tAlarmDelayTime = buff.get(offset).toUByte()

                        val alarmTime = C18_AlarmTime(C18_AlarmTime_Type.getKey(tAlarmType),tAlarmHour,tAlarmMin,tAlarmRepeat,tAlarmDelayTime)
                        alarmTimeArr.add(alarmTime)
                    }
                }
                return mapOf(BleIdentificationKey.C18_Alarm_Setting to alarmTimeArr)
            }
        }
        return null
    }

    // 各種ユーザーの設定をデバイスからbyte形式で取得し数値に変換
    fun dealDeviceInfo(infoData:ByteArray) : Map<BleIdentificationKey,Any>? {
        var tDevId:UShort = 0u
        var tDevVersionNum:UShort = 0u
        var tBatteryState:UByte = 0u
        var tBatteryNum:UByte = 0u
        var tBindState:UByte = 0u
        var tSyncFlag:UByte = 0u

        var offset = 0
        val buffData = ByteBuffer.wrap(infoData).order(ByteOrder.LITTLE_ENDIAN)

        tDevId = buffData.getShort(offset).toUShort()
        offset += 2
        tDevVersionNum = buffData.getShort(offset).toUShort()
        offset += 2
        tBatteryState = buffData.get(offset).toUByte()
        offset += 1
        tSyncFlag = buffData.get(offset).toUByte()
        offset += 1

        val sNo = (tDevVersionNum.toInt() shr 8) and 0xff
        val bNo = tDevVersionNum.toInt() and 0xff

        Log.d(TAG,"デバイスID：${tDevId} ファイムウェア：${sNo}.${bNo} バッテリ：${tBatteryNum}")
        return mapOf(BleIdentificationKey.C18_DevId to tDevId.toInt(),BleIdentificationKey.C18_DevVer to tDevVersionNum.toInt(),
                     BleIdentificationKey.C18_DevBatterStatus to tBatteryState.toInt(),BleIdentificationKey.C18_DevBatterNum to tBatteryNum.toInt())
    }

    fun dealUserSetInfo(userSetInfo:ByteArray) : Map<BleIdentificationKey,Any>? {
        var tTagetInfo_step:UInt = 0u
        var tTagetInfo_kcal:UInt = 0u
        var tTagetInfo_distance:UInt = 0u
        var tTageInfo_sleepHour:UByte = 0u
        var tTageInfo_sleepMin:UByte = 0u

        var tUserInfo_height:UByte = 0u
        var tUserInfo_weight:UByte = 0u
        var tUserInfo_gender:UByte = 0u
        var tUserInfo_age:UByte = 0u

        var tUnit_distance:UByte = 0u
        var tUnit_weight:UByte = 0u
        var tUnit_temp:UByte = 0u
        var tUnit_timeMode:UByte = 0u

        var tLongSite_start_timeHour1:UByte = 0u
        var tLongSite_start_timeMin1:UByte = 0u
        var tLongSite_end_timeHour1:UByte = 0u
        var tLongSite_end_timeMin1:UByte = 0u
        var tLongSite_start_timeHour2:UByte = 0u
        var tLongSite_start_timeMin2:UByte = 0u
        var tLongSite_end_timeHour2:UByte = 0u
        var tLongSite_end_timeMin2:UByte = 0u
        var tLongSite_interval:UByte = 0u
        var tLongSite_repeat:UByte = 0u

        var tLoseAlert_mode:UByte = 0u
        var tLoseAlert_RSSI:UByte = 0u
        var tLoseAlert_interval:UByte = 0u
        var tLoseAlert_extension:UByte = 0u
        var tLoseAlert_repeat:UByte = 0u

        var tAlertSwitch_all:UByte = 0u
        var tAlertSwitch_sub1:UByte = 0u
        var tAlertSwitch_sub2:UByte = 0u

        var tOther1_handWear:UByte = 0u
        var tOther1_heartAlert:UByte = 0u
        var tOther1_heartValue:UByte = 0u
        var tOther1_heartMon:UByte = 0u
        var tOther1_autoInterval:UByte = 0u

        var tOther2_lang:UByte = 0u
        var tOther2_raiseDisplay:UByte = 0u
        var tOther2_screenBright:UByte = 0u
        var tOther2_skinColor:UByte = 0u

        var tSleepMode_switch:UByte = 0u
        var tSleepMode_startTimeHour:UByte = 0u
        var tSleepMode_startTimeMin:UByte = 0u
        var tSleepMode_endTimeHour:UByte = 0u
        var tSleepMode_endTimeMin:UByte = 0u

        var offset = 0
        val buffData = ByteBuffer.wrap(userSetInfo).order(ByteOrder.LITTLE_ENDIAN)

        tTagetInfo_step = buffData.get3Byte(offset).toUInt()
        offset += 3
        tTagetInfo_kcal = buffData.get3Byte(offset).toUInt()
        offset += 3
        tTagetInfo_distance = buffData.get3Byte(offset).toUInt()
        offset += 3
        tTageInfo_sleepHour = buffData.get(offset).toUByte()
        offset += 1
        tTageInfo_sleepMin = buffData.get(offset).toUByte()
        offset += 1

        tUserInfo_height = buffData.get(offset).toUByte()
        offset += 1
        tUserInfo_weight = buffData.get(offset).toUByte()
        offset += 1
        tUserInfo_gender = buffData.get(offset).toUByte()
        offset += 1
        tUserInfo_age = buffData.get(offset).toUByte()
        offset += 1

        tUnit_distance = buffData.get(offset).toUByte()
        offset += 1
        tUnit_weight = buffData.get(offset).toUByte()
        offset += 1
        tUnit_temp = buffData.get(offset).toUByte()
        offset += 1
        tUnit_timeMode = buffData.get(offset).toUByte()
        offset += 1

        tLongSite_start_timeHour1 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_start_timeMin1 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_end_timeHour1 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_end_timeMin1 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_start_timeHour2 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_start_timeMin2 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_end_timeHour2 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_end_timeMin2 = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_interval = buffData.get(offset).toUByte()
        offset += 1
        tLongSite_repeat = buffData.get(offset).toUByte()
        offset += 1

        tLoseAlert_mode = buffData.get(offset).toUByte()
        offset += 1
        tLoseAlert_RSSI = buffData.get(offset).toUByte()
        offset += 1
        tLoseAlert_interval = buffData.get(offset).toUByte()
        offset += 1
        tLoseAlert_extension = buffData.get(offset).toUByte()
        offset += 1
        tLoseAlert_repeat = buffData.get(offset).toUByte()
        offset += 1

        tAlertSwitch_all = buffData.get(offset).toUByte()
        offset += 1
        tAlertSwitch_sub1 = buffData.get(offset).toUByte()
        offset += 1
        tAlertSwitch_sub2 = buffData.get(offset).toUByte()
        offset += 1

        tOther1_handWear = buffData.get(offset).toUByte()
        offset += 1
        tOther1_heartAlert = buffData.get(offset).toUByte()
        offset += 1
        tOther1_heartValue = buffData.get(offset).toUByte()
        offset += 1
        tOther1_heartMon = buffData.get(offset).toUByte()
        offset += 1
        tOther1_autoInterval = buffData.get(offset).toUByte()
        offset += 1

        tOther2_lang = buffData.get(offset).toUByte()
        offset += 1
        tOther2_raiseDisplay = buffData.get(offset).toUByte()
        offset += 1
        tOther2_screenBright = buffData.get(offset).toUByte()
        offset += 1
        tOther2_skinColor = buffData.get(offset).toUByte()
        offset += 1

        //予備3Byteあるため
        offset += 3

        tSleepMode_switch = buffData.get(offset).toUByte()
        offset += 1
        tSleepMode_startTimeHour = buffData.get(offset).toUByte()
        offset += 1
        tSleepMode_startTimeMin = buffData.get(offset).toUByte()
        offset += 1
        tSleepMode_endTimeHour = buffData.get(offset).toUByte()
        offset += 1
        tSleepMode_endTimeMin = buffData.get(offset).toUByte()
        offset += 1

        val tagetInfo = C18_TagetInfo(tTagetInfo_step.toInt(),tTagetInfo_kcal.toInt(),tTagetInfo_distance.toInt(),tTageInfo_sleepHour.toInt(),tTageInfo_sleepMin.toInt())
        val userInfo = UserInfo(tUserInfo_height.toInt(),tUserInfo_weight.toInt(),tUserInfo_gender.toInt(),tUserInfo_age.toInt())
        userInfo.skinColor = C18_Skin_Color.getKey(tOther2_skinColor)
        val unitInfo = C18_UnitInfo(tUnit_distance.toInt(),tUnit_weight.toInt(),tUnit_temp.toInt(),tUnit_timeMode.toInt())
        val longSiteInfo = C18_LongSite(tLongSite_start_timeHour1,tLongSite_start_timeMin1,tLongSite_end_timeHour1,tLongSite_end_timeMin1,tLongSite_start_timeHour2,tLongSite_start_timeMin2,tLongSite_end_timeHour2,tLongSite_end_timeMin2)
        longSiteInfo.interval = tLongSite_interval
        longSiteInfo.setRepeater(tLongSite_repeat)
        val userSetInfo = C18_UserSettingInfo
        userSetInfo.tagetInfo = tagetInfo
        userSetInfo.userInfo = userInfo
        userSetInfo.unitInfo = unitInfo
        userSetInfo.longSiteInfo = longSiteInfo
        userSetInfo.handWear = tOther1_handWear.toInt()
        userSetInfo.heartAlert_isOpen = tOther1_heartAlert.toBool()
        userSetInfo.heartAlert_value = tOther1_heartValue.toInt()
        userSetInfo.heartMon_mode = tOther1_heartMon.toBool()
        userSetInfo.heartMon_AutoMode_interval = tOther1_autoInterval.toInt()
        userSetInfo.os_lang = C18_OS_Language.getKey(tOther2_lang)
        userSetInfo.raiseDisplay = tOther2_raiseDisplay.toBool()
        userSetInfo.screenBright = tOther2_screenBright.toInt()

        return mapOf(BleIdentificationKey.C18_UserSetInfo to userSetInfo)
    }

    // デバイスのサポート機能の取得
    fun dealSupport(supportData:ByteArray) : Map<BleIdentificationKey,Any>? {
        var tMain1:UByte = 0u
        var tMain2:UByte = 0u
        var tAlarmNum:UByte = 0u
        var tAlarmType:UByte = 0u
        var tInfoAncs1:UByte = 0u
        var tInfoAncs2:UByte = 0u
        var tOther1:UByte = 0u
        var tOther2:UByte = 0u
        var tOther3:UByte = 0u
        var tOther4:UByte = 0u
        var tOther5:UByte = 0u
        var tSportModeType:UByte = 0u
        var tMtu:UShort = 0u

        var offset = 0
        val buffData = ByteBuffer.wrap(supportData).order(ByteOrder.LITTLE_ENDIAN)

        //dataのサイズが14byte未満の場合、サポート機能がないということ
        if (supportData.size < 14) {
            return null
        }
        tMain1 = buffData.get(offset).toUByte()
        offset += 1
        tMain2 = buffData.get(offset).toUByte()
        offset += 1
        tAlarmNum = buffData.get(offset).toUByte()
        offset += 1
        tAlarmType = buffData.get(offset).toUByte()
        offset += 1
        tInfoAncs1 = buffData.get(offset).toUByte()
        offset += 1
        tInfoAncs2 = buffData.get(offset).toUByte()
        offset += 1
        tOther1 = buffData.get(offset).toUByte()
        offset += 1
        tOther2 = buffData.get(offset).toUByte()
        offset += 1
        tOther3 = buffData.get(offset).toUByte()
        offset += 1
        tOther4 = buffData.get(offset).toUByte()
        offset += 1
        tOther5 = buffData.get(offset).toUByte()
        offset += 1
        tSportModeType = buffData.get(offset).toUByte()
        offset += 1
        tMtu = buffData.getShort(offset).toUShort()
        offset += 2

        val supportInfo = C18_DeviceSupportInfo
        supportInfo.isSupportStep = tMain1.toBoolByBit(7)
        supportInfo.isSupportSleep = tMain1.toBoolByBit(6)
        supportInfo.isSupportRealSync = tMain1.toBoolByBit(5)
        supportInfo.isSupportOTA = tMain1.toBoolByBit(4)
        supportInfo.isSupportHeart = tMain1.toBoolByBit(3)
        supportInfo.isSupportInfoPush = tMain1.toBoolByBit(2)
        supportInfo.isSupportLang = tMain1.toBoolByBit(1)
        supportInfo.isSupportBlood = tMain1.toBoolByBit(0)

        supportInfo.isSupportHeartAlarm = tMain2.toBoolByBit(7)
        supportInfo.isSupportBloodAlarm = tMain2.toBoolByBit(6)
        supportInfo.isSupportRealECGSync = tMain2.toBoolByBit(5)
        supportInfo.isSupportHistoryECGSync = tMain2.toBoolByBit(4)
        supportInfo.isSupportOO = tMain2.toBoolByBit(3)
        supportInfo.isSupportRespiratoryRate = tMain2.toBoolByBit(2)
        supportInfo.isSupportHRV = tMain2.toBoolByBit(1)
        supportInfo.isSupportMultSportMode = tMain2.toBoolByBit(0)

        supportInfo.isSupportAlarmNum = tAlarmNum.toInt()
        supportInfo.isSupportAlarmType1 = tAlarmType.toBoolByBit(7)
        supportInfo.isSupportAlarmType2 = tAlarmType.toBoolByBit(6)
        supportInfo.isSupportAlarmType3 = tAlarmType.toBoolByBit(5)
        supportInfo.isSupportAlarmType4 = tAlarmType.toBoolByBit(4)
        supportInfo.isSupportAlarmType5 = tAlarmType.toBoolByBit(3)
        supportInfo.isSupportAlarmType6 = tAlarmType.toBoolByBit(2)
        supportInfo.isSupportAlarmType7 = tAlarmType.toBoolByBit(1)
        supportInfo.isSupportAlarmType1 = tAlarmType.toBoolByBit(0)

        supportInfo.isSupport_LongSite = tOther1.toBoolByBit(7)
        supportInfo.isSupport_Antilose = tOther1.toBoolByBit(6)
        supportInfo.isSupport_findPhone = tOther1.toBoolByBit(5)
        supportInfo.isSupport_findDev = tOther1.toBoolByBit(4)
        supportInfo.isSupport_FactorySettings = tOther1.toBoolByBit(3)
        supportInfo.isSupport_BloodLevelSetting = tOther1.toBoolByBit(2)
        supportInfo.isSupport_NotDisturb = tOther1.toBoolByBit(1)
        supportInfo.isSupport_turnWrist = tOther1.toBoolByBit(0)

        supportInfo.isSupport_skinsetting = tOther2.toBoolByBit(7)
        supportInfo.isSupport_ECGdiagnosis = tOther2.toBoolByBit(2)
        supportInfo.isSupportCVRR = tOther3.toBoolByBit(5)
        supportInfo.isSupport_MainPageSetting = tOther3.toBoolByBit(2)
        supportInfo.isSupport_TestTemp = tOther3.toBoolByBit(0)

        Log.d(TAG,"isSupportStep：${supportInfo.isSupportStep} isSupportSleep：${supportInfo.isSupportSleep} isSupportRealSync：${supportInfo.isSupportRealSync} " + "isSupportOTA：${supportInfo.isSupportOTA} isSupportHeart：${supportInfo.isSupportHeart} isSupportInfoPush：${supportInfo.isSupportInfoPush} isSupportLang：${supportInfo.isSupportLang} " + "isSupportBlood:${supportInfo.isSupportBlood} " +
                "isSupportHeartAlarm：${supportInfo.isSupportHeartAlarm} isSupportBloodAlarm：${supportInfo.isSupportBloodAlarm} isSupportRealECGSync：${supportInfo.isSupportRealECGSync} \" + \"isSupportHistoryECGSync：${supportInfo.isSupportHistoryECGSync} isSupportOO：${supportInfo.isSupportOO} isSupportRespiratoryRate：${supportInfo.isSupportRespiratoryRate} isSupportHRV：${supportInfo.isSupportHRV} isSupportMultSportMode:${supportInfo.isSupportMultSportMode} " +
                "isSupportAlarmNum：${supportInfo.isSupportAlarmNum} isSupportAlarmType1：${supportInfo.isSupportAlarmType1} isSupportAlarmType2：${supportInfo.isSupportAlarmType2} \" + \"isSupportAlarmType3：${supportInfo.isSupportAlarmType3} isSupportAlarmType4：${supportInfo.isSupportAlarmType4} isSupportAlarmType5：${supportInfo.isSupportAlarmType5} isSupportAlarmType6：${supportInfo.isSupportAlarmType6} isSupportAlarmType7:${supportInfo.isSupportAlarmType7} " +
                "isSupport_LongSite：${supportInfo.isSupport_LongSite} isSupport_Antilose：${supportInfo.isSupport_Antilose} isSupport_findPhone：${supportInfo.isSupport_findPhone} \" + \"isSupport_findDev：${supportInfo.isSupport_findDev} isSupport_FactorySettings：${supportInfo.isSupport_FactorySettings} isSupport_BloodLevelSetting：${supportInfo.isSupport_BloodLevelSetting} isSupport_NotDisturb：${supportInfo.isSupport_NotDisturb} isSupport_turnWrist:${supportInfo.isSupport_turnWrist} " +
                "isSupport_skinsetting：${supportInfo.isSupport_skinsetting} isSupport_ECGdiagnosis：${supportInfo.isSupport_ECGdiagnosis} isSupportCVRR：${supportInfo.isSupportCVRR} \" + \"isSupport_MainPageSetting：${supportInfo.isSupport_MainPageSetting} isSupport_TestTemp：${supportInfo.isSupport_TestTemp}")
        return mapOf(BleIdentificationKey.C18_SupportInfo to supportInfo)
    }

    fun dealSwitchStatus(switchStatus:ByteArray) : Map<BleIdentificationKey,Any>? {
        var tAlertSwitchAll:UByte = 0u
        var tAlertSwitch_1:UByte = 0u
        var tAlertSwtich_2:UByte = 0u
        var tOtherSwitch_1:UByte = 0u
        var tOtherSwitch_2:UByte = 0u

        var offset = 0
        val buffData = ByteBuffer.wrap(switchStatus).order(ByteOrder.LITTLE_ENDIAN)

        tAlertSwitchAll = buffData.get(offset).toUByte()
        offset += 1
        tAlertSwitch_1 = buffData.get(offset).toUByte()
        offset += 1
        tAlertSwtich_2 = buffData.get(offset).toUByte()
        offset += 1
        tOtherSwitch_1 = buffData.get(offset).toUByte()
        offset += 1
        tOtherSwitch_2 = buffData.get(offset).toUByte()
        offset += 1

        val switchStatusInfo = C18_SwitchStatusInfo
        switchStatusInfo.sitDown_Switch = tOtherSwitch_2.toBoolByBit(7)
        switchStatusInfo.loss_Switch = tOtherSwitch_2.toBoolByBit(6)
        switchStatusInfo.sleep_Switch = tOtherSwitch_2.toBoolByBit(5)
        switchStatusInfo.heart_Switch = tOtherSwitch_2.toBoolByBit(4)
        switchStatusInfo.blood_Switch = tOtherSwitch_2.toBoolByBit(3)
        switchStatusInfo.alarm_Switch = tOtherSwitch_2.toBoolByBit(2)
        switchStatusInfo.wakeUp_Switch = tOtherSwitch_2.toBoolByBit(0)

        Log.d(TAG,"sitDown_Switch：${switchStatusInfo.sitDown_Switch} loss_Switch：${switchStatusInfo.loss_Switch} sleep_Switch：${switchStatusInfo.sleep_Switch} heart_Switch：${switchStatusInfo.heart_Switch} loss_Switch：${switchStatusInfo.loss_Switch} alarm_Switch：${switchStatusInfo.alarm_Switch} wakeUp_Switch：${switchStatusInfo.wakeUp_Switch}")

        return mapOf(BleIdentificationKey.C18_SwitchStatusInfo to switchStatusInfo)
    }

    // デバイスのマックアドレスを設定
    fun dealDeviceMac(macData:ByteArray) : Map<BleIdentificationKey,Any>? {
        var tMac0:UByte = 0u
        var tMac1:UByte = 0u
        var tMac2:UByte = 0u
        var tMac3:UByte = 0u
        var tMac4:UByte = 0u
        var tMac5:UByte = 0u

        val buffData = ByteBuffer.wrap(macData).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0

        tMac0 = buffData.get(offset).toUByte()
        offset += 1
        tMac1 = buffData.get(offset).toUByte()
        offset += 1
        tMac2 = buffData.get(offset).toUByte()
        offset += 1
        tMac3 = buffData.get(offset).toUByte()
        offset += 1
        tMac4 = buffData.get(offset).toUByte()
        offset += 1
        tMac5 = buffData.get(offset).toUByte()
        offset += 1

        val macStr = "%s:%s:%s:%s:%s:%s".format(tMac5.toString(16),tMac4.toString(16),tMac3.toString(16),tMac2.toString(16),tMac1.toString(16),tMac0.toString(16))
        Log.d(TAG,"デバイスMac:${macStr}")
        return mapOf(BleIdentificationKey.C18_DeviceMacInfo to macStr)
    }

    //デバイス名の設定
    fun dealDeviceName(deviceData:ByteArray) : Map<BleIdentificationKey,Any>? {
        val deviceName:String = deviceData.toString(Charsets.UTF_8)
        Log.d(TAG,"デバイス名前:${deviceName}")
        return mapOf(BleIdentificationKey.C18_DeviceNameInfo to deviceName)
    }

    // デバイスのホーム画面表示設定の情報を取得
    fun dealMainTheme(themeData:ByteArray) : Map<BleIdentificationKey,Any>? {
        var tTotalIndex:UByte = 0u
        var tCurIndex:UByte = 0u

        if (themeData.size < 2){
            return null
        }
        val buffData = ByteBuffer.wrap(themeData).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0

        tTotalIndex = buffData.get(offset).toUByte()
        offset += 1
        tCurIndex = buffData.get(offset).toUByte()
        offset += 1

        Log.d(TAG,"テーマ総数:${tTotalIndex.toInt()} 現在設定してるテーマ:${tCurIndex.toInt()}")
        val themeType = C18_Theme_Type.getKey(tCurIndex)
        return mapOf(BleIdentificationKey.C18_MainThemeInfo to themeType)
    }



    fun dealSyncHealthAck(healthData:ByteArray) : Map<BleIdentificationKey,Int> {
        var tHistoryNum:UShort = 0u
        var tHistoryTotalByte:UInt = 0u
        var tHistoryTotalBlock:UInt = 0u

        var offset = 0
        val buffData = ByteBuffer.wrap(healthData).order(ByteOrder.LITTLE_ENDIAN)

        tHistoryNum = buffData.getShort().toUShort()
        offset += 2
        buffData.position(offset)
        if (tHistoryNum.toInt() > 0){
            tHistoryTotalBlock = buffData.getInt().toUInt()
            offset += 4
            buffData.position(offset)
            tHistoryTotalByte = buffData.getInt().toUInt()
            offset += 4
            buffData.position(offset)
        }
        return mapOf(BleIdentificationKey.C18_HistoryNum to tHistoryNum.toInt(),BleIdentificationKey.C18_HistoryTotalByte to tHistoryTotalByte.toInt(),BleIdentificationKey.C18_HistoryBlock to tHistoryTotalBlock.toInt())
    }

    fun dealBlockSureData(blockSureData:ByteArray): Map<BleIdentificationKey,Int> {
        var tHistoryNum:UShort = 0u
        var tHistoryTotalCrc:UShort = 0u
        var tHistoryTotalByte:UShort = 0u

        var offset = 0

        val buffData = ByteBuffer.wrap(blockSureData).order(ByteOrder.LITTLE_ENDIAN)
        tHistoryNum = buffData.getShort(offset).toUShort()
        offset += 2
        tHistoryTotalByte = buffData.getShort(offset).toUShort()
        offset += 2
        tHistoryTotalCrc = buffData.getShort(offset).toUShort()

        return mapOf(BleIdentificationKey.C18_HistoryNum to tHistoryNum.toInt(),BleIdentificationKey.C18_HistoryTotalByte to tHistoryTotalByte.toInt(),BleIdentificationKey.C18_HistoryTotalCrc to tHistoryTotalCrc.toInt())
    }

    // 過去の健康データを取得
    fun dealHistoryData(historyData:ByteArray,keyType:C18_HealthData_Key){
        val secondsFromGMT = C18Utils.getSecondsFromGMT()
        val buffData = ByteBuffer.wrap(historyData).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0


        when (keyType) {

            //運動
            C18_HealthData_Key.Step -> {
                var tStartTime:UInt = 0u
                var tEndTime:UInt = 0u
                var tStep:UShort = 0u
                var tCal:UShort = 0u
                var tDis:UShort = 0u

                while (offset < historyData.count()){
                    tStartTime = buffData.getInt(offset).toUInt()
                    offset += 4
                    tEndTime = buffData.getInt(offset).toUInt()
                    offset += 4
                    tStep = buffData.getShort(offset).toUShort()
                    offset += 2
                    tCal = buffData.getShort(offset).toUShort()
                    offset += 2
                    tDis = buffData.getShort(offset).toUShort()
                    offset += 2

                    tStartTime = tStartTime - secondsFromGMT.toUInt() + SecFrom30Year.toUInt() //UNIX時間を計算(単位:秒)
                    val startTimeDate = Date(tStartTime.toLong() * 1000)   //秒からミリ秒変更
                    tEndTime = tEndTime - secondsFromGMT.toUInt() + SecFrom30Year.toUInt()
                    val endTimeDate = Date(tEndTime.toLong() * 1000)

                    Log.d(TAG,"タイマー：${startTimeDate.toTimeStampString()} - ${endTimeDate.toTimeStampString()} 歩数：$tStep 距離：$tDis カロリー：$tCal")
                }
            }

            //睡眠
            C18_HealthData_Key.Sleep -> {
                var tSleepHead:UShort = 0u
                var tAllLen:UShort = 0u
                var tStartTime:UInt = 0u
                var tEndTime:UInt = 0u
                var tDeepSleepNum:UShort = 0u
                var tLightSleepNum:UShort = 0u
                var tDeepSleepTotalMin:UShort = 0u
                var tLightSleepTotalMin:UShort = 0u

                while (offset < historyData.count()){
                    tSleepHead = buffData.getShort(offset).toUShort()
                    offset += 2
                    tAllLen = buffData.getShort(offset).toUShort()
                    offset += 2
                    tStartTime = buffData.getInt(offset).toUInt()
                    offset += 4
                    tEndTime = buffData.getInt(offset).toUInt()
                    offset += 4
                    tDeepSleepNum = buffData.getShort(offset).toUShort()
                    offset += 2
                    tLightSleepNum = buffData.getShort(offset).toUShort()
                    offset += 2
                    tDeepSleepTotalMin = buffData.getShort(offset).toUShort()
                    offset += 2
                    tLightSleepTotalMin = buffData.getShort(offset).toUShort()
                    offset += 2

                    val tSubOffset = offset
                    var tSingleType:UByte = 0u   //深い眠り ：0xF1 浅い眠り：0xF2
                    var tSingleStartTime:UInt = 0u
                    var tSingleLenSec:UInt = 0u

                    while (offset < tSubOffset + tAllLen.toInt() - 20) {
                        tSingleType = buffData.get(offset).toUByte()
                        offset += 1
                        tSingleStartTime = buffData.getInt(offset).toUInt()
                        offset += 4
                        tSingleLenSec = buffData.get3Byte(offset).toUInt()
                        offset += 3

                        tSingleStartTime = tSingleStartTime - secondsFromGMT.toUInt() + SecFrom30Year.toUInt() //UNIX時間を計算(単位:秒)
                        val tSingleEndTime = tSingleStartTime + tSingleLenSec
                        val tSingleStartDate = Date(tSingleStartTime.toLong() * 1000)   //秒からミリ秒変更
                        val tSingleEndDate = Date(tSingleEndTime.toLong() * 1000)

                        Log.d(TAG,"タイマー：${tSingleStartDate.toTimeStampString()} - ${tSingleEndDate.toTimeStampString()} 睡眠Type：$tSingleType")
                    }
                }
            }

            //心拍
            C18_HealthData_Key.Heart -> {
                var tStartTime:UInt = 0u
                var tHeartNum:UByte = 0u

                while (offset < historyData.count()) {
                    tStartTime = buffData.getInt(offset).toUInt()
                    offset += 4

                    offset += 1 //モードのByte

                    tHeartNum = buffData.get(offset).toUByte()
                    offset += 1

                    tStartTime = tStartTime - secondsFromGMT.toUInt() + SecFrom30Year.toUInt() //UNIX時間を計算(単位:秒)
                    val startTimeDate = Date(tStartTime.toLong() * 1000)   //秒からミリ秒変更

                    Log.d(TAG,"タイマー：${startTimeDate.toTimeStampString()}} 心拍数：$tHeartNum")
                }
            }

            //血圧
            C18_HealthData_Key.Blood -> {
                var tStartTime:UInt = 0u
                var tSBP:UByte = 0u
                var tDBP:UByte = 0u

                while (offset < historyData.count()) {
                    tStartTime = buffData.getInt(offset).toUInt()
                    offset += 4

                    offset += 1 //モードのByte

                    tSBP = buffData.get(offset).toUByte()
                    offset += 1

                    tDBP = buffData.get(offset).toUByte()
                    offset += 1

                    offset += 1 //不明のByte

                    tStartTime = tStartTime - secondsFromGMT.toUInt() + SecFrom30Year.toUInt() //UNIX時間を計算(単位:秒)
                    val startTimeDate = Date(tStartTime.toLong() * 1000)   //秒からミリ秒変更

                    Log.d(TAG,"タイマー：${startTimeDate.toTimeStampString()}} 高血圧：${tSBP.toInt()} 低血圧:${tDBP.toInt()}")
                }
            }

            //他データ
            C18_HealthData_Key.MultData -> {
                var tStartTime:UInt = 0u
                var tStep:UShort = 0u
                var tHeartNum:UByte = 0u
                var tSBP:UByte = 0u
                var tDBP:UByte = 0u
                var tOO:UByte  = 0u
                var tHuxiRate:UByte = 0u
                var tHrv:UByte = 0u
                var tCVRR:UByte = 0u
                var tTempInt:UByte = 0u
                var tTempFloat:UByte = 0u

                while (offset < historyData.count()) {
                    tStartTime = buffData.getInt(offset).toUInt()
                    offset += 4
                    tStep = buffData.getShort(offset).toUShort()
                    offset += 2
                    tHeartNum = buffData.get(offset).toUByte()
                    offset += 1
                    tSBP = buffData.get(offset).toUByte()
                    offset += 1
                    tDBP = buffData.get(offset).toUByte()
                    offset += 1
                    tOO = buffData.get(offset).toUByte()
                    offset += 1
                    tHuxiRate = buffData.get(offset).toUByte()
                    offset += 1
                    tHrv = buffData.get(offset).toUByte()
                    offset += 1
                    tCVRR = buffData.get(offset).toUByte()
                    offset += 1
                    tTempInt = buffData.get(offset).toUByte()
                    offset += 1
                    tTempFloat = buffData.get(offset).toUByte()
                    offset += 1

                    offset += 5  // 予備Byteのため

                    tStartTime = tStartTime - secondsFromGMT.toUInt() + SecFrom30Year.toUInt() //UNIX時間を計算(単位:秒)
                    val startTimeDate = Date(tStartTime.toLong() * 1000)   //秒からミリ秒変更
                    Log.d(TAG,"タイマー:${startTimeDate.toTimeStampString()}歩数：$tStep 心拍：$tHeartNum 高血圧：$tSBP 低血圧：$tDBP SPO2：$tOO 呼吸数：$tHuxiRate HRV：$tHrv CVRR:$tCVRR 体温(整数部分):$tTempInt 体温(小数部分)：$tTempFloat")
                }
            }
        }
    }

    fun dealRealDataStep(stepData:ByteArray) {
        var tStep:UShort = 0u
        var tDis:UShort = 0u
        var tCal:UShort = 0u

        val buffData = ByteBuffer.wrap(stepData).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0

        tStep = buffData.getShort(offset).toUShort()
        offset += 2
        tDis = buffData.getShort(offset).toUShort()
        offset += 2
        tCal = buffData.getShort(offset).toUShort()
        offset += 2

        Log.d(TAG,"歩数:${tStep} 距離:${tDis} カロリー:${tCal}")
    }

    fun dealRealDataBlood(bloodData:ByteArray) {
        var tSystolicB:UByte = 0u
        var tDiastolicB:UByte = 0u
        var tHeartNum:UByte = 0u

        val buffData = ByteBuffer.wrap(bloodData).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0

        tSystolicB = buffData.get(offset).toUByte()
        offset += 1
        tDiastolicB = buffData.get(offset).toUByte()
        offset += 1
        tHeartNum = buffData.get(offset).toUByte()
        offset += 1

        Log.d(TAG,"SBP:${tSystolicB} DBP:${tDiastolicB} 心拍:${tHeartNum}")
    }

    fun dealRealDataSpo2(spo2Data:ByteArray) {
        var tSpo2Num:UByte = 0u

        val buffData = ByteBuffer.wrap(spo2Data).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0

        tSpo2Num = buffData.get(offset).toUByte()
        offset += 1

        Log.d(TAG,"SPO2:${tSpo2Num}")
    }

    fun dealRealDataHeart(heartData:ByteArray) {
        var tHeartNum:UByte = 0u

        val buffData = ByteBuffer.wrap(heartData).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0

        tHeartNum = buffData.get(offset).toUByte()
        offset += 1

        Log.d(TAG,"心拍:${tHeartNum}")
    }

    fun dealRealDataECG(ecgData:ByteArray) {
        var tEcgValue:Int = 0
        var tEcgArray: MutableList<Int> = mutableListOf<Int>()
        val buffData = ByteBuffer.wrap(ecgData).order(ByteOrder.LITTLE_ENDIAN)
        var offset = 0

        while (offset < ecgData.size) {
            tEcgValue += buffData.get().toUByte().toInt()
            tEcgValue += buffData.get(1).toUByte().toInt() shl 8
            tEcgValue += buffData.get(2).toUByte().toInt() shl 16

            if (buffData.get(2).toInt() shr 7 != 0){
                tEcgValue = tEcgValue or 0xff000000.toInt()
            }
            offset += 3
            tEcgArray.add(tEcgValue)
        }

        Log.d(TAG,"ECG Values:${tEcgArray}}")
    }
}