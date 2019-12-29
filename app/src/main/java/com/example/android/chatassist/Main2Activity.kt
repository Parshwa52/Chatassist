package com.example.android.chatassist

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.speech.tts.TextToSpeech
import com.github.bassaer.chatmessageview.model.ChatUser
import com.github.bassaer.chatmessageview.model.Message
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.android.extension.responseJson
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.*

class Main2Activity : AppCompatActivity()// latest protocol
// random Id
// English language
{

    lateinit var mTTS:TextToSpeech
    companion object {
        private const val ACCESS_TOKEN = "4240b496ea8947a9904596224dcb09eb"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main2)
        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                mTTS.language = Locale.UK
            }
        })

        FuelManager.instance.baseHeaders = mapOf(
            "Authorization" to "Bearer $ACCESS_TOKEN"
    )

    FuelManager.instance.basePath = "https://api.dialogflow.com/v1/"

    FuelManager.instance.baseParams = listOf(
            "v" to "20170712",                  // latest protocol
            "sessionId" to UUID.randomUUID(),   // random Id
            "lang" to "en"                      // English language
    )

    val human = ChatUser(
            1,
            "You",
            BitmapFactory.decodeResource(resources,
                    R.drawable.ic_account_circle)
    )

    val agent = ChatUser(
            2,
            "Bot",
            BitmapFactory.decodeResource(resources,
                    R.drawable.ic_account_circle)
    )

    my_chat_view.setOnClickSendButtonListener(View.OnClickListener {
        my_chat_view.send(Message.Builder()
                .setUser(human)
                .setText(my_chat_view.inputText)
                .build()
        )
        Fuel.get("/query",
                listOf("query" to my_chat_view.inputText))
                .responseJson { _, _, result ->
                    val reply = result.get().obj()
                            .getJSONObject("result")
                            .getJSONObject("fulfillment")
                            .getString("speech")

                    my_chat_view.send(Message.Builder()
                            .setRight(true)
                            .setUser(agent)
                            .setText(reply)
                            .build()
                    )
                    val toSpeak = reply.toString()
                    mTTS.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)
                    val intent:String? = result.get().obj()
                            .getJSONObject("result")
                            .optJSONObject("metadata")
                            .optString("intentName")

                    if(intent!! == "WEIGHT") {
                        val unitWeightName = result.get().obj()
                                .getJSONObject("result")
                                .getJSONObject("parameters")
                                .getString("unit-weight-name")

                        val unitWeight = result.get().obj()
                                .getJSONObject("result")
                                .getJSONObject("parameters")
                                .getJSONObject("unit-weight")
                                .getDouble("amount")

                        val result = if(unitWeightName == "lb") {
                            unitWeight * 2.20462
                        } else {
                            unitWeight / 2.20462
                        }

                        my_chat_view.send(Message.Builder()
                                .setRight(true)
                                .setUser(agent)
                                .setText("That's ${"%.2f".format(result)} $unitWeightName")
                                .build()
                        )
                    }
                }

    })
    }
}