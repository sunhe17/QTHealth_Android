package qoi.myhealth.API

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import qoi.myhealth.Ble.model.LocalUserInfo
import qoi.myhealth.BuildConfig
import qoi.myhealth.Manager.ShareDataManager
import java.io.IOException

class APIManager {

    val OK: String = "200"
    val ERROR_REQUEST = "400"
    val ERROR_TOKEN = "401"
    val ERROR_SERVER = "500"



    private val client: OkHttpClient = OkHttpClient()
    private val gsonBuilder = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(javaClass, DateTypeAdapter()).create()
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    companion object {

        // シングルトンインスタンスの宣言
        private var instance: APIManager = APIManager()
        // インスタンス取得
        fun getInstance() : APIManager {
            return instance
        }
        var authToken: String = ""
    }

    // 環境ごろとにURLを取得
    private fun setBaseURL(): String{
        // 本番環境
        if(!BuildConfig.DEVELOPMENT_MODE){
            return "https://api.quantum-op.com/"
        }
        return "https://apitest.quantum-op.com/"
    }

    // ポップアップテキスト表示
    private fun showToasCode(context: Context, code: String ,success: String){
        when(code){
            OK->{Toast.makeText(context, success, Toast.LENGTH_LONG).show()}
            ERROR_REQUEST->{Toast.makeText(context, "送信する内容が間違っています", Toast.LENGTH_LONG).show()}
            ERROR_SERVER->{Toast.makeText(context, "予期せぬエラーが発生しました", Toast.LENGTH_LONG).show()}
        }
    }

    // requestパラメータ作成
    private fun makeSendJson(requestData: Map<String,String>): String{
        var forCount: Int = 0
        var requestCount: Int = requestData.count()

        var data = "{"
        requestData.forEach{(title,value)->
            data += "\""+title+"\":\""+value+"\""
            if(forCount != (requestCount - 1)){
                data += ","
            }
            forCount ++
        }
        data += "}"

        return data
    }

    // 認証トークン取得
    fun getAuthToken(key:String,method:(String,String)->Unit){
        val url = setBaseURL() + "api/v1/token/authentication_token"

        val sendDataJson = "{\"access_key\":\""+key+"\"}"

        val head: Headers = Headers.Builder()
            .add("Content-Type","application/json")
            .build()

        val request = Request.Builder()
            .url(url)
            .headers(head)
            .post(sendDataJson.toRequestBody(JSON_MEDIA))
            .build()

        GlobalScope.launch(Dispatchers.Main) {
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    // Responseの読み出し
                    val responseBody = JSONObject(response.body!!.string())
                    if (responseBody["code"].toString() == OK) {
                        authToken = responseBody["authentication_token"].toString()
                        method(
                            responseBody["code"].toString(),
                            responseBody["authentication_token"].toString()
                        )
                    } else {
                        method(responseBody["code"].toString(), "")
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Error", e.toString())
                }
            })
        }
    }

    // ユーザーデータの送信
    fun setUserInfo(context: Context, birth:String, gender: String, height: String, weight: String,method:(String)->Unit){

        // アクセスキー
        val accessKey: String = ShareDataManager.getScanData().key

        // UUID
        val uuid: String = ShareDataManager.getScanData().uuid

        // ユーザーデータ
        val userInfo: LocalUserInfo = ShareDataManager.getAppUserInfo()

        val url = setBaseURL() + "api/v1/personal/personal_info"

        val head: Headers = Headers.Builder()
            .add("Content-Type","application/json")
            .add("Authorization",APIManager.authToken)
            .build()

        // requestデータ作成
        val requestMap: Map<String,String> = mapOf(
            "access_key" to accessKey,
            "identify_id" to uuid,
            "birthday" to birth,
            "gender" to gender,
            "height" to height,
            "weight" to weight
        )
        val sendDataJson: String = makeSendJson(requestMap)

        val request = Request.Builder()
            .url(url)
            .headers(head)
            .post(sendDataJson.toRequestBody(JSON_MEDIA))
            .build()

        GlobalScope.launch(Dispatchers.Main) {
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    // Responseの読み出し
                    val responseBody = JSONObject(response.body!!.string())
                    if(ERROR_TOKEN == responseBody["code"].toString()){
                        getAuthToken(ShareDataManager.getScanData().key) { code, key ->
                            if (code == OK) {
                                setUserInfo(context,birth,gender,height,weight) { it ->
                                    GlobalScope.launch(Dispatchers.Main) {showToasCode(context, responseBody["code"].toString(),"登録が完了しました") }
                                    method(it)
                                }
                            }
                        }
                    }
                    else{
                        GlobalScope.launch(Dispatchers.Main) {showToasCode(context, responseBody["code"].toString(),"登録が完了しました") }
                        method(responseBody["code"].toString())
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Error", e.toString())
                }
            })
        }
    }

    // 心電図送信
    fun setECGData(){

    }
}