package com.example.gpttest4

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var queryEdt: TextInputEditText
    lateinit var messageRV: RecyclerView
    lateinit var messageRVAdapter: MessageRVAdapter
    lateinit var messageList: ArrayList<MessageRVModal>
    val url = "https://api.openai.com/v1/completions"

    val OPENAI_API_KEY = ""


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

        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener{ textView: TextView, i, keyEvent: KeyEvent? ->
            if(i == EditorInfo.IME_ACTION_SEND){
                if(queryEdt.text.toString().isNotEmpty()) {
                    messageList.add(MessageRVModal(queryEdt.text.toString(), "user"))
                    messageRVAdapter.notifyDataSetChanged()
                    getResponse(queryEdt.text.toString())
                } else {
                    Toast.makeText(this, "Please enter your query", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false

        })
    }

    private fun getResponse(query: String) {
        queryEdt.setText("")
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject = JSONObject()
        jsonObject.put("model", "text-davinci-003")
        jsonObject.put("prompt", query)
        jsonObject.put("temperature", 0)
        jsonObject.put("max_tokens", 100)

        val postRequest: JsonObjectRequest =
            @SuppressLint("NotifyDataSetChanged")
            object: JsonObjectRequest(Method.POST, url, jsonObject, Response.Listener { response ->
                val responseMsg: String = response.getJSONArray("choices").getJSONObject(0).getString("text")
                messageList.add(MessageRVModal(responseMsg, "bot"))
                println(responseMsg)
                messageRVAdapter.notifyDataSetChanged()

            }, Response.ErrorListener {
                Toast.makeText(applicationContext, "Fail to get response..", Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer $OPENAI_API_KEY"
                    return params
                }
            }

        postRequest.retryPolicy = object: RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            override fun retry(error: VolleyError?) {

            }
        }
        queue.add(postRequest)
    }
}