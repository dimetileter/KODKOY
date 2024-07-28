package com.aliosman.paylasimuygulamasi.view

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aliosman.paylasimuygulamasi.R
import com.aliosman.paylasimuygulamasi.databinding.ActivityMainPageBinding
import com.aliosman.paylasimuygulamasi.model.SharingModel
import com.aliosman.paylasimuygulamasi.recyclerAdapter.RecyclerAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class MainPage : AppCompatActivity() {

    private lateinit var binding: ActivityMainPageBinding
    private lateinit var popupMenu: PopupMenu
    val modelList = ArrayList<SharingModel>()
    var adapter: RecyclerAdapter? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        getSharingFromDatabase()
        adapter = RecyclerAdapter(modelList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter


        //Menü tanımlaması
        popupMenu = PopupMenu(this, binding.btnPopupMenu)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.cikis_menusu, popupMenu.menu)


    }


    //==========Butonlar==========//
    //Gönderi eklemek
    public fun btn_ekle(view: View)
    {
        val intent = Intent(this, AddImagePage::class.java)
        startActivity(intent)
    }

    //Popup menüsüne erişim
    public fun btn_popupMenu(view: View)
    {
        showPopupMenu(view)
    }


    //==========Kontroller ve İşlemler==========//
    //Popup Menü oluştur
    private fun showPopupMenu(view: View) {

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->

            when (item.itemId) {
                //TODO: Çıkış yapıldığında bir hata mesajı veriyor bunu düzelt!
                R.id.menu_item_cikisyap -> {
                    auth.signOut()
                    toastMessage("Çıkış yapıldı")
                    startLoginActivity()
                    true
                }

                R.id.menu_item_test -> {
                    toastMessage("test tamamlandı")
                    true
                }
                else -> false
            }
        }
    }

    //Login aktiviteye geç
    private fun startLoginActivity()
    {
        val intent = Intent(this, LoginPage::class.java)
        val option = ActivityOptions
            .makeCustomAnimation(this, R.anim.custom_fade_in, R.anim.custom_fade_out)
            .toBundle()

        startActivity(intent,option)
        finish()
    }

    //Verileri al
    private fun getSharingFromDatabase()
    {
        db.collection("sharings").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if(error == null && value != null && !value.isEmpty)
            {
                val documents = value.documents
                modelList.clear()

                for (document in documents)
                {
                    val userName = document.get("userName") as String
                    val description = document.get("description") as String?
                    val timeStamp = document.get("time") as Timestamp
                    val location = document.get("location") as String?
                    val url = document.get("url") as String

                    val date = timeStamp.toDate()
                    val time = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)

                    //Alınan verileri data sınıfı aracılığıyla RecyclerAdaptöre gönder
                    val sharing = SharingModel(userName, description, time, location, url)
                    modelList.add(sharing)
                    RecyclerAdapter(modelList)
                }
                adapter!!.notifyDataSetChanged()
            }
            else
            {
                error?.let {
                    toastMessage(error.localizedMessage)
                }
            }
        }
    }





    //==========Mesajlar ve Bildirimler==========//

    //Toast mesajı
    private fun toastMessage(message: String)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}