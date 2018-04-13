package peter.skydev.quickest

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_leaderboard.*
import org.json.JSONArray
import org.json.JSONObject

class Leaderboard : Activity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var leaderboardDataDB: DatabaseReference
    private lateinit var db: FirebaseDatabase
    var currentUser: FirebaseUser? = null
    var jsonArray = JSONArray()

    private val TAG: String = "Leaderboard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val lv = findViewById<ListView>(R.id.list)

        mAuth = FirebaseAuth.getInstance()
        this.currentUser = mAuth!!.currentUser

        FirebaseDatabase.getInstance()
        db = FirebaseDatabase.getInstance()
        leaderboardDataDB = db.getReference("Leaderboard")

        leaderboardDataDB.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(p0: DataSnapshot?) {
                var pos = 0
                var aux = 0
                for (messageSnapshot in p0!!.getChildren()) {
                    Log.d(TAG, "Snapshot: " + messageSnapshot.toString())
                    var job = JSONObject()
                    job.put("UserScore", messageSnapshot.child("UserScore").getValue())
                    job.put("UserName", messageSnapshot.child("UserName").getValue())
                    job.put("appid", messageSnapshot.child("appid").getValue())
                    jsonArray.put(job)

                    if (currentUser!!.uid.equals(messageSnapshot.child("appid").getValue())) {
                        pos = aux
                    } else {
                        aux++
                    }
                }

                if(pos>2){
                    pos = pos - 3
                } else {
                    pos = 0
                }

                leaderboardFirstName.text = jsonArray.getJSONObject(0).getString("UserName")
                leaderboardFirstPoints.text = jsonArray.getJSONObject(0).getString("UserScore")

                leaderboardSecondName.text = jsonArray.getJSONObject(1).getString("UserName")
                leaderboardSecondPoints.text = jsonArray.getJSONObject(1).getString("UserScore")

                leaderboardThirdName.text = jsonArray.getJSONObject(2).getString("UserName")
                leaderboardThirdPoints.text = jsonArray.getJSONObject(2).getString("UserScore")

                jsonArray.remove(0)
                jsonArray.remove(0)
                jsonArray.remove(0)

                lv.adapter = ListExampleAdapter(applicationContext, jsonArray, currentUser!!)
                list.setSelection(pos)
            }
        })
    }

    private class ListExampleAdapter(context: Context, jsonArray: JSONArray, currentUser: FirebaseUser) : BaseAdapter() {
        private val mInflator: LayoutInflater
        private val jsonArray: JSONArray
        private val currentUser = currentUser
        private val context = context

        init {
            this.mInflator = LayoutInflater.from(context)
            this.jsonArray = jsonArray
        }

        override fun getCount(): Int {
            Log.d("Leaderboard", "count: " + jsonArray.length())
            return jsonArray.length()
        }

        override fun getItem(position: Int): Any {
            Log.d("Leaderboard", "count: " + jsonArray.getJSONObject(position))
            return jsonArray.getJSONObject(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view: View?
            val vh: ListRowHolder
            if (convertView == null) {
                view = this.mInflator.inflate(R.layout.list_row, parent, false)
                vh = ListRowHolder(view)
                view.tag = vh
            } else {
                view = convertView
                vh = view.tag as ListRowHolder
            }

            vh.leaderboardItemPosition.text = Integer.toString(position + 4) + "#"
            vh.leaderboardItemName.text = jsonArray.getJSONObject(position).getString("UserName")
            vh.leaderboardItemScore.text = jsonArray.getJSONObject(position).getLong("UserScore").toString()

            return view
        }
    }

    private class ListRowHolder(row: View?) {
        val leaderboardItemPosition: TextView
        val leaderboardItemName: TextView
        val leaderboardItemScore: TextView
        val leaderboardItemLN: LinearLayout

        init {
            this.leaderboardItemPosition = row?.findViewById(R.id.leaderboardItemPosition)!!
            this.leaderboardItemName = row?.findViewById(R.id.leaderboardItemName)!!
            this.leaderboardItemScore = row?.findViewById(R.id.leaderboardItemScore)!!
            this.leaderboardItemLN = row?.findViewById(R.id.listViewLN)!!
        }
    }
}
