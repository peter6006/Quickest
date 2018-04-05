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
import io.vrinda.kotlinpermissions.PermissionCallBack
import io.vrinda.kotlinpermissions.PermissionsActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import java.util.regex.Pattern


class MainActivity : PermissionsActivity() {

    private val TAG = "MainActivity"
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            val permissionCheck = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.GET_ACCOUNTS)
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "Permission denied")
                requestPermissions(android.Manifest.permission.GET_ACCOUNTS, object : PermissionCallBack {
                    override fun permissionGranted() {
                        super.permissionGranted()
                        Log.v("Call permissions", "Granted")
                        mAuth = FirebaseAuth.getInstance()

                        mAuth.createUserWithEmailAndPassword(getEmail(), "passwordDefault")
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "User created successfully!")
                                    } else {
                                        Log.d(TAG, "There was an error creating the user")
                                    }
                                }
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
            }
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

        }
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
