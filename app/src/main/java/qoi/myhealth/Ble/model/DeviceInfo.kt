package qoi.myhealth.Ble.model


object DeviceInfo {
    var uuid: String = ""
    var accessKey: String =""
}

data class Device_ID_KEY(var uuid: String, var key: String, var auth: Boolean = false, var language: Int = 0, var walkGoal:Int = 100, var sleepGoal: Double = 7.5){

}