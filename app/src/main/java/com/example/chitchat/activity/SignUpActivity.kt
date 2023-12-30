package com.example.chitchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.chitchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var buttonSignup: Button
    private lateinit var uName: EditText
    private lateinit var uEmail: EditText
    private lateinit var uPassword: EditText
    private lateinit var uConfirmPassword: EditText
    private lateinit var buttonLogin: Button


    private  lateinit var auth: FirebaseAuth
    private  lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initViews()

        auth = FirebaseAuth.getInstance()
        buttonSignup.setOnClickListener{
            val userName = uName.text.toString()
            val userPassword = uPassword.text.toString()
            val userEmail = uEmail.text.toString()
            val userConfirmPassword = uConfirmPassword.text.toString()

            if (TextUtils.isEmpty(userName)){
                Toast.makeText(applicationContext, "username is required", Toast.LENGTH_SHORT).show()
            }

            if (TextUtils.isEmpty(userEmail)){
                Toast.makeText(applicationContext,  "email is required", Toast.LENGTH_SHORT).show()
            }

            if (TextUtils.isEmpty(userPassword)){
                Toast.makeText(applicationContext,  "password is required", Toast.LENGTH_SHORT).show()
            }

            if (TextUtils.isEmpty(userConfirmPassword)){
                Toast.makeText(applicationContext,  "confirm password is required", Toast.LENGTH_SHORT).show()
            }

            if (!userPassword.equals(userConfirmPassword)){
                Toast.makeText(applicationContext,  "password not matched", Toast.LENGTH_SHORT).show()
            }

            registerUser(userName,userEmail,userPassword)

        }

        buttonLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun initViews() {
        buttonSignup = findViewById(R.id.btnSignUp)
        uName = findViewById(R.id.etName)
        uEmail = findViewById(R.id.etEmail)
        uPassword = findViewById(R.id.etPassword)
        buttonLogin = findViewById(R.id.btnLogin)
        uConfirmPassword= findViewById(R.id.etConfirmPassword)
    }

    private fun registerUser(userName:String,email:String,password:String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful){
                    val user:FirebaseUser? = auth.currentUser
                    val userId:String = user!!.uid

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                    val hashMap:HashMap<String,String> = HashMap()
                    hashMap.put("userId",userId)
                    hashMap.put("userName",userName)
                    hashMap.put("profileImage","")

                    databaseReference.setValue(hashMap).addOnCompleteListener(this) {
                        if (it.isSuccessful){
                            uEmail.setText("")
                            uPassword.setText("")
                            uName.setText("")
                            uConfirmPassword.setText("")
                            val intent =Intent(this, UsersActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
    }

}