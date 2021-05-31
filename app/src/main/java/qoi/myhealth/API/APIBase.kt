package qoi.myhealth.API

import android.util.Log
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
import qoi.myhealth.BuildConfig
import qoi.myhealth.Controller.Util.Progress
import qoi.myhealth.Manager.ShareDataManager
import java.io.IOException

class APIBase {

    val CODE_STATUS: Int = 0
    val OK: String = "200"

    private val client: OkHttpClient = OkHttpClient()
    private val gsonBuilder = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(javaClass, DateTypeAdapter()).create()
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    companion object {

        // シングルトンインスタンスの宣言
        private var instance: APIBase = APIBase()
        // インスタンス取得
        fun getInstance() : APIBase {
            return instance
        }
    }

    private fun setBaseURL(): String{
        // 本番環境
        if(!BuildConfig.DEVELOPMENT_MODE){
            return "https://api.quantum-op.com/"
        }
        return "https://apitest.quantum-op.com/"
    }

    fun getAuthToken(key:String,method:(String,String)->Unit){
        val url = setBaseURL() + "api/v1/token/authentication_token"

        val sendDataJson = "{\"access_key\":\""+key+"\"}"
        println("Json=>"+sendDataJson)

        val head: Headers = Headers.Builder()
            .add("Content-Type","application/json")
            .build()

        val request = Request.Builder()
            .url(url)
            .headers(head)
            .post(sendDataJson.toRequestBody(JSON_MEDIA))
            .build()

       client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // Responseの読み出し
                val responseBody =  JSONObject(response.body!!.string())
                if(responseBody["code"].toString() == OK ){
                    method(responseBody["code"].toString(),responseBody["authentication_token"].toString())
                }
                else{
                    method(responseBody["code"].toString(),"")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }
        })
    }


}