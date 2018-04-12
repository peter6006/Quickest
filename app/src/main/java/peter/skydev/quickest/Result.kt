package peter.skydev.quickest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_result.*
import java.util.*

class Result : Activity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userDataDB: DatabaseReference
    private lateinit var db: FirebaseDatabase
    var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        mAuth = FirebaseAuth.getInstance()
        this.currentUser = mAuth!!.currentUser

        FirebaseDatabase.getInstance()
        db = FirebaseDatabase.getInstance()
        userDataDB = db.getReference("UserData")

        val result: Long = intent.getLongExtra("finalScore", 0)

        yourResult.text = Objects.toString(result, "Error")
        yourEquivalent.text = Objects.toString((result / 1000000000.0), "").substring(0, 5) + " s"

        repeatButton.setOnClickListener {
            finish()

            val intent = Intent(this, Game::class.java)
            startActivity(intent)
        }

        uploadButton.setOnClickListener {
            // TODO Firebase system
            userDataDB.child(currentUser!!.uid).child("UserScore").setValue(result)
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
