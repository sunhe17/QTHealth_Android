package qoi.myhealth

import org.junit.Test

import org.junit.Assert.*
import qoi.myhealth.Ble.Spo2.Spo2Utils
import qoi.myhealth.Ble.Spo2.Spo2Utils.checkSpo2Sum


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun byteBuffTest() {
        val i = ubyteArrayOf(0xf1u)
        println(i[0])
        val data = Spo2Utils.getCurrDateBytes()

        val newData = data.checkSpo2Sum()
        println(newData[0])
    }
}