package qoi.myhealth.Controller.Util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AppStatusCheck {

    companion object
    {

        /// 初回起動か確認
        //  ture:初回起動
        fun isFirstJudgment(context: Context) : Boolean {
            // 共有環境変数のインスタンス生成
            val dataStore : SharedPreferences = context.getSharedPreferences("DataStore",Context.MODE_PRIVATE)

            return dataStore.getBoolean("Is_First",true)
        }

        /// 初回起動フラグの設定変更
        fun setFirstFlg(context: Context,flg:Boolean){
            // 共有環境変数のインスタンス生成
            val edit : SharedPreferences.Editor = context.getSharedPreferences("DataStore",Context.MODE_PRIVATE).edit()
            edit.putBoolean("Is_First",flg)
            edit.apply()
        }

        // 位置情報権限
        fun checkPermission(context: Context,activity: Activity){
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
            }
        }

        // 曜日確認
        fun checkWeek(weekData:Array<Boolean>):String{
            val weekNameList = arrayOf("月曜日","火曜日","水曜日","木曜日","金曜日","土曜日","日曜日")
            when {
                // 平日か確認
                (!weekData[5]) and (!weekData[6]) and ( weekData[0] or weekData[1] or weekData[2] or weekData[4] )->{
                     return "平日"
                }
                // 休日か確認
                (weekData[5]) or (weekData[6]) and ( (!weekData[0]) and (!weekData[1]) and (!weekData[2]) and (!weekData[4]) ) ->{
                    return  "休日"
                }
                // 毎日か確認
                !weekData.toBooleanArray().contains(false)->{
                    return "毎日"
                }
                else->{
                    var data: String = ""
                    for (i in 0..6){
                        if(weekData[i]){
                            data += weekNameList[i]
                        }
                    }
                    return data
                }
            }

            return ""
        }

    }

}