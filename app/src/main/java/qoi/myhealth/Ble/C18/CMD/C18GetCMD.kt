package qoi.myhealth.Ble.C18.CMD


enum class C18_Get_Key(val vl: UByte) {
    DevInfo(0x00u),
    Support(0x01u),
    Mac(0x02u),
    Name(0x03u),
    SwitchStatus(0x04u),
    UserSetInfo(0x07u),
    MainTheme(0x09u),
    EcgLocation(0x0au);

    companion object {
        fun getKey(vl:UByte): C18_Get_Key? = C18_Get_Key.values().find { it.vl == vl }?: null
    }
}

object C18GetCMD {

    //デバイス基本情報の取得
    fun getDeviceInfo() : UByteArray{
        var data = ubyteArrayOf()
        data += 0x47u
        data += 0x43u
        return data
    }

    //デバイス各機能のスイッチ状態の取得
    fun getSwitchStatus() : UByteArray {
        var data = ubyteArrayOf()
        data += 0x47u
        data += 0x53u
        return data
    }

    //ユーザ設定情報の取得
    fun getUserSetInfo() : UByteArray {
        var data = ubyteArrayOf()
        data += 0x43u
        data += 0x46u
        return data
    }

    //デバイスMacの取得
    fun getDeviceMac() : UByteArray {
        var data = ubyteArrayOf()
        data += 0x47u
        data += 0x4Du
        return data
    }

    //デバイスNameの取得
    fun getDeviceName() : UByteArray {
        var data = ubyteArrayOf()
        data += 0x47u
        data += 0x50u
        return data
    }

    //デバイスサポート情報の取得
    fun getSupportInfo() : UByteArray {
        var data = ubyteArrayOf()
        data += 0x47u
        data += 0x46u
        return data
    }

    //テーマ情報の取得
    fun getMainTheme() : UByteArray {
        var data = ubyteArrayOf()
        return  data
    }

}