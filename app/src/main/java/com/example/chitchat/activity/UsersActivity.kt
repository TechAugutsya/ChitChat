package com.example.chitchat.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.adapter.UserAdapter
import com.example.chitchat.firebase.FirebaseService
import com.example.chitchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class UsersActivity : AppCompatActivity() {
    private lateinit var userRecyclerView: RecyclerView
    var userList = ArrayList<User>()
    private lateinit var imgBack:ImageView
    private lateinit var imgProfile:ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        initViews()

        FirebaseService.sharedPref = getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            FirebaseService.token = it
        }

        userRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

        imgBack.setOnClickListener{
            onBackPressed()

        }
        imgProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        getUsersList()



    }
    fun getUsersList(){

        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topic/$userid")

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUser = snapshot.getValue(User::class.java)
                if (currentUser!!.profileImage == ""){
                    imgProfile.setImageResource(R.drawable.man)
                }
                else{
                    Glide.with(this@UsersActivity).load(currentUser.profileImage).into(imgProfile)
                }

                for (dataSnapShot:DataSnapshot in snapshot.children){
                    val user = dataSnapShot.getValue(User::class.java)
                    if (!user!!.userId.equals(firebase.uid)){

                      userList.add(user)

                    }
                }
                val userAdapter = UserAdapter(this@UsersActivity,userList)
                userRecyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,error.message,Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun initViews() {
        imgBack = findViewById(R.id.imgBack)
        imgProfile = findViewById(R.id.imgProfile)
        userRecyclerView = findViewById(R.id.userRecyclerView)
    }
}