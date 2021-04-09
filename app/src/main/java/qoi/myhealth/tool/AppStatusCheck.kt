package qoi.myhealth.tool

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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

    }

}