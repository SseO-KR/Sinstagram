package org.kpu.sinstagram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageView

import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.kpu.sinstagram.utils.Constants
import org.kpu.sinstagram.utils.Constants.TAG
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class UploadActivity : AppCompatActivity() {

    var upload_image : ImageView? = null
    var photo_uri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        upload_image = findViewById(R.id.upload_image)
        val storage = FirebaseStorage.getInstance()

        var db : FirebaseFirestore = FirebaseFirestore.getInstance()
        val auth = Firebase.auth
        val currentuser = auth.currentUser

        val upload_check = findViewById<ImageView>(R.id.upload_check)
        val upload_quit = findViewById<ImageView>(R.id.upload_image)
        val upload_contents = findViewById<EditText>(R.id.upload_contents)

        upload_quit.setOnClickListener {
            finish()
        }

        upload_image?.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startForResult.launch(intent)
        }

        upload_check.setOnClickListener {

            val filename = SimpleDateFormat("yyyyMMddhhmmss").format(Date()) + ".jpg"

            val img_reference = storage.reference.child(filename)

            img_reference.putFile(photo_uri!!).addOnSuccessListener { _ ->
                Log.d(TAG, "Upload Activity - Upload Success")

                img_reference.downloadUrl.addOnSuccessListener { storage_photo_uri ->
                    photo_uri = storage_photo_uri
                    val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                    val data = hashMapOf("email" to currentuser?.email,
                        "photo" to photo_uri.toString(),
                        "contents" to upload_contents.text.toString(),
                        "favorite_map" to mutableMapOf<String, String>(),
                        "comment" to mutableMapOf<String, MutableMap<String, ArrayList<String>>>(),
                        "timestamp" to timestamp
                    )

                    db.collection("data").document(timestamp).set(data).addOnSuccessListener {
                        Log.d(TAG, "UploadActivity Photo Data Upload Success")

                    }.addOnFailureListener { e ->
                        Log.e(TAG, "UploadActivity Photo Data Upload Fail", e)
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "Upload storage downloadurl fail", it)
                }

            }




            finish()
        }


    }



    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result : ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK){
            try{
                val intent = result.data
                photo_uri = intent?.data
                Glide.with(application).load(photo_uri).into(upload_image!!)
                // 위는 glide 이용
                //ImageView.setImageResource(uri)
            } catch (e : Exception){
                Log.e(Constants.TAG, "Upload Activity - Bring Image Error", e)
            }

        }
    }

}