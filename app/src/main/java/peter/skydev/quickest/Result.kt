package peter.skydev.quickest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*
import java.util.*

class Result : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

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
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
