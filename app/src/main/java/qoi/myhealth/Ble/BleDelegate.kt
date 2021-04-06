package qoi.myhealth.Ble

interface BleDelegate {
    fun onBleDeviceConnection()
    fun onBleDeviceDisConnection()
}