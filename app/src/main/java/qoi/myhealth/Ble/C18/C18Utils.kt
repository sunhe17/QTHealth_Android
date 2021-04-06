package qoi.myhealth.Ble.C18

import qoi.myhealth.Ble.Extension.toUBytes
import java.nio.ByteOrder
import java.util.*

object C18Utils {

    fun checkSumToArray(data:UByteArray) : UByteArray{
        var retCrc:UShort = 0xFFFFu
        for (i in data){
            retCrc = ((retCrc.toUInt() shr 8).toUByte()).toUShort() or (retCrc.toUInt() shl 8).toUShort()
            retCrc = retCrc xor i.toUShort()
            retCrc = retCrc xor ((retCrc and 0xFFu).toUInt() shr 4).toUShort()
            retCrc = retCrc xor ((retCrc.toUInt() shl 8) shl 4).toUShort()
            retCrc = retCrc xor (((retCrc and 0xFFu).toUInt() shl 4) shl 1).toUShort()
        }
        return retCrc.toUBytes()
    }

    fun checkSumToShort(data:UByteArray) : UShort{
        var retCrc:UShort = 0xFFFFu
        for (i in data){
            retCrc = ((retCrc.toUInt() shr 8).toUByte()).toUShort() or (retCrc.toUInt() shl 8).toUShort()
            retCrc = retCrc xor i.toUShort()
            retCrc = retCrc xor ((retCrc and 0xFFu).toUInt() shr 4).toUShort()
            retCrc = retCrc xor ((retCrc.toUInt() shl 8) shl 4).toUShort()
            retCrc = retCrc xor (((retCrc and 0xFFu).toUInt() shl 4) shl 1).toUShort()
        }
        return retCrc
    }

    fun getSecondsFromGMT():Int {
        return TimeZone.getDefault().rawOffset / 1000
    }

}