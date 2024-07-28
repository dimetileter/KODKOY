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

class LoginPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LoginPageLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if(user != null) {
            val intent = Intent(this, MainPage::class.java)
            val option = actionAnimation()
            startActivity(intent, option)
            finish()
        }

    }


    //==========Butonlar==========//

    //Hesap giriş kontorlü
    public fun btn_giris(view: View) {

        val email = findViewById<EditText>(R.id.etxt_login_email)
        val password = findViewById<EditText>(R.id.etxt_login_password)

        if (email.text.isNullOrEmpty() || password.text.isNullOrEmpty()) {
            errorMesagge("Kullanıcı bilgileri boş bırakılamaz")

        }
        else {
            //Giriş kontorllerine git
            checkLoginInformation(email.text.toString(), password.text.toString())
        }
    }

    //Kayıt ol butonu
    public fun btn_kayitOl(view: View) {

        val intetn = Intent(this, SignInPage::class.java)
        val option = actionAnimation()
        startActivity(intetn,option)

    }





    //==========Kontrol Fonksiyonları==========//

    //Hesap bilgilerinin kontrolü ve hesaba giriş işlemi
    private fun checkLoginInformation(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    //Ana sayfaya geç
                    val intent = Intent(this, MainPage::class.java)
                    val option = actionAnimation()
                    startActivity(intent, option)
                    finish()

                    //Hoşgeldin mesajı göster
                    val user = auth.currentUser?.displayName.toString()
                    welcomeMesaage(user)
                }

            }.addOnFailureListener {
            errorMesagge(it.localizedMessage)
        }
    }





    //==========Mesajlar ve Basit Yapılar==========//

    private fun welcomeMesaage(user: String) {
        Toast.makeText(this, "İyi günler ${user}", Toast.LENGTH_SHORT).show()
    }

    private fun errorMesagge(errorMessage: String?) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    //Geçiş animasyonu
    private fun actionAnimation(): Bundle
    {
        return ActivityOptions
            .makeCustomAnimation(this, R.anim.custom_fade_in, R.anim.custom_fade_out)
            .toBundle()
    }
}