package qoi.myhealth.Ble

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*


object Extension {

    fun UShort.toUBytes(order: ByteOrder = ByteOrder.LITTLE_ENDIAN) : UByteArray{

        if (order == ByteOrder.BIG_ENDIAN){
            return ubyteArrayOf(((this.toInt() and 0xFF00) shr (8)).toUByte(),(this.toInt() and 0x00FF).toUByte())
        }
        return ubyteArrayOf((this.toInt() and 0x00FF).toUByte(), ((this.toInt() and 0xFF00) shr (8)).toUByte())
    }

    fun ByteArray.toHexString(separator: CharSequence = " ",  prefix: CharSequence = "[",  postfix: CharSequence = "]") =
        this.joinToString(separator, prefix, postfix) {
            String.format("0x%02X", it)
        }

    fun Date.toTimeStampString(format:String = "yyyy-MM-dd HH:mm:ss") : String{
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(this)
    }

    fun ByteBuffer.get3Byte(offset:Int) : Int{
        var tOffInc = 1
        var tOffset = offset
        if (this.order() == ByteOrder.BIG_ENDIAN){
            tOffInc = -1
            tOffset += 2
        }
        var rtn:Int = this.get(tOffset).toInt()
        println("tOffInc = $tOffInc")
        println("tOffset = $tOffset")
        rtn += this.get(tOffset + (1 * tOffInc)).toInt() shl 8
        println(rtn)
        rtn += this.get(tOffset + (2 * tOffInc)).toInt() shl 16
        println(rtn)
        return rtn
    }

    fun UByte.toBool() : Boolean {
        return when (this.toInt()) {
            1 -> true
            else -> false
        }
    }

    fun UByte.toBoolByBit(bit:Int) : Boolean {
        if (bit < 0 || bit > 7){
            return false
        }
        return when ((this.toInt() shr bit) and 1) {
            1 -> true
            else -> false
        }
    }
}
