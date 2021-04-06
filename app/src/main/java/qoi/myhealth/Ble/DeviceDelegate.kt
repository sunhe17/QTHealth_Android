package qoi.myhealth.Ble

import android.bluetooth.BluetoothGatt


typealias SendCallBack = (Ble_Result,Map<BleIdentificationKey,Any>?) -> Unit

enum class Ble_Result {
    Success,
    Failed
}

abstract class DeviceDelegate {

    protected var mGatt: BluetoothGatt? = null

    constructor(mGatt: BluetoothGatt){
        this.mGatt = mGatt
    }

    abstract fun recvDataUnpacket(recvData:ByteArray)
    abstract fun deviceInit(sendCallback: SendCallBack?)
    abstract fun syncHealthData(sendCallback: SendCallBack?)
}