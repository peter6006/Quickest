package peter.skydev.quickest

import android.Manifest
import android.accounts.AccountManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import io.vrinda.kotlinpermissions.PermissionCallBack
import io.vrinda.kotlinpermissions.PermissionsActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import java.util.regex.Pattern


class MainActivity : PermissionsActivity() {

    private val TAG = "MainActivity"
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userDataDB: DatabaseReference
    private lateinit var db: FirebaseDatabase
    var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        this.currentUser = mAuth!!.currentUser

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            val permissionCheck = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.GET_ACCOUNTS)
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "Permission denied")
                requestPermissions(android.Manifest.permission.GET_ACCOUNTS, object : PermissionCallBack {
                    override fun permissionGranted() {
                        super.permissionGranted()
                        Log.v("Call permissions", "Granted")

                        signInOrLogIn()
                    }

                    override fun permissionDenied() {
                        super.permissionDenied()
                        Log.v("Call permissions", "Denied")
                        alert("We only need your email for leaderboard purposes", "Permission") {
                            yesButton {}
                        }.show()
                    }
                })
            } else {
                Log.d(TAG, "Permission granted")
                signInOrLogIn()
            }
        }else{
            signInOrLogIn()
        }

        if (currentUser != null) {
            setListeners()
        }


        gameButton.setOnClickListener {
            val intent = Intent(this, Game::class.java)
            startActivity(intent)
        }

        rulesButton.setOnClickListener {
            val intent = Intent(this, Rules::class.java)
            startActivity(intent)
        }

        leaderboardButton.setOnClickListener {
            val intent = Intent(this, Leaderboard::class.java)
            startActivity(intent)
        }
    }

    private fun signInOrLogIn(){
        mAuth.createUserWithEmailAndPassword(getEmail(), "passwordDefault")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User created successfully!")
                        currentUser = mAuth!!.currentUser
                        userDataDB.child(currentUser!!.uid).child("UserEmail").setValue(currentUser!!.email)
                        userDataDB.child(currentUser!!.uid).child("UserScore").setValue(0)
                        userDataDB.child(currentUser!!.uid).child("UserPosition").setValue(0)
                        userDataDB.child(currentUser!!.uid).child("UserName").setValue("")

                        setListeners()
                    } else {
                        Log.d(TAG, "There was an error creating the user")
                        if (task.exception.toString().contains("The email address is already in use by another account")) {
                            mAuth.signInWithEmailAndPassword(getEmail(), "passwordDefault").addOnCompleteListener { result ->
                                if (result.isSuccessful) {
                                    currentUser = mAuth!!.currentUser
                                    setListeners()
                                }
                            }
                        }
                    }
                }
    }

    private fun setListeners() {
        FirebaseDatabase.getInstance()
        db = FirebaseDatabase.getInstance()
        userDataDB = db.getReference("UserData")

        userDataDB.child(currentUser!!.uid).child("UserScore").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(p0: DataSnapshot?) {
                tvYourScore.text = p0!!.value.toString()
            }
        })

        userDataDB.child(currentUser!!.uid).child("UserPosition").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(p0: DataSnapshot?) {
                tvYourPosition.text = p0!!.value.toString()
            }
        })
    }

    private fun getEmail(): String {
        val permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "Permission denied")
        } else {
            Log.d(TAG, "Permission granted")
            val emailPattern: Pattern = Patterns.EMAIL_ADDRESS
            val accounts = AccountManager.get(this).accounts
            for (i in 0..accounts.size) {
                if (emailPattern.matcher(accounts[i].name).matches()) {
                    Log.d(TAG, "Email found: " + accounts[i].name)
                    return accounts[i].name
                }
            }
            return "error@error.com"
        }
        return "error@error.com"
    }
}
