package qoi.myhealth.Ble.C18.Model

class C18_LongSite(_start_timeHour_1 : UByte = 0u,_start_timeMin_1:UByte = 0u,_end_timeHour_1:UByte = 0u,_end_timeMin_1:UByte = 0u,
                   _start_timeHour_2 : UByte = 0u,_start_timeMin_2:UByte = 0u,_end_timeHour_2:UByte = 0u,_end_timeMin_2:UByte = 0u) {

    var start_timeHour_1 : UByte = _start_timeHour_1    // 0-23
    var start_timeMin_1 : UByte = _start_timeMin_1      // 0-59
    var end_timeHour_1 : UByte = _end_timeHour_1        // 0-23
    var end_timeMin_1 : UByte = _end_timeMin_1          // 0-59
    var start_timeHour_2 : UByte = _start_timeHour_2    // 0-23
    var start_timeMin_2 : UByte = _start_timeMin_2      // 0-59
    var end_timeHour_2 : UByte = _end_timeHour_2        // 0-23
    var end_timeMin_2 : UByte = _end_timeMin_2          // 0-59
    var interval : UByte = 15u                          //15-45
    private var repeater : UByte = 0u
    private var isOpen : Boolean = false
    private var week : Array<Boolean>? = null           //[月、火、水、木、金、土、日]

    init {
        this.week = arrayOf(false,false,false,false,false,false,false)
        this.dealWeekIsOpen()
    }

    fun getRepeater() : UByte {
        return this.repeater
    }

    fun getIsOpen() : Boolean {
        return this.isOpen
    }

    fun getWeek() : Array<Boolean>{
        return this.week!!
    }

    fun setRepeater(repeater:UByte){
        this.repeater = repeater
        this.dealRepeater()
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
