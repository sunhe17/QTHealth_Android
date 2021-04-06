package qoi.myhealth.Ble

import java.util.*

enum class ServiceUUID(val uuid: UUID) {
    C18_PULSE(UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb")),
    C18_SERVICE(UUID.fromString("be940000-7333-be46-b7ae-689e71722bd5")),
    C18_kWrite1(UUID.fromString("be940001-7333-be46-b7ae-689e71722bd5")),
    C18_kWrite2(UUID.fromString("be940002-7333-be46-b7ae-689e71722bd5")),
    C18_kRead(UUID.fromString("be940003-7333-be46-b7ae-689e71722bd5")),
    SPO2_PULSE(UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")),
    SPO2_SERVICE(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")),
    SPO2_kWrite1(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")),
    SPO2_kWrite2(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")),
    SPO2_kRead1(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")),
    SPO2_kRead2(UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb")),

    Public_Descriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))

}