package qoi.myhealth.Ble.Spo2

import qoi.myhealth.Ble.Extension.toUBytes
import java.util.*

object Spo2Utils {

    fun UByteArray.checkSpo2Sum() : UByteArray {
        var tData = this
        val sum = this.sum() and 0xffu
        tData += sum.toUByte()
        return tData
    }

    fun Array<UByteArray>.removedDuplicate(num:Int) : Array<UByteArray> {
        var removed = 0
        var results = mutableListOf<UByteArray>()

        for (item in this) {
            if (removed < num && results.contains(item)){
                removed += 1
            }else {
                results.add(item)
            }
        }
        return results.toTypedArray()
    }

    fun Array<UByteArray>.records() : Array<UByteArray> {
        var results = mutableListOf<UByteArray>()
        for (item in this){
            if (item.size < 16) {return arrayOf()}
            results.add(item.copyOfRange(1,5))
            results.add(item.copyOfRange(5,9))
            results.add(item.copyOfRange(9,13))
            results.add(item.copyOfRange(13,17))
        }
        return results.toTypedArray()
    }
}