package peter.skydev.quickest

import android.Manifest
import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import io.vrinda.kotlinpermissions.PermissionCallBack
import io.vrinda.kotlinpermissions.PermissionsActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import java.util.regex.Pattern
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class MainActivity : PermissionsActivity() {

    private val TAG = "MainActivity"
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userDataDB: DatabaseReference
    private lateinit var userNamesDB: DatabaseReference
    private lateinit var userNamesDataSnapshot: DataSnapshot
    var userName: String = ""
    lateinit var mAdView : AdView

    private lateinit var db: FirebaseDatabase
    var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        this.currentUser = mAuth!!.currentUser

        MobileAds.initialize(this,
                "ca-app-pub-4855450974262250~6878071293")

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                Log.d("MAIN", "onAdLoaded")
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                Log.d("MAIN", "onAdFailedToLoad" + errorCode)
            }

            override fun onAdOpened() {
                Log.d("MAIN", "onAdOpened")
            }

            override fun onAdLeftApplication() {
                Log.d("MAIN", "onAdLeftApplication")
            }

            override fun onAdClosed() {
                Log.d("MAIN", "onAdClosed")
            }
        }

        val permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.GET_ACCOUNTS)
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            askForPermission()
        } else {
            signInOrLogIn()
        }

        if (currentUser != null) {
            setListeners()
        }

        tvYourName.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.custom_dialog, null)
            dialogBuilder.setView(dialogView)

            val editText = dialogView.findViewById<View>(R.id.editTextName) as EditText

            dialogBuilder.setMessage(resources.getString(R.string.resultAddName))
            dialogBuilder.setPositiveButton("Save") { _, _ ->
                if (!userNamesDataSnapshot.hasChild(editText.text.toString())) {
                    userDataDB.child(currentUser!!.uid).child("UserName").setValue(editText.text.toString())

                    userNamesDB.child(userName).removeValue()
                    userNamesDB.child(editText.text.toString()).setValue(currentUser!!.uid)
                } else {
                    toast(resources.getString(R.string.resultAddNameExists))
                    showNewNameDialog()
                }
            }
            dialogBuilder.setNegativeButton("Cancel") { _, _ ->
                //pass
            }
            val b = dialogBuilder.create()
            b.show()
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

    private fun askForPermission() {
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
                        alert("We only need access to your account in order to obtain your email for leaderboard purposes", "Permission") {
                            yesButton {
                                askForPermission()
                            }
                            onCancel {
                                askForPermission()
                            }
                        }.show()
                    }
                })
            } else {
                Log.d(TAG, "Permission granted")
                signInOrLogIn()
            }
        } else {
            signInOrLogIn()
        }
    }

    private fun signInOrLogIn() {
        mAuth.createUserWithEmailAndPassword(getEmail(), "passwordDefault")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirebaseDatabase.getInstance()
                        db = FirebaseDatabase.getInstance()
                        userDataDB = db.getReference("UserData")
                        userNamesDB = db.getReference("UserNames")

                        Log.d(TAG, "User created successfully!")
                        currentUser = mAuth!!.currentUser
                        userDataDB.child(currentUser!!.uid).child("UserEmail").setValue(currentUser!!.email)
                        userDataDB.child(currentUser!!.uid).child("UserScore").setValue(0)
                        userDataDB.child(currentUser!!.uid).child("UserPosition").setValue(0)
                        userDataDB.child(currentUser!!.uid).child("UserName").setValue("")

                        setListeners()

                        showNewNameDialog()
                    } else {
                        Log.d(TAG, "There was an error creating the user")
                        if (task.exception.toString().contains("The email address is already in use by another account")) {
                            FirebaseDatabase.getInstance()
                            db = FirebaseDatabase.getInstance()
                            userDataDB = db.getReference("UserData")
                            userNamesDB = db.getReference("UserNames")

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
        userNamesDB = db.getReference("UserNames")

        userDataDB.child(currentUser!!.uid).child("UserScore").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                tvYourScore.text = p0!!.value.toString()
            }
        })

        userDataDB.child(currentUser!!.uid).child("UserPosition").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                tvYourPosition.text = p0!!.value.toString()
            }
        })

        userDataDB.child(currentUser!!.uid).child("UserName").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                tvYourName.setText(p0!!.value.toString() + ":")
                userName = p0!!.value.toString()
            }
        })

        userNamesDB.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                userNamesDataSnapshot = p0!!
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

    fun showNewNameDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)

        val editText = dialogView.findViewById<View>(R.id.editTextName) as EditText

        dialogBuilder.setMessage(resources.getString(R.string.resultAddName))
        dialogBuilder.setPositiveButton("Save", DialogInterface.OnClickListener { dialog, whichButton ->
            if (!userNamesDataSnapshot.hasChild(editText.text.toString())) {

                userDataDB.child(currentUser!!.uid).child("UserName").setValue(editText.text.toString())

                userNamesDB.child(editText.text.toString()).setValue(currentUser!!.uid)
            } else {
                toast(resources.getString(R.string.resultAddNameExists))
                showNewNameDialog()
            }
        })
        dialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, whichButton ->
            //pass
        })
        val b = dialogBuilder.create()
        b.show()
    }
}
