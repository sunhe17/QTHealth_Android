package qoi.myhealth.Controller.Util

import android.app.ProgressDialog
import android.content.Context

class Progress {

    companion object {

        // シングルトンインスタンスの宣言
        private var instance: Progress = Progress()
        // インスタンス取得
        fun getInstance() : Progress {
            return instance
        }
    }

    var progressDialog: ProgressDialog? = null

    fun showDialog(context: Context,text: String =""){
        progressDialog =  ProgressDialog(context)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setMessage(text)
        progressDialog!!.setCancelable(true)
        progressDialog!!.show()

        progressDialog!!.setCancelable(false)
    }

    fun closeDialog(){
        progressDialog!!.setCancelable(true)
        progressDialog!!.dismiss()
    }

}