package qoi.myhealth.Manager

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import qoi.myhealth.Ble.C18.Model.C18_UserSettingInfo

object ShareDataManager {
    private val TAG = this::class.java.simpleName
    private lateinit var prefs: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    private lateinit var gson:Gson
    private val FILE_NAME = "QOI_Health"

    private val C18_DEVICE_MAC_KEY = "DeviceMacKey"
    private val C18_USERSETTING_KEY = "UsersettingKey"

    fun setUp(context: Context){
        Log.d(TAG,"setUp")
        prefs = context.getSharedPreferences(FILE_NAME,Activity.MODE_PRIVATE)
        edit = prefs.edit()
        gson = Gson()
    }


    fun saveConnectedDeviceMac(var1:String?){
        edit.putString(C18_DEVICE_MAC_KEY,var1).commit()
    }

    fun getConnectionDeviceMac():String?{
        return prefs.getString(C18_DEVICE_MAC_KEY,null)
    }

    fun saveUserSetInfo(var1: C18_UserSettingInfo) {
        edit.putString(C18_USERSETTING_KEY, gson.toJson(var1)).commit()
    }

    fun getUserSetInfo() : C18_UserSettingInfo? {
        val tData = prefs.getString(C18_USERSETTING_KEY,null)
        if (tData.isNullOrEmpty()){
            return null
        }else{
            return gson.fromJson(tData!!,C18_UserSettingInfo.javaClass)
        }
    }

}