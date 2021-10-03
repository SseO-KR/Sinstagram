package org.kpu.sinstagram

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.kpu.sinstagram.utils.Constants.TAG
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {
    private val PERMISSION_CODE = 100
    private var googleSignInClient: GoogleSignInClient? = null
    private var auth: FirebaseAuth = Firebase.auth
    private var edittext_email: EditText? = null
    private var edittext_passwd: EditText? = null
    private var dialog_edt_email : EditText? = null
    private var dialog_edt_passwd : EditText? = null
    private var dialog_edt_displayName : EditText? = null
    private var dialog_imgview_photouri : ImageView? = null
    private var user_photo_uri : Uri? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CODE)
        }

        val btn_login = findViewById<Button>(R.id.btn_login)
        val btn_joinin = findViewById<Button>(R.id.btn_joinin)
        edittext_email = findViewById<EditText>(R.id.login_edittext_email)
        edittext_passwd = findViewById<EditText>(R.id.login_edittext_password)
        val btn_signin_google = findViewById<SignInButton>(R.id.btn_signin_google)




        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btn_login.setOnClickListener {
            if((edittext_email?.text.toString().equals("") != true )&& (edittext_passwd?.text.toString().equals("") != true)){
                try{
                    signInEmail(edittext_email?.text.toString(),
                        edittext_passwd?.text.toString()
                    )
                }catch(e: Exception){
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "signInEmail fail", e)
                }
            }
        }

        val imgview = findViewById<ImageView>(R.id.imageView)
        imgview.setOnClickListener {
            edittext_email?.requestFocus()
            val inputmethodmanager : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputmethodmanager.showSoftInput(edittext_email, InputMethodManager.SHOW_IMPLICIT)
        }

        edittext_email?.clearFocus()

        edittext_email?.setOnKeyListener { view, i, keyEvent ->
            if(keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER){
                edittext_passwd?.performClick()
                true
            }
            false
        }

        edittext_passwd?.setOnKeyListener { view, i, keyEvent ->
            if(keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER){
                btn_login.performClick()
                val inputmethodmanager : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputmethodmanager.hideSoftInputFromWindow(edittext_passwd?.windowToken, 0)
                true
            }
            false
        }

        btn_joinin.setOnClickListener {
            makeDialog()
        }

        btn_signin_google.setOnClickListener{
            signInGoogle()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LoginActivity - onDestory()")
    }

    private fun signInEmail(email : String, passwd : String) {
        auth.signInWithEmailAndPassword(email, passwd)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Log.d(TAG, "signInWithEmail : success")
                    val intent = Intent(this, MainActivity::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener(this) {
                Log.w(TAG, "signInWithEmail : Fail", it)
                Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient?.signInIntent
        signForResult.launch(signInIntent)
    }

    val signForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "FirebaseAuthWithGoogle : " + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithCredential : Success")
            } else {
                Log.w(TAG, "signInWithCredential : failed", task.exception)
            }
        }
    }

    private fun join_in_email(email : String, passwd : String){

        if(email.contains("@") == true){
            auth.createUserWithEmailAndPassword(email, passwd)
                .addOnCompleteListener(this){
                    if(it.isSuccessful){
                        Log.d(TAG, "createUserWithEmail : Success")
                        Toast.makeText(this, "이메일 등록 성공", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener(this){
                    Log.e(TAG, "createUserWithEmail : Fail", it)
                    Toast.makeText(this@LoginActivity, "Create User failed", Toast.LENGTH_LONG).show()
                }
        }else{
            Toast.makeText(this@LoginActivity, "제대로 된 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
        }

    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result : ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK){
            try{
                val intent = result.data
                user_photo_uri = intent?.data

                Glide.with(application).load(user_photo_uri).into(dialog_imgview_photouri!!)
                // 위는 glide 이용
                //ImageView.setImageResource(uri)
            } catch (e : Exception){
                Log.e(TAG, "Bring Image Error", e)
            }

        }
    }

    private fun makeDialog(){
        val dialogView = layoutInflater.inflate(R.layout.joinin_dialog, null)
        val builder = AlertDialog.Builder(this)

        dialog_edt_email = dialogView.findViewById<EditText>(R.id.dialog_edt_email)
        dialog_edt_passwd = dialogView.findViewById<EditText>(R.id.dialog_edt_passwd)
        dialog_edt_displayName = dialogView.findViewById<EditText>(R.id.dialog_edt_displayName)
        dialog_imgview_photouri = dialogView.findViewById<ImageView>(R.id.dialog_imgview_photouri)

        dialog_imgview_photouri?.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startForResult.launch(intent)
        }


        builder.setTitle("join in")
        builder.setView(dialogView).setPositiveButton(R.string.signin){ _, _ ->

            if(dialog_edt_email?.text.toString().contains("@") == true){
                val user_email = dialog_edt_email?.text.toString()

                join_in_email(user_email, dialog_edt_passwd?.text.toString())


                val storage = FirebaseStorage.getInstance()
                val reference = storage.reference
                val filename = SimpleDateFormat("yyyyMMddhhmmss").format(Date()) + ".jpg"
                val user_name = dialog_edt_displayName?.text.toString()


                val db = FirebaseFirestore.getInstance()
                val img_reference = reference.child(filename)

                img_reference.putFile(user_photo_uri!!).addOnSuccessListener { _ ->
                    Log.d(TAG, "Login Activity - storage photo upload success")

                    img_reference.downloadUrl.addOnSuccessListener { storage_photo_uri ->

                        Log.d(TAG, "Login Activity - storage downloadurl success")

                        val profile_data = hashMapOf("email" to user_email, "displayName" to user_name, "introduction" to "", "profile_photo" to storage_photo_uri.toString())
                        db.collection("profile").document(user_email).set(profile_data).addOnSuccessListener {
                            Log.d(TAG, "Login Activity - Profile data upload success")
                        }.addOnFailureListener {
                            Log.e(TAG, "Login Activity - Profile data upload fail")
                        }

                        val follow_data = hashMapOf("email" to user_email, "follower" to mutableMapOf<String, String>(), "following" to mutableMapOf<String, String>())
                        db.collection("follow").document(user_email).set(follow_data).addOnSuccessListener {
                            Log.d(TAG, "Login Activity - Follow data upload success")
                        }.addOnFailureListener {
                            Log.e(TAG, "Login Activity - Follow data upload fail", it)
                        }

                        val search_data = hashMapOf("email" to user_email, "search_list" to arrayListOf<String>())
                        db.collection("search").document(user_email).set(search_data).addOnSuccessListener {
                            Log.d(TAG, "Login Activity - Search data upload success")
                        }.addOnFailureListener {
                            Log.e(TAG, "Login Activity - Search data upload fail", it)
                        }



                    }.addOnFailureListener {
                        Log.e(TAG, "Login Activity - storage downloadurl fail")
                    }

                }.addOnFailureListener{
                    Log.e(TAG, "Login Activity - photo upload fail", it)
                }



            }else{
                Toast.makeText(this, "이메일을 제대로 입력해주세요", Toast.LENGTH_SHORT).show()
            }

        }.setNegativeButton("Cancel"){_, _ ->

        }.show()
    }

}