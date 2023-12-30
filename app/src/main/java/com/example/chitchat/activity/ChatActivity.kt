package com.example.chitchat.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.RetrofitInstance
import com.example.chitchat.adapter.ChatAdapter
import com.example.chitchat.model.Chat
import com.example.chitchat.model.NotificationData
import com.example.chitchat.model.PushNotification
import com.example.chitchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    private lateinit var chatRecyclerView: RecyclerView
    var chatList = ArrayList<Chat>()
    private lateinit var imgBack: ImageView
    private lateinit var imgProfile: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var etMessage: TextView
    private lateinit var btnSendMsg: ImageButton
    private var topic =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initViews()
        chatRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )

        imgBack.setOnClickListener {
            onBackPressed()
        }
       var intent = getIntent()
        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")


        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                tvUserName.text = user!!.userName
                if (user.profileImage == "") {
                    imgProfile.setImageResource(R.drawable.man)
                } else {
                    Glide.with(this@ChatActivity).load(user.profileImage).into(imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        btnSendMsg.setOnClickListener {
            var message: String = etMessage.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "message is empty", Toast.LENGTH_SHORT).show()
                etMessage.setText("")

            } else {
                sendMessage(firebaseUser!!.uid, userId, message)
                etMessage.setText("")
                topic = "/topics/$userId"
                PushNotification(NotificationData(userName!!,message),topic)
                    .also {
                        sendNotification(it)
                    }
            }
        }
        readMessage(firebaseUser!!.uid, userId)

    }

    private fun initViews() {
        imgBack = findViewById(R.id.imgBack)
        imgProfile = findViewById(R.id.imgProfile)
        tvUserName = findViewById(R.id.tvUserName)
        btnSendMsg = findViewById(R.id.btnSendMsg)
        etMessage = findViewById(R.id.etMessage)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()
        var hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)

        reference!!.child("Chat").push().setValue(hashMap)


    }

    fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chat")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (dataSnapShot:DataSnapshot in snapshot.children){
                    val chat = dataSnapShot.getValue(Chat::class.java)
                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)){
                        chatList.add(chat)

                    }
                }
                val chatAdapter = ChatAdapter(this@ChatActivity,chatList)
                chatRecyclerView.adapter = chatAdapter
            }


        })}

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            }
            else{
                Log.e("TAG", response.errorBody()!!.string())
            }
        }
        catch (e:Exception){
            Log.e("TAG", e.toString())
        }
    }

}