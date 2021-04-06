package qoi.myhealth.Ble.C18

import qoi.myhealth.Ble.Extension.toUBytes
import qoi.myhealth.Ble.SendCallBack
import java.nio.ByteOrder


enum class QOI_CMD_TYPE(val vl: UByte) {
    CMD_OTA(0x00u),
    CMD_Setting(0x01u),
    CMD_Get(0x02u),
    CMD_AppControl(0x03u),
    CMD_DevControl(0x04u),
    CMD_HealthData(0x05u),
    CMD_RealData(0x06u),
    CMD_Collect(0x07u),
    CMD_End(0x08u);

    companion object {
        fun getType(vl:UByte): QOI_CMD_TYPE? = values().find { it.vl == vl }?: null
    }
}

class QOISendPacket constructor(cmd_type:QOI_CMD_TYPE,key_Type:UByte,optionData:UByteArray = ubyteArrayOf()) {

        var cmd_type:QOI_CMD_TYPE = cmd_type
        var key_Type = key_Type
        var optionData = optionData
        var sendCallBack:SendCallBack? = null

        fun willSend():ByteArray{
            var tRetData = ubyteArrayOf(cmd_type.vl,key_Type)
            val tLen = (optionData.count() + 6).toUShort()
            tRetData += tLen.toUBytes()
            tRetData += (optionData)
            tRetData += C18Utils.checkSumToArray(tRetData)
            return tRetData.toByteArray()
        }

}