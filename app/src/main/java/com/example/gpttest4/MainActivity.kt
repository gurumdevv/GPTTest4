package com.example.gpttest4

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gpttest4.API_KEY.MY_API_KEY
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var textInputLayout: TextInputLayout
    lateinit var queryEdt: TextInputEditText
    lateinit var messageRV: RecyclerView
    lateinit var messageRVAdapter: MessageRVAdapter
    lateinit var messageList: ArrayList<MessageRVModal>
    lateinit var question: String
    private val client = OkHttpClient() //OKHttp 객체 생성

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        queryEdt = findViewById(R.id.idEdtQuery)
        messageRV = findViewById(R.id.idRVMessages)
        messageList = ArrayList()
        messageRVAdapter = MessageRVAdapter(messageList)
        val layoutManager = LinearLayoutManager(applicationContext)
        messageRV.layoutManager = layoutManager
        messageRV.adapter = messageRVAdapter
        textInputLayout = findViewById(R.id.idTILQuery)

        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener{ textView: TextView, i: Int, keyEvent: KeyEvent? -> //엔터가 입력시 동작
            if(i == EditorInfo.IME_ACTION_SEND){
                processData()
                return@OnEditorActionListener true
            }
            false
        })

        textInputLayout.setEndIconOnClickListener { //전송 버튼을 누를 경우 동작
            processData()
        }
    }

    private fun processData() {
        //textbox에 있는 string문을 검사하고 조건에 부합하는 경우 messageList(Arraylist<MessageRVModal>)에 추가함 -> RecyclerView.Adapter에 통지되어 View가 갱신됨
        if(queryEdt.text.toString().isNotEmpty()) {
            question = queryEdt.text.toString()
            messageList.add(MessageRVModal(question, "user"))
            messageRVAdapter.notifyDataSetChanged()

            getResponse(queryEdt.text.toString()) { response ->
                //getResponse 메서드를 호출하면 response가 string문으로 반환됨 -> 반환된 string 문을 messageList에 추가 -> RecyclerView.Adapter에 통지되어 View가 갱신됨
                runOnUiThread {
                    messageList.add(MessageRVModal(response, "bot"))
                    Log.v("data", response)
                    messageRVAdapter.notifyDataSetChanged()
                }
            }

        } else {
            Toast.makeText(this, "문장을 입력하세요", Toast.LENGTH_SHORT).show() //입력된 문장이 없는 경우 Toast 출력
        }
    }

    private fun getResponse(qustion: String, callback: (String) -> Unit) {
        queryEdt.setText("")

        val url = "https://api.openai.com//v1/chat/completions"

        val requestBody = """
            {
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "user", "content": "${qustion} 반말로 대답해줘"}]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .header( "Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $MY_API_KEY")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback { // 요청을 비동기 처리
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API FALILED", e)
                Toast.makeText(this@MainActivity, "현재 네트워크가 불안정합니다. 잠시 후 시도해주세요.",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body?.string()

                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data", "empty")
                    Toast.makeText(this@MainActivity, "현재 네트워크가 불안정합니다. 잠시 후 시도해주세요.",
                        Toast.LENGTH_SHORT).show()
                }


                val jsonObject = JSONObject(body)
                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getJSONObject("message").getString("content")
                callback(textResult)

            }
        })
    }
}