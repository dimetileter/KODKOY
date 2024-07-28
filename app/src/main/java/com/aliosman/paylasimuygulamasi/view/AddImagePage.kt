package com.aliosman.paylasimuygulamasi.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.aliosman.paylasimuygulamasi.R
import com.aliosman.paylasimuygulamasi.databinding.ActivityAddImagePageBinding
import com.aliosman.paylasimuygulamasi.databinding.FindingLocationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.Locale
import java.util.UUID


class AddImagePage : AppCompatActivity() {

    private var selectedImageBitmap: Bitmap? = null
    private var selectedImageUri: Uri? = null
    private var sharingInformation = HashMap<String, Any>()
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var binding: ActivityAddImagePageBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncherGallery: ActivityResultLauncher<String>
    private lateinit var permissionLauncherLocation: ActivityResultLauncher<String>

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddImagePageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore

        activityResultLauncher()
        permissionLauncherGallery()
        permissionLauncherLocation()

        binding.txtKullaniciAdi.text = "@" + auth.currentUser?.displayName.toString()
    }


    //==========Buton İşlemleri==========//

    //Anasayfaya dön butonu
    public fun btn_geri_don(view: View)
    {
        val intent = Intent(this, MainPage::class.java)
        startActivity(intent)
        finish()
    }

    //Firebase paylaşım işlemleri
    public fun btn_paylas(view: View)
    {
        saveImageWithFirebaseStorage()
    }

    //Resim ekle butonu ve galeri izin kontrolü
    public fun btn_resim_ekle(view: View)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            //İizn verilmemişse izin iste verilmişse galeriye git
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                askForGalleryPermission(view, Manifest.permission.READ_MEDIA_IMAGES)
            }
            else
            {
                openGallery()
            }
        }
        //SDK tiramusu altı seviyeler için işlemler
        else
        {
            //İzin verilmemişse izin iste verilmişse galeriye git
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                askForGalleryPermission(view, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else
            {
                openGallery()
            }
        }
    }

    //Konum izni iste ve konum işlemlerine git
    public fun btn_konum_ekle(view: View)
    {
        //Lokasyon yöneticisini başlat
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //İzin kontrollerini yap
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            askForLocationPermission(view)
        }
        else if( isLocationIsEnable())
        {
            //Konum izni verildi
            val alertDialog = findingLocatonLoadingAlert()
            alertDialog.show()

            //Konumu al daha sonra uyarıyı kapat
            getLocation(alertDialog)

        }
        else
        {
            snackbarLoacationSettingAction()
        }
    }





    //==========Kontroller ve İşlemler==========//
    //Resim kayıt işlemleri
    private fun saveImageWithFirebaseStorage()
    {
        if(selectedImageUri != null)
        {
            binding.progressBar.visibility = View.VISIBLE
            val uuid = UUID.randomUUID()
            val imageUUID = "${uuid}.jpeg"

            //Dosya yolu ve dosya ismi oluşturuldu
            val referance = storage.reference
            val imageReferance = referance.child("image").child(imageUUID) // Resimleri imageUUID ismi ile kaydet

            //Eğer seçili resim varsa resmi ilgili referansa koy ve yükle
            imageReferance.putFile(selectedImageUri!!).addOnSuccessListener {
                imageReferance.downloadUrl.addOnSuccessListener {
                    //Diğer paylaşım bilgilerini yükle
                    val url = it.toString()
                    saveInfoWithFirebaseFirestore(url)
                }
            }.addOnFailureListener{
                toastMessage(it.localizedMessage)
            }
        }
        else
        {
            toastMessage("Bir resim seçiniz")
        }
    }

    //Paylaşım bilgileri
    private fun saveInfoWithFirebaseFirestore(url: String)
    {
        val userName = auth.currentUser!!.displayName.toString()
        val description = binding.txtGoderiYorumu.text.toString()
        val time = Timestamp.now()
        val location = binding.txtEklenecekKonumBilgisi.text.toString()

        sharingInformation.put("userName", userName)
        sharingInformation.put("description", description)
        sharingInformation.put("time", time)
        sharingInformation.put("location", location)
        sharingInformation.put("url", url)


        db.collection("sharings").add(sharingInformation).addOnCompleteListener {
            if(it.isSuccessful)
            {
                binding.progressBar.visibility = View.GONE
                toastMessage("Paylaşıldı")
                val intent = Intent(this, MainPage::class.java)
                startActivity(intent)
                finish()
            }

        }.addOnFailureListener {
            toastMessage(it.localizedMessage)
        }
    }

    //Konum alınıyor yükleme ekranı
    private fun findingLocatonLoadingAlert(): AlertDialog
    {
        //Konum yükleniyor bildirimi tanımla
        val alert = AlertDialog.Builder(this)
        val alertBinding = FindingLocationBinding.inflate(layoutInflater)

        //Bildirim özelliklerini ayarla
        alert.setCancelable(false)
        alert.setView(alertBinding.root)

        //Bildirimi oluştur ve göster
        val alertDialog = alert.create()
        val backgroundDrawable = ColorDrawable(Color.TRANSPARENT)
        alertDialog.window?.setBackgroundDrawable(backgroundDrawable)

        return alertDialog
    }

    //Konum servisi açık mı?
    private fun isLocationIsEnable(): Boolean
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }



    //==========İzin İşlemleri ve Resim-Konum Seçimi==========//

    //Galeri izni isite ve gerekliliğini anlat
    private fun askForGalleryPermission(view: View, permission: String)
    {
        //İzin verilmemişse iste verilmezse snackbar ile açıkla
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
        {
            Snackbar.make(view, "Galeri erişimi için izin gerekli", Snackbar.LENGTH_LONG)
                    .setAction("İzin ver", View.OnClickListener {
                        permissionLauncherGallery.launch(permission)
                    }).show()
        }
        else
        {
            permissionLauncherGallery.launch(permission)
        }
    }

    //Konum izni iste ve gerekliliğini anlat
    private fun askForLocationPermission(view:  View)
    {
        //İzin verilmemişse iste verilmezse snackbar ile açıkla
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            Snackbar.make(view, "Konuma bilgisi için izin gerekli", Snackbar.LENGTH_LONG)
                .setAction("İzin ver", View.OnClickListener {
                    permissionLauncherLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }).show()
        }
        else
        {
            permissionLauncherLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    //İzin verilmişse galeriye git
    private fun openGallery()
    {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intent)
    }

    //Konumu al
    private fun getLocation(alertDialog: AlertDialog)
    {
        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location) {
                val geoCoder = Geocoder(this@AddImagePage, Locale.getDefault())

                try
                {
                    val userLocationList = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (userLocationList != null && userLocationList.size > 0)
                    {
                        var userLocationString = userLocationList.get(0).subLocality?.toString() // İlçe
                        userLocationString += " " + userLocationList.get(0).featureName?.toString() // Bölge
                        userLocationString += "/" + userLocationList.get(0).subAdminArea?.toString() // Şehir

                        if (!userLocationString.isNullOrEmpty())
                        {
                            binding.txtEklenecekKonumBilgisi.text = userLocationString
                            binding.txtEklenecekKonumBilgisi.visibility = View.VISIBLE
                            alertDialog.dismiss()
                        }
                        else
                        {
                            toastMessage("Konum bilgisi alınırken bir hata oluştu")
                            alertDialog.dismiss()
                        }
                    }
                }
                catch (ex: Exception)
                {
                    toastMessage(ex.localizedMessage)
                    alertDialog.dismiss()
                }
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000f, locationListener)

        }
    }

    //İzin sonucuna göre seçilen resim için işlemleri yap - Aktivite sonucunu değerlendir
    private fun activityResultLauncher()
    {
        //Galeri seçimi (izin) sonucuna göre işleme gidilmesi
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            //İzin verilmişse
            if(it.resultCode == RESULT_OK)
            {
                //Veriyi al
                val galleryIntentResult = it.data

                if(galleryIntentResult != null)
                {
                    //Uri olarak resmi al
                    selectedImageUri = galleryIntentResult.data
                    try
                    {
                        //SDK sürümüne göre resmi bitmap olarak çevir
                        if(Build.VERSION.SDK_INT >= 28)
                        {
                            val source = ImageDecoder.createSource(this.contentResolver, selectedImageUri!!)
                            selectedImageBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedImageBitmap)

                        }
                        else
                        {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                            binding.imageView.setImageBitmap(selectedImageBitmap)
                        }
                    }
                    catch (e: Exception)
                    {

                    }
                }
            }
        }
    }

    //İzin sonucunu değerlendir
    private fun permissionLauncherGallery()
    {
        //İzin sonuçlarının değerlendirilmesi
        permissionLauncherGallery = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            //İzin verilmişse galeriye git
            if(it)
            {
                openGallery()
            }
            else
            {
                toastMessage("Medya erişimini ayarlardan açınız")
            }
        }
    }

    //İzin sonucunu değerlendir
    private fun permissionLauncherLocation()
    {
        permissionLauncherLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            if(it)
            {
                if (isLocationIsEnable())
                {
                    //İizn verildi ise konum alınıyor uyarısı göster ve konumu al
                    val alertDialog = findingLocatonLoadingAlert()
                    alertDialog.show()
                    getLocation(alertDialog)
                }
                else
                {
                    snackbarLoacationSettingAction()
                }

            }
            else
            {
                toastMessage("Konum erişimini ayarlardan izin veriniz")
            }

        }
    }




    //=============Bildirimler============//

    private fun toastMessage(message: String)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun snackbarLoacationSettingAction()
    {
        Snackbar.make(binding.root, "Konum servisini açın", Snackbar.LENGTH_LONG)
            .setAction("Konumu aç", View.OnClickListener {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }).show()
    }



}