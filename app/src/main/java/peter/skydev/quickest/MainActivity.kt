package peter.skydev.quickest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}
