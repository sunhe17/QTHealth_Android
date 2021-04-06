package qoi.myhealth.Ble.model

import android.bluetooth.BluetoothDevice

class BleDevice constructor(_device:BluetoothDevice,_device_rssi:Int = 0){
    var device:BluetoothDevice
    var device_rssi:Int

    init {
        device = _device
        device_rssi = _device_rssi
    }

}