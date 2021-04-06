package qoi.myhealth.Ble.C18.CMD


enum class C18_AppControl_Key(val vl:UByte) {

    FindDev(0x00u),
    HeartSwitch(0x01u),
    BloodSwitch(0x02u),
    WaveSync(0x0bu),
    RealSync(0x09u);

    companion object {
        fun getKey(vl:UByte): C18_AppControl_Key? = C18_AppControl_Key.values().find { it.vl == vl }?: null
    }
}

enum class C18_Wave_Type(val v1:UByte) {
    PGG(0x00u),
    ECG(0x01u),
    AmbientLight(0x02u)
}

enum class C18_Real_Type(val v1:UByte) {
    Step(0x00u),
    Blood(0x03u)
}


object C18AppControl {

    //波形伝送制御
    fun controlWave(open:Boolean,waveType: C18_Wave_Type) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 1 else 0
        data += openByte.toUByte()
        data += waveType.v1
        return data
    }

    //心拍測定制御
    fun controlHeart(open: Boolean) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 2 else 0   //ON:0x02 off:0x00
        data += openByte.toUByte()
        return data
    }

    //血圧測定制御
    fun controlBlood(open: Boolean) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 2 else 0   //ON:0x02 off:0x00
        data += openByte.toUByte()
        return data
    }

    //リアルタイムデータ伝送制御
    fun controlReal(open: Boolean,realType: C18_Real_Type,interval:UByte) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 1 else 0
        data += openByte.toUByte()
        data += realType.v1
        data += interval
        return data
    }

    //バンドを探す
    fun controlFindDev(open: Boolean) : UByteArray {
        var data = ubyteArrayOf()
        val openByte = if(open) 1 else 0
        data += openByte.toUByte()
        data += 0x05u       //アラート回数（変更可能、一応固定値で設定しておく）
        data += 0x02u       //アラート間隔（変更可能、一応固定値で設定しておく）
        return data
    }

}