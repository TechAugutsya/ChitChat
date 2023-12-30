package com.example.chitchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.chitchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var buttonSignup: TextView
    private lateinit var buttonLogin: Button
    private lateinit var uEmail: EditText
    private lateinit var uPassword: EditText
    private  lateinit var auth: FirebaseAuth
    private   var firebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()

        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser

        if (firebaseUser !=null){
            val intent = Intent(this, UsersActivity::class.java)
            startActivity(intent)
            finish()
        }


        buttonLogin.setOnClickListener{
            val userPassword = uPassword.text.toString()
            val userEmail = uEmail.text.toString()

            if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPassword)){
                Toast.makeText(applicationContext,  "email and password is required", Toast.LENGTH_SHORT).show()
            }
            else
            {
                auth.signInWithEmailAndPassword(userEmail,userPassword)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful){
                            uEmail.setText("")
                            uPassword.setText("")
                            val intent = Intent(this, UsersActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else
                        {
                            Toast.makeText(applicationContext,  "email or password  invalid", Toast.LENGTH_SHORT).show()

                        }
                    }

            }
        }
        buttonSignup.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        buttonSignup = findViewById(R.id.btnSignUp)
        buttonLogin = findViewById(R.id.btnLogin)
        uEmail = findViewById(R.id.etEmail)
        uPassword = findViewById(R.id.etPassword)
    }
}