package org.kpu.sinstagram

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException


import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.main_fragment.Main_Fragment1
import org.kpu.sinstagram.main_fragment.Main_Fragment2
import org.kpu.sinstagram.main_fragment.Main_Fragment3
import org.kpu.sinstagram.main_fragment.Main_Fragment4
import org.kpu.sinstagram.utils.Constants.TAG
import java.io.Serializable


class MainActivity : AppCompatActivity() {
    //var mcontext : Context? = null
    private val PERMISSION_CODE = 100
    var mainactivity : MainActivity = this
    private var main_bn_bar: BottomNavigationView? = null
    private var currentuser_email : String = ""
    private var flag = ""
    private var favorite_rcv_list : ArrayList<C_rcv_favorite>? = null
    private var comment_map : MutableMap<String, MutableMap<String, ArrayList<String>>>? = null
    private var user_displayName : String = ""
    private var user_profile_photo : String = ""
    private var data_db_timestamp : String = ""
    private var comment : MutableMap<String, MutableMap<String, ArrayList<String>>>? = null
    private var contents = ""
    private var user_email = ""
    private var doubleBack_exit_flag = false
    private var post_photo : String = ""
    private var post_user_email : String = ""
    //                val fragmentTransaction : FragmentTransaction = mainActivity.supportFragmentManager.beginTransaction()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CODE)
        }


        //mcontext = this
        main_bn_bar = findViewById(R.id.main_bn_bar)
        currentuser_email = FirebaseAuth.getInstance().currentUser?.email!!

        replaceFragment(Main_Fragment1())


        main_bn_bar?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bn_bar_home -> {
                    replaceFragment(Main_Fragment1())
                    true
                }
                R.id.bn_bar_search -> {
                    replaceFragment(Main_Fragment2())
                    true
                }
                R.id.bn_bar_profile -> {
                    replaceFragment(Main_Fragment3())
                    true
                }
                else -> false
            }
        }


    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentmanager = supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        var tag = ""
        when(fragment.javaClass.simpleName){
            "Main_Fragment1" -> tag = "home"
            "Main_Fragment2" -> tag = "search"
            "Main_Fragment3" -> tag = "profile"
            "Main_Fragment4" -> tag = "user_profile"
            "Main_Fragment5" -> tag = "post"
        }
        fragmentmanager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE) // 같은 tag 이름의 stack을 지움
        fragmentTransaction.replace(R.id.main_fragment, fragment, tag)
        fragmentTransaction.addToBackStack(tag)
        //fragmentTransaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.commit()
    }

    val resultlauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val intent = it.data
            user_email = intent?.getStringExtra("user_email")!!
            flag = intent?.getStringExtra("flag")!!
            when(flag){
                "favorite" ->
                    favorite_rcv_list = intent.getSerializableExtra("favorite_rcv_list") as ArrayList<C_rcv_favorite>
                "comment" -> {
                    comment_map = intent.getSerializableExtra("comment_map") as MutableMap<String, MutableMap<String, ArrayList<String>>>
                    user_displayName = intent.getStringExtra("user_displayName")!!
                    user_profile_photo = intent.getStringExtra("user_profile_photo")!!
                    data_db_timestamp = intent.getStringExtra("timestamp")!!
                    contents = intent.getStringExtra("contents")!!
                }
                "recomment" ->{
                    user_displayName = intent.getStringExtra("user_displayName")!!
                    user_profile_photo = intent.getStringExtra("user_profile_photo")!!
                    data_db_timestamp = intent.getStringExtra("timestamp")!!
                    contents = intent.getStringExtra("contents")!!
                    val db = FirebaseFirestore.getInstance()
                    db.collection("data").document(data_db_timestamp).get().addOnSuccessListener {
                        if(it != null) {
                            comment = it.data?.get("comment") as MutableMap<String, MutableMap<String, ArrayList<String>>>
                        }
                    }.addOnFailureListener {
                        Log.e(TAG, "MainActivity - flag(recomment) : data listen fail", it)
                    }
                }
                "postTofavorite" ->{
                    post_photo = intent.getStringExtra("photo")!!
                    favorite_rcv_list = intent.getSerializableExtra("favorite_rcv_list") as ArrayList<C_rcv_favorite>
                    post_user_email = intent.getStringExtra("post_user_email")!!
                }

            }
            if(user_email == currentuser_email){
                replaceFragment(Main_Fragment3())
            }else {
                replaceFragment(Main_Fragment4())
            }
        }
    }

    override fun onBackPressed() {


        when(flag){
            "favorite" -> {
                val intent = Intent(this, FavoriteActivity::class.java)
                intent.putExtra("favorite_rcv_list", favorite_rcv_list)
                resultlauncher.launch(intent)
                flag = "no"
            }
            "comment" -> {
                val intent = Intent(this, CommentActivity::class.java)
                intent.putExtra("comment_map", comment_map as Serializable)
                intent.putExtra("user_displayName", user_displayName)
                intent.putExtra("user_profile_photo", user_profile_photo)
                intent.putExtra("timestamp", data_db_timestamp)
                intent.putExtra("contents", contents)
                resultlauncher.launch(intent)
                flag = "no"
            }
            "recomment" -> {
                val intent = Intent(this, CommentActivity::class.java)
                intent.putExtra("comment", comment as Serializable)
                intent.putExtra("user_displayName", user_displayName)
                intent.putExtra("user_profile_photo", user_profile_photo)
                intent.putExtra("timestamp", data_db_timestamp)
                intent.putExtra("contents", contents)
                resultlauncher.launch(intent)
                flag = "no"
            }
            "search" -> {
                replaceFragment(Main_Fragment2())
                flag = "no"
            }
            "postTofavorite" -> {
                val intent = Intent(this, FavoriteActivity::class.java)
                intent.putExtra("favorite_rcv_list", favorite_rcv_list)
                intent.putExtra("photo", post_photo)
                intent.putExtra("flag", "backTopost")
                intent.putExtra("post_user_email", post_user_email)
                startActivity(intent)
            }
            else -> {
                val count = supportFragmentManager.backStackEntryCount

                if(count ==  0 || count == 1){
                    if(doubleBack_exit_flag){
                        super.onBackPressed()
                        return
                    }else{
                        doubleBack_exit_flag = true
                        Toast.makeText(this, "종료하려면 뒤로가기 버튼을 한 번 더 누르세요.", Toast.LENGTH_SHORT).show()

                        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBack_exit_flag = false}, 2000)
                    }
                }else {
                    super.onBackPressed()
                    val navigation = findViewById<BottomNavigationView>(R.id.main_bn_bar)
                    updateBottomMenu(navigation)
                }
            }
        }


    }

    private fun updateBottomMenu(navigation : BottomNavigationView){
        val tag1 : Fragment? = supportFragmentManager.findFragmentByTag("home")
        val tag2 : Fragment? = supportFragmentManager.findFragmentByTag("search")
        val tag3 : Fragment? = supportFragmentManager.findFragmentByTag("profile")

        if(tag1 != null) navigation.menu.findItem(R.id.bn_bar_home).isChecked = true
        if(tag2 != null) navigation.menu.findItem(R.id.bn_bar_search).isChecked = true
        if(tag3 != null) navigation.menu.findItem(R.id.bn_bar_profile).isChecked = true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity - onDestroy()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity - onResume()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity - onStop()")
    }
}