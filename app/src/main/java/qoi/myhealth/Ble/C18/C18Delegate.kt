package qoi.myhealth.Ble.C18

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import qoi.myhealth.Ble.*
import qoi.myhealth.Ble.C18.CMD.*
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime
import qoi.myhealth.Ble.C18.Model.C18_SleepInfo
import qoi.myhealth.Ble.model.UserInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.concurrent.schedule


enum class QOISendState{
    SendState_idle,
    SendState_ing
}

class C18Delegate(mGatt: BluetoothGatt) :
    DeviceDelegate(mGatt) {

    private var taskQueue:MutableList<QOISendPacket> = mutableListOf()
    private var taskState:QOISendState = QOISendState.SendState_idle

    private var mRecvData:ByteArray? = null
    private var curSyncHealthKeyType:C18_HealthData_Key? = null
    private var willRecvAllLen:Int? = null
    private var sendCallback:SendCallBack? = null

    private fun pushTask(task:QOISendPacket){
        taskQueue.add(task)
        if (taskState == QOISendState.SendState_idle){
            taskRun()
        }
    }

    private fun popTask(){
        taskState = QOISendState.SendState_idle
        if (taskQueue.count() > 0) {
            taskQueue.removeAt(0)
            taskRun()
        }
    }

    private fun taskRun(){
        if (taskQueue.count() > 0) {
            val firPacket = taskQueue.first()
            taskState = QOISendState.SendState_ing
            sendDataToDev(firPacket)
        }
    }

    private fun sendDataToDev(sendPacket: QOISendPacket){
        val tWillData = sendPacket.willSend()
        if (mGatt != null) {
            var wChar = mGatt!!.getService(ServiceUUID.C18_SERVICE.uuid).getCharacteristic(ServiceUUID.C18_kWrite1.uuid)
            wChar.setValue(tWillData)
            val result = mGatt!!.writeCharacteristic(wChar)
            println("sendDataToDev = $result")
        }
    }

    private fun sendAckToDev(cmdType: QOI_CMD_TYPE,keyTpe:UByte,ackCode:UByte) {
        val sendPacket = QOISendPacket(cmdType,keyTpe, ubyteArrayOf(ackCode))
        if (mGatt != null) {
            var wChar = mGatt!!.getService(ServiceUUID.C18_SERVICE.uuid).getCharacteristic(ServiceUUID.C18_kWrite1.uuid)
            wChar.setValue(sendPacket.willSend())
            wChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            val result = mGatt!!.writeCharacteristic(wChar)
            println("sendAckToDev = $result")
        }
    }

    private fun checkCMDKey(cmdType: QOI_CMD_TYPE?,key:UByte) : Boolean{
        var result = false
        if (taskQueue.count() > 0) {
            val task = taskQueue.first()
            result = task.cmd_type == cmdType && task.key_Type == key
        }
        return result
    }

    override fun recvDataUnpacket(recvData: ByteArray) {
        val buffData = ByteBuffer.wrap(recvData).order(ByteOrder.LITTLE_ENDIAN)
        var tOffset = 0
        val tCmd:QOI_CMD_TYPE? = QOI_CMD_TYPE.getType(buffData.get().toUByte())
        tOffset += 1
        buffData.position(tOffset)
        val tKey:UByte = buffData.get().toUByte()
        tOffset += 1
        buffData.position(tOffset)

        val tDataLen = buffData.getShort()
        tOffset += 2
        buffData.position(tOffset)
        val isFiristTask = checkCMDKey(tCmd,tKey)

        when (tCmd) {
            /*
            　* 0x01 基本設定
　　　　　　　　　　  */
            QOI_CMD_TYPE.CMD_Setting -> {
                val tKey = C18_Setting_Key.getKey(tKey)
                when (tKey) {
                    C18_Setting_Key.Time,C18_Setting_Key.Antilose,C18_Setting_Key.UserInfo,C18_Setting_Key.LongSite,C18_Setting_Key.DisplayBright,
                    C18_Setting_Key.RaiseScreen,C18_Setting_Key.HeartAlarm,C18_Setting_Key.ReSet,C18_Setting_Key.Language,C18_Setting_Key.HandWear,
                    C18_Setting_Key.BloodRange,C18_Setting_Key.MainTheme,C18_Setting_Key.SkinColor,C18_Setting_Key.SleepMode-> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val result = C18Hepler.dealAckCode(subData)
                        if (isFiristTask) {
                            if (result){
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,null)
                            }else{
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Failed,null)
                            }
                        }
                    }

                    C18_Setting_Key.Alarm -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val dicInfo = C18Hepler.dealAlarm(subData)
                        if (isFiristTask) {
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,dicInfo)
                        }
                    }
                }
            }

            /*
            　* 0x02 基本情報取得
　　　　　　　　　　  */
            QOI_CMD_TYPE.CMD_Get -> {
                val tKey = C18_Get_Key.getKey(tKey)
                when (tKey) {
                    C18_Get_Key.DevInfo -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val devInfo = C18Hepler.dealDeviceInfo(subData)
                        if (isFiristTask) {
                            taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,devInfo)
                        }
                    }

                    C18_Get_Key.UserSetInfo -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val userSetInfo = C18Hepler.dealUserSetInfo(subData)
                        if (isFiristTask) {
                            taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,userSetInfo)
                        }
                    }

                    C18_Get_Key.Support -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val supportInfo = C18Hepler.dealSupport(subData)
                        if (isFiristTask) {
                            taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,supportInfo)
                        }
                    }

                    C18_Get_Key.SwitchStatus -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val switchStatusInfo = C18Hepler.dealSwitchStatus(subData)
                        if (isFiristTask) {
                            taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,switchStatusInfo)
                        }
                    }

                    C18_Get_Key.Mac -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val deviceMacInfo = C18Hepler.dealDeviceMac(subData)
                        if (isFiristTask) {
                            taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,deviceMacInfo)
                        }
                    }

                    C18_Get_Key.Name -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val deviceNameInfo = C18Hepler.dealDeviceName(subData)
                        if (isFiristTask) {
                            taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,deviceNameInfo)
                        }
                    }

                    C18_Get_Key.MainTheme -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val mainTheme = C18Hepler.dealMainTheme(subData)
                        if (isFiristTask) {
                            taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,mainTheme)
                        }
                    }
                }
            }

            /*
            *0x03 APP制御コマンド
            */
            QOI_CMD_TYPE.CMD_AppControl -> {
                val tKey = C18_AppControl_Key.getKey(tKey)
                when (tKey) {
                    C18_AppControl_Key.FindDev,C18_AppControl_Key.BloodSwitch,C18_AppControl_Key.HeartSwitch -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val result = C18Hepler.dealAckCodeNumber(subData)
                        if (isFiristTask) {
                            if (result == 0 || result == 2){
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,null)
                            }else {
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Failed,null)
                            }
                        }
                    }

                    C18_AppControl_Key.WaveSync,C18_AppControl_Key.RealSync-> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val result = C18Hepler.dealAckCode(subData)
                        if (isFiristTask) {
                            if (result){
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,null)
                            }else{
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Failed,null)
                            }
                        }
                    }

                }
            }


            /*
            * 0x05 健康データ同期
            */
            QOI_CMD_TYPE.CMD_HealthData -> {
                val tKey = C18_HealthData_Key.getKey(tKey)
                when (tKey) {

                    C18_HealthData_Key.Step,C18_HealthData_Key.Sleep,C18_HealthData_Key.Blood,
                        C18_HealthData_Key.Heart,C18_HealthData_Key.MultData -> {
                        mRecvData = byteArrayOf()
                        curSyncHealthKeyType = tKey
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        val disInfo = C18Hepler.dealSyncHealthAck(subData)
                        willRecvAllLen = disInfo.get(BleIdentificationKey.C18_HistoryTotalByte)
                        if (willRecvAllLen == null || willRecvAllLen == 0) { //請求データが無いの場合
                            if (isFiristTask) {
                                taskQueue.first().sendCallBack?.invoke(Ble_Result.Success,null)
                            }
                        }else{
                            if (isFiristTask) {
                                sendCallback = taskQueue.first().sendCallBack //請求データを削除するまでに、コールバックを保存する
                            }
                        }
                    }

                    C18_HealthData_Key.SyncStep,C18_HealthData_Key.SyncBlood,C18_HealthData_Key.SyncSleep,
                        C18_HealthData_Key.SyncHeart,C18_HealthData_Key.SyncMultData -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        mRecvData =
                            mRecvData?.plus(subData)
                        }

                    C18_HealthData_Key.BlockOK -> {
                        if (mRecvData == null || curSyncHealthKeyType == null) {
                            sendCallback?.invoke(Ble_Result.Failed,null)
                        }else {
                            val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                            val disInfo = C18Hepler.dealBlockSureData(subData)
                            val tRecvAllLen = disInfo.get(BleIdentificationKey.C18_HistoryTotalByte)
                            val tRecvAllCrc = disInfo.get(BleIdentificationKey.C18_HistoryTotalCrc)
                            var tAckCode:UByte = 0u
                            val tMyCrc = C18Utils.checkSumToShort(mRecvData!!.toUByteArray()).toInt()
                            if (tRecvAllLen == willRecvAllLen) {
                                if (tRecvAllCrc == tMyCrc){
                                    C18Hepler.dealHistoryData(mRecvData!!,curSyncHealthKeyType!!)
                                }else{
                                    tAckCode = 2u
                                }
                            }else {
                                tAckCode = 3u
                            }
                            sendAckToDev(tCmd,C18_HealthData_Key.BlockOK.vl,tAckCode)
                            Timer().schedule(500){
                                delHealthDataByKey(curSyncHealthKeyType!!,null)
                            }
                        }
                    }

                    C18_HealthData_Key.DelStep,C18_HealthData_Key.DelSleep,C18_HealthData_Key.DelHeart,
                        C18_HealthData_Key.DelBlood,C18_HealthData_Key.DelMultData -> {
                            sendCallback?.invoke(Ble_Result.Success,null)
                        }
                }
            }

            /*
            * 0x06 リアルタイムデータ伝送
            */
            QOI_CMD_TYPE.CMD_RealData -> {
                val tKey = C18_RealData_Key.getKey(tKey)
                when (tKey) {
                    C18_RealData_Key.Step -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        C18Hepler.dealRealDataStep(subData)
                    }

                    C18_RealData_Key.ECG -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        C18Hepler.dealRealDataECG(subData)
                    }
                    C18_RealData_Key.Blood -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        C18Hepler.dealRealDataBlood(subData)
                    }
                    C18_RealData_Key.Spo2 -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        C18Hepler.dealRealDataSpo2(subData)
                    }
                    C18_RealData_Key.Heart -> {
                        val subData = buffData.array().copyOfRange(tOffset,tOffset + (tDataLen - 6))
                        C18Hepler.dealRealDataHeart(subData)
                    }
                }

            }
        }
        if (isFiristTask) {
            Log.d("recvDataUnpacket","CMD_TypeとKeyが一致のため、タスクキューから削除")
            popTask()
        }
    }

    override fun deviceInit(sendCallback: SendCallBack?) {
    }

    //全てのバイタルデータを同期
    override fun syncHealthData(sendCallback: SendCallBack?) {
        //CallBack地獄に入ってしまった、多種のバイタルデータを一種類づつで同期
        syncHealthDataStep { bleResult, info ->
            if (bleResult == Ble_Result.Failed){
                sendCallback?.invoke(bleResult,info)
            }else {
                syncHealthDataSleep { bleResult, info ->
                    if (bleResult == Ble_Result.Failed){
                        sendCallback?.invoke(bleResult,info)
                    }else{
                        syncHealthDataBlood { bleResult, info ->
                            if (bleResult == Ble_Result.Failed){
                                sendCallback?.invoke(bleResult,info)
                            }else{
                                syncHealthDataHeart { bleResult, info ->
                                    if (bleResult == Ble_Result.Failed){
                                        sendCallback?.invoke(bleResult,info)
                                    }else{
                                        syncHealthDataMult { bleResult, info ->
                                            sendCallback?.invoke(bleResult,info)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //デバイス時間を設定
    fun settingTime(sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getTimeSettingModel()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.Time.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //紛失防ぐ機能を設定
    fun settingAntiLost(open:Boolean,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getAntiLostSettingModel(open)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.Antilose.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //心拍監視機能を設定
    fun settingHeartMonitor(open: Boolean,interval:Int,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getHeartMonitorSettingModel(open,interval.toUByte())
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.HeartAutoMode.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //個人情報を設定
    fun settingUserInfo(userInfo: UserInfo,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getUserInfoSettingModel(userInfo.height.toUByte(),userInfo.weight.toUByte(),userInfo.gender.toUByte(),userInfo.age.toUByte())
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.UserInfo.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //画面の明るいさを設定
    fun settingDisplayBright(brightLevel:Int,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getDisplayBrightSettingModel(brightLevel.toUByte())
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.DisplayBright.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //腕を上げてスリープ解除する機能を設定
    fun settingRaiseScreen(open: Boolean,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getRaiseScreenSettingModel(open)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.RaiseScreen.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //心拍数アラートを設定
    fun settingHeartAlarm(open: Boolean,height:Int,low:Int,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getHeartAlarmSettingModel(open,height.toUByte(),low.toUByte())
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.HeartAlarm.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //出荷状態回復設定
    fun settingReSet(sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getReSettingModel()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.ReSet.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //言語設定
    fun settingLanguage(language: C18_OS_Language,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getLanguageSettingModel(language)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.Language.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //血圧範囲設定
    fun settingBloodRange(range: C18_Blood_Range,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getBloodRanageSettingModel(range)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.BloodRange.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //左右装着設定
    fun settingHandWear(handWear: C18_Hand_Wear,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getHandWearSettingModel(handWear)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.HandWear.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //テーマ設定
    fun settingMainTheme(themeType: C18_Theme_Type,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getThemeSettingModel(themeType)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.MainTheme.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //皮膚色設定
    fun settingSkinColor(skinColor: C18_Skin_Color,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getSkinColorSettingModel(skinColor)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.SkinColor.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //アラート設定 検索
    fun settingSelectAlarmTime(sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.selectAlarmTimeSetting()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.Alarm.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //アラート設定 追加
    fun settingAddAlarmTime(alarmTime: C18_AlarmTime,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.addAlarmTimeSetting(alarmTime)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.Alarm.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //アラート設定 削除
    fun settingDelAlarmTime(oldAlarmTime: C18_AlarmTime,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.delAlarmTimeSetting(oldAlarmTime)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.Alarm.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //アラート設定 変更
    fun settingChangeAlarmTime(oldAlarmTime: C18_AlarmTime,newAlarmTime: C18_AlarmTime,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.chanageAlarmTimeSetting(oldAlarmTime,newAlarmTime)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.Alarm.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //おやすみモード設定
    fun settingSleep(sleepInfo: C18_SleepInfo,sendCallback: SendCallBack?) {
        val optionData = C18SettingCMD.getSleepModeSettingModel(sleepInfo.isOpen,sleepInfo.start_timeHour.toUByte(),sleepInfo.start_timeMin.toUByte(),sleepInfo.end_timeHour.toUByte(),sleepInfo.end_timeMin.toUByte())
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Setting,C18_Setting_Key.SleepMode.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //デバイス基本情報の取得
    fun getDevInfo(sendCallback: SendCallBack?) {
        val optionData = C18GetCMD.getDeviceInfo()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Get,C18_Get_Key.DevInfo.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //デバイスサポート情報の取得
    fun getDevSupport(sendCallback: SendCallBack?) {
        val optionData = C18GetCMD.getSupportInfo()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Get,C18_Get_Key.Support.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //デバイス各機能のスイッチ状態の取得
    fun getSwitchStatus(sendCallback: SendCallBack?) {
        val optionData = C18GetCMD.getSwitchStatus()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Get,C18_Get_Key.SwitchStatus.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //ユーザ設定情報の取得
    fun getUserSetInfo(sendCallback: SendCallBack?) {
        val optionData = C18GetCMD.getUserSetInfo()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Get,C18_Get_Key.UserSetInfo.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //デバイスMacの取得
    fun getDeviceMac(sendCallback: SendCallBack?) {
        val optionData = C18GetCMD.getDeviceMac()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Get,C18_Get_Key.Mac.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //デバイスNameの取得
    fun getDeviceName(sendCallback: SendCallBack?) {
        val optionData = C18GetCMD.getDeviceName()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Get,C18_Get_Key.Name.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //デバイステーマの取得
    fun getDeviceTheme(sendCallback: SendCallBack?) {
        val optionData = C18GetCMD.getMainTheme()
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_Get,C18_Get_Key.MainTheme.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //デバイスを探す
    fun controlFindDev(sendCallback: SendCallBack?){
        val optionData = C18AppControl.controlFindDev(true)  //スイッチではないため、常にTrueにする
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.FindDev.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //心拍測定スイッチ制御
    fun controlHeartSwitch(open: Boolean,sendCallback: SendCallBack?) {
        val optionData = C18AppControl.controlHeart(open)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.HeartSwitch.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //血圧測定スイッチ制御
    fun controlBloodSwitch(open: Boolean,sendCallback: SendCallBack?) {
        val optionData = C18AppControl.controlBlood(open)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.BloodSwitch.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //PGG伝送スイッチ制御
    fun controlPGGWaveSwitch(open: Boolean,sendCallback: SendCallBack?) {
        val optionData = C18AppControl.controlWave(open,C18_Wave_Type.PGG)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.WaveSync.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //ECG伝送スイッチ制御
    fun controlECGWaveSwitch(open: Boolean,sendCallback: SendCallBack?) {
        val optionData = C18AppControl.controlWave(open,C18_Wave_Type.ECG)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.WaveSync.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //AmbientLight伝送スイッチ制御
    fun controlLightWaveSwitch(open: Boolean,sendCallback: SendCallBack?) {
        val optionData = C18AppControl.controlWave(open,C18_Wave_Type.AmbientLight)
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.WaveSync.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //リアルタイムの運動情報伝送スイッチ制御
    fun controlRealStepSwitch(open: Boolean,interval: Int,sendCallback: SendCallBack?){
        val optionData = C18AppControl.controlReal(open,C18_Real_Type.Step,interval.toUByte())
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.RealSync.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //リアルタイムの血圧情報伝送スイッチ制御
    fun controlRealBloodSwitch(open: Boolean,interval: Int = 2,sendCallback: SendCallBack?){
        val optionData = C18AppControl.controlReal(open,C18_Real_Type.Blood,interval.toUByte())
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_AppControl,C18_AppControl_Key.RealSync.vl,optionData)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    fun beginECGTest(open:Boolean,sendCallback: SendCallBack?){

        controlBloodSwitch(open){result,_ ->
            if (result == Ble_Result.Failed){
                sendCallback?.invoke(result,null)
                return@controlBloodSwitch
            }else{
                controlECGWaveSwitch(open){result,_->
                    if (result == Ble_Result.Failed){
                        sendCallback?.invoke(result,null)
                        return@controlECGWaveSwitch
                    }else{
                        controlRealBloodSwitch(open){result,_ ->
                            sendCallback?.invoke(result,null)
                            return@controlRealBloodSwitch
                        }
                    }
                }
            }
        }
//        controlBloodSwitch(true){success,_ ->
//            if (success == Ble_Result.Success){
//                controlECGWaveSwitch(true){
//                    success,_ ->
//                    if (success == Ble_Result.Success) {
//                        controlRealBloodSwitch(open){ success,_ ->
//                            sendCallback?.invoke(success,null)
//                        }
//                    }
//                }
//            }
//        }
    }



    //多バイタルデータを同期
    fun syncHealthDataMult(sendCallback: SendCallBack?){
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_HealthData,C18_HealthData_Key.MultData.vl)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //血圧バイタルデータを同期
    fun syncHealthDataBlood(sendCallback: SendCallBack?){
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_HealthData,C18_HealthData_Key.Blood.vl)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //睡眠バイタルデータを同期
    fun syncHealthDataSleep(sendCallback: SendCallBack?){
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_HealthData,C18_HealthData_Key.Sleep.vl)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //心拍バイタルデータを同期
    fun syncHealthDataHeart(sendCallback: SendCallBack?){
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_HealthData,C18_HealthData_Key.Heart.vl)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    //運動バイタルデータを同期
    fun syncHealthDataStep(sendCallback: SendCallBack?){
        val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_HealthData,C18_HealthData_Key.Step.vl)
        sendPacket.sendCallBack = sendCallback
        pushTask(sendPacket)
    }

    /*
    *デバイス内のバイタルデータを削除
    * key:C18_HealthData_Key バイタルデータのタイプ
    */
    fun delHealthDataByKey(key:C18_HealthData_Key,sendCallback: SendCallBack?){
        val optionData = C18HealthDataCMD.getDelHealthDataModel(C18HealthDataCMD.Del_Health_Type.All)
        var delKey:UByte? = null
        when (key) {
            C18_HealthData_Key.Step -> delKey = C18_HealthData_Key.DelStep.vl
            C18_HealthData_Key.Sleep -> delKey = C18_HealthData_Key.DelSleep.vl
            C18_HealthData_Key.Heart -> delKey = C18_HealthData_Key.DelHeart.vl
            C18_HealthData_Key.Blood -> delKey = C18_HealthData_Key.DelBlood.vl
            C18_HealthData_Key.MultData -> delKey = C18_HealthData_Key.DelMultData.vl
        }
        if (delKey != null) {
            val sendPacket = QOISendPacket(QOI_CMD_TYPE.CMD_HealthData,delKey,optionData)
            sendPacket.sendCallBack = sendCallback
            pushTask(sendPacket)
        }
    }

}
