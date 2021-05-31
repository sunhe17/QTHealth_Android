package qoi.myhealth.Ble.model

import qoi.myhealth.Ble.C18.CMD.C18_Skin_Color

class UserInfo(_height:Int = 170,_weight:Int = 65,_gender:Int = 0,_age:Int = 20,_skinColor: C18_Skin_Color = C18_Skin_Color.Yellow) {

    var height = _height
    var weight = _weight
    var gender = _gender
    var age    = _age
    var skinColor = _skinColor

}

data class LocalUserInfo(var name: String = "", var birth: String ="1997-04-04", var gender: Int = 1, var height: Int =150, var wight: Int = 50, var skincolor: Int =0, var blood: Int = 0 ){}

data class AppSettingInfo(var walkGoal: Int = 0, var sleepHour: Int = 0, var sleepMinite: Int = 0, var language: String = "JA"){}