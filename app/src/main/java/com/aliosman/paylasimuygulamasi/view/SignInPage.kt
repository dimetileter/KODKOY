package com.aliosman.paylasimuygulamasi.view

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aliosman.paylasimuygulamasi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignInPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.SignInPageLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

    }

    //Kayıt ol butonu
    fun btn_hesapOlustur(view: View)
    {
        val userName = findViewById<EditText>(R.id.etxt_register_nickname)
        val email = findViewById<EditText>(R.id.etxt_register_email)
        val password = findViewById<EditText>(R.id.etxt_register_password)

        if(userName.text.isNullOrEmpty() || email.text.isNullOrEmpty() || password.text.isNullOrEmpty()) {
            errorMessage("Kullanıcı bilgileri boş bırakılamaz")

        }else {
            //Girilen kayıt bilgilerini al
            saveLoginInfermation(userName.text.toString(), email.text.toString(), password.text.toString())
        }
    }





    //==========Kontroller==========//

    //LoginPage aktivitesine geçiş
    private fun startLoginActivity()
    {
        val intent = Intent(this, LoginPage::class.java)
        val option = ActivityOptions.makeCustomAnimation(this,
            R.anim.custom_fade_in,
            R.anim.custom_fade_out
        ).toBundle()
        startActivity(intent,option)
    }

    //Kullanıcı kayıt işlemlerini tamamla
    private fun saveLoginInfermation(name: String, email: String, password: String)
    {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {

                //Şuanki kullanıcıya isim ekle
                val user = auth.currentUser
                user?.let {

                    //Kullanıcı ismini yerlerştir ve güncelle
                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    user.updateProfile(profileUpdates).addOnCompleteListener { profileUpdateTask ->

                        if (!profileUpdateTask.isSuccessful) {
                            errorMessage("isim eklemek başarısız")
                        }
                    }
                }

                //İşlemler tamamlanınca giriş sayfasına geri dön
                startLoginActivity()
                finish()
                successfulRegistraitonMessage()
            }

        }.addOnFailureListener {
            errorMessage(it.localizedMessage)
        }
    }





    //==========Mesajlar ve Bildirimler==========//

    //kayıt başarılı mesajı
    private fun successfulRegistraitonMessage()
    {
        Toast.makeText(this,"Kayıt başarıyla tamamlandı. Giriş yapınız", Toast.LENGTH_SHORT).show()
    }

    private fun errorMessage(errorMessage: String?)
    {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}