package qoi.myhealth.Ble.model

import qoi.myhealth.Ble.C18.CMD.C18_Skin_Color

class UserInfo(_height:Int = 170,_weight:Int = 65,_gender:Int = 0,_age:Int = 20,_skinColor: C18_Skin_Color = C18_Skin_Color.Yellow) {

    var height = _height
    var weight = _weight
    var gender = _gender
    var age    = _age
    var skinColor = _skinColor

}