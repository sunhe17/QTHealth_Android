package qoi.myhealth.Manager

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import qoi.myhealth.Ble.C18.Model.C18_UserSettingInfo
import qoi.myhealth.Ble.model.*

object ShareDataManager {
    private val TAG = this::class.java.simpleName
    private lateinit var prefs: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    private lateinit var gson:Gson
    private val FILE_NAME = "QOI_Health"

    private val C18_DEVICE_MAC_KEY = "DeviceMacKey"
    private val C18_USERSETTING_KEY = "UsersettingKey"
    private val DEVICE_TYPE_KEY     = "DeviceTypeKey"
    private val SCANUUID_KEY = "SaveUUIDKey"
    private val APPUSER_KEY = "AppUserInfoKey"
    private val APPSETTING_KEY = "AppSettingKey"

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

    // どのデバイスと接続が確認
    fun saveConnectType(var1:String?){
        edit.putString(DEVICE_TYPE_KEY,var1).commit()
    }

    // 接続しているデバイスを取得
    fun getConnectType():String?{
        return prefs.getString(DEVICE_TYPE_KEY,null)
    }

    // 読み取ったUUIDを保存
    fun saveScanData(var1: Device_ID_KEY){
        edit.putString(SCANUUID_KEY,gson.toJson(var1)).commit()
    }

    fun getScanData(): Device_ID_KEY {
        val tData = prefs.getString(SCANUUID_KEY,null)
        if (tData.isNullOrEmpty()){
            return Device_ID_KEY("","")
        }else{
            return gson.fromJson(tData!!,Device_ID_KEY::class.java)
        }
    }

    // ユーザーデータを保存
    fun saveAppUserInfo(var1: LocalUserInfo){
        edit.putString(APPUSER_KEY,gson.toJson(var1)).commit()
    }

    fun getAppUserInfo(): LocalUserInfo {
        val tData = prefs.getString(APPUSER_KEY,null)
        if (tData.isNullOrEmpty()){
            return LocalUserInfo()
        }else{
            return gson.fromJson(tData!!,LocalUserInfo::class.java)
        }
    }
}