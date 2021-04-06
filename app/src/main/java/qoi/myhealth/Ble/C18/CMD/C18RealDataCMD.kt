package qoi.myhealth.Ble.C18.CMD

enum class C18_RealData_Key(val vl:UByte) {
    Step(0x00u),
    Heart(0x01u),
    Spo2(0x02u),
    Blood(0x03u),
    PGG(0x04u),
    ECG(0x05u),
    Respiratory(0x07u);

    companion object {
        fun getKey(vl:UByte): C18_RealData_Key? = C18_RealData_Key.values().find { it.vl == vl }?: null
    }
}