package com.example.s87sun.synopsis

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.os.Build
import android.view.*
import android.widget.*

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var pop_view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val dollPane = DollPane(this)
        findViewById<LinearLayout>(R.id.container).addView(dollPane)
        val toolbar: LinearLayout = findViewById(R.id.toolbar)
        toolbar.findViewById<ImageButton>(R.id.reset_tool).setOnClickListener { dollPane.reset() }
        toolbar.findViewById<ImageButton>(R.id.about_popup).setOnClickListener {
            val inflater:LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            // Inflate a custom view using layout inflater
            pop_view = inflater.inflate(R.layout.about_popup,null)
            val popupWindow = PopupWindow(pop_view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            // Set an elevation for the popup window
            popupWindow.elevation = 10.0F

            val close_button = pop_view.findViewById<ImageButton>(R.id.close_popup)
            close_button.setOnClickListener {
                popupWindow.dismiss()
            }
//            popupWindow.showAsDropDown(toolbar.findViewById<ImageButton>(R.id.reset_tool),20,90)
            popupWindow.showAtLocation(findViewById<LinearLayout>(R.id.container), Gravity.CENTER, 0, 0)
        }
    }
}
