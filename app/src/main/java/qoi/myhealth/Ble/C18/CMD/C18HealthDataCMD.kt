package qoi.myhealth.Ble.C18.CMD


enum class C18_HealthData_Key(val vl: UByte) {
    Step(0x02u),
    Sleep(0x04u),
    Heart(0x06u),
    Blood(0x08u),
    MultData(0x09u),

    SyncStep(0x11u),
    SyncSleep(0x13u),
    SyncHeart(0x15u),
    SyncBlood(0x17u),
    SyncMultData(0x18u),

    DelStep(0x40u),
    DelSleep(0x41u),
    DelHeart(0x42u),
    DelBlood(0x43u),
    DelMultData(0x44u),

    BlockOK(0x80u);

    companion object {
        fun getKey(vl:UByte): C18_HealthData_Key? = C18_HealthData_Key.values().find { it.vl == vl }?: null
    }

}

object C18HealthDataCMD {

    enum class Del_Health_Type(val v1:UByte){
        OneDay(0x00u),   //当日分
        Past(0x01u),     //過去
        All(0x02u);      //全部
    }

    fun getDelHealthDataModel(delType:Del_Health_Type):UByteArray{
        var data = ubyteArrayOf(delType.v1)
        return data
    }
}