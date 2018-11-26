package peter.skydev.quickest

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_result.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class Result : Activity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userDataDB: DatabaseReference
    private lateinit var userNamesDB: DatabaseReference
    private lateinit var userNamesDataSnapshot: DataSnapshot
    private lateinit var leaderboardDataDB: DatabaseReference
    private lateinit var db: FirebaseDatabase
    var currentUser: FirebaseUser? = null
    var userName: String = ""
    private var result: Long = 0
    private val TAG: String = "Result"
    private var savedScore: Int = 0
    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.GET_ACCOUNTS)
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
//            Toast.makeText(this, "You have to give permission in order to access the leaderboard", Toast.LENGTH_LONG)
            uploadButton.visibility = View.GONE
        } else {
            mAuth = FirebaseAuth.getInstance()
            this.currentUser = mAuth!!.currentUser

            FirebaseDatabase.getInstance()
            db = FirebaseDatabase.getInstance()
            userDataDB = db.getReference("UserData")
            userNamesDB = db.getReference("UserNames")
            leaderboardDataDB = db.getReference("Leaderboard")

            userDataDB.child(currentUser!!.uid).child("UserName").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    userName = p0!!.value.toString()
                }
            })

            userNamesDB.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    userNamesDataSnapshot = p0!!
                }
            })

            userDataDB.child(currentUser!!.uid).child("UserScore").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    savedScore = Integer.parseInt(p0!!.value.toString())
                }
            })

            result = intent.getLongExtra("finalScore", 0)
            uploadButton.setOnClickListener {
                if(result > savedScore) {
                    alert("Your saved score is better than your actual score, if you upload it you may will lose some positions", "STOP!") {
                        yesButton {
                            if (userName.equals("")) {
                                showNewNameDialog()
                            } else {
                                uploadToFirebase()
                            }
                        }
                    }.show()
                } else {
                    if (userName.equals("")) {
                        showNewNameDialog()
                    } else {
                        uploadToFirebase()
                    }
                }
            }
        }


        yourResult.text = Objects.toString(result, "Error")
        yourEquivalent.text = Objects.toString((result / 1000000000.0), "").substring(0, 5) + " s"

        repeatButton.setOnClickListener {
            finish()

            val intent = Intent(this, Game::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    fun showNewNameDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)

        val editText = dialogView.findViewById<View>(R.id.editTextName) as EditText

        dialogBuilder.setMessage(resources.getString(R.string.resultAddName))
        dialogBuilder.setPositiveButton("Save") { dialog, whichButton ->
            if (!userNamesDataSnapshot.hasChild(editText.text.toString())) {
                userDataDB.child(currentUser!!.uid).child("UserName").setValue(editText.text.toString())
                uploadToFirebase()

                userNamesDB.child(userName).removeValue()
                userNamesDB.child(editText.text.toString()).setValue(currentUser!!.uid)
            } else {
                toast(resources.getString(R.string.resultAddNameExists))
                showNewNameDialog()
            }
        }
        dialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, whichButton ->
            //pass
        })
        val b = dialogBuilder.create()
        b.show()
    }

    fun uploadToFirebase(){
        userDataDB.child(currentUser!!.uid).child("UserScore").setValue(result)

        userDataDB.orderByChild("UserScore").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                var jsonArray = JSONArray()

                for (messageSnapshot in p0!!.getChildren()) {
                    Log.d(TAG, "Snapshot: " + messageSnapshot.toString())
                    var job = JSONObject()
                    job.put("UserScore", messageSnapshot.child("UserScore").getValue())
                    job.put("UserName", messageSnapshot.child("UserName").getValue())
                    job.put("appid", messageSnapshot.key as String)
                    jsonArray.put(job)
                }

                var jsonArrayFinal = JSONArray()

                for(j in 0..jsonArray.length()-1){
                    var jsonO = jsonArray.getJSONObject(j)
                    if(jsonO.getInt("UserScore") != 0) {
                        jsonArrayFinal.put(jsonO)
                    }
                }

                for(j in 0..jsonArrayFinal.length()-1){
                    var jsonO = jsonArrayFinal.getJSONObject(j)
                    if(jsonO.getInt("UserScore") != 0) {
                        leaderboardDataDB.child(Integer.toString(j + 1)).child("appid").setValue(jsonO.getString("appid"))
                        leaderboardDataDB.child(Integer.toString(j + 1)).child("UserScore").setValue(jsonO.getInt("UserScore"))
                        leaderboardDataDB.child(Integer.toString(j + 1)).child("UserName").setValue(jsonO.getString("UserName"))

                        userDataDB.child(jsonO.getString("appid")).child("UserPosition").setValue(j + 1)
                    }
                }

                toast(resources.getString(R.string.resultUploadSuccessful))
            }
        })
    }
}
