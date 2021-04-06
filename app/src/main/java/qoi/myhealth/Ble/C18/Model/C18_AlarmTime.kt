package qoi.myhealth.Ble.C18.Model

import qoi.myhealth.Ble.C18.CMD.C18_HealthData_Key

enum class C18_AlarmTime_Type(val vl:UByte) {
    WakeUp(0x00u),
    Sleep(0x01u),
    WorkOut(0x02u),
    TakeMedicine(0x03u),
    Date(0x04u),
    Party(0x05u),
    Meeting(0x06u),
    Other(0x07u);

    companion object {
        fun getKey(vl:UByte): C18_AlarmTime_Type = C18_AlarmTime_Type.values().find { it.vl == vl }?: WakeUp
    }
}

enum class C18_AlarmTime_Action(val vl:UByte) {
    Select(0x00u),
    Add(0x01u),
    Del(0x02u),
    Change(0x03u);

    companion object {
        fun getKey(vl:UByte): C18_AlarmTime_Action? = C18_AlarmTime_Action.values().find { it.vl == vl }?: null
    }
}

class C18_AlarmTime(){

    var alarmtimeType:UByte = 0x00u     //0x00~0x07
    var hour:UByte = 0x00u              // 0-23
    var minute:UByte = 0x00u            // 0-59
    var repeater:UByte = 0X00U
    var isOpen:Boolean = false
    var week:Array<Boolean>? = null     //[月、火、水、木、金、土、日]
    var snoozeInterval:UByte = 0x00u


    constructor(type: C18_AlarmTime_Type,hour:UByte,minute:UByte,repeater:UByte,snoozeInterval:UByte = 0x00u) : this() {
        this.alarmtimeType = type.vl
        this.hour = hour
        this.minute = minute
        this.repeater = repeater
        this.snoozeInterval = snoozeInterval
        this.dealRepeater()
    }

    constructor(type: C18_AlarmTime_Type,hour:UByte,minute:UByte,isOpen:Boolean,week:Array<Boolean>,snoozeInterval:UByte = 0x00u) : this() {
        this.alarmtimeType = type.vl
        this.hour = hour
        this.minute = minute
        this.isOpen = isOpen
        this.week = week
        this.snoozeInterval = snoozeInterval
        this.dealWeekIsOpen()
    }

    private fun dealRepeater(){
        this.week = arrayOf(false,false,false,false,false,false,false)
        repeat(this.week!!.size) {
            week!![it] = ((this.repeater.toInt() shr it) and 1) != 0
        }
        this.isOpen = ((this.repeater.toInt() shr 7) and 1) != 0
    }

    private fun dealWeekIsOpen(){
        var tValue:UByte = 0x00u
        repeat(this.week!!.size){
            if (this.week!![it]){
                tValue = tValue or (1 shl it).toUByte()
            }
        }
        if (this.isOpen) {
            tValue = tValue or (1 shl 7).toUByte()
        }
        this.repeater = tValue
    }

}
