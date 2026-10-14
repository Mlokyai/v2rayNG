package com.v2ray.ang.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

object ShizziHelper {
    private var isTethering = false

    fun attachFloatingButton(activity: Activity) {
        activity.window.decorView.post {
            val rootGroup = activity.findViewById<ViewGroup>(android.R.id.content) ?: return@post
            val density = activity.resources.displayMetrics.density
            val size = (56 * density).toInt()

            val shizziBtn = ImageButton(activity).apply {
                setImageResource(android.R.drawable.ic_menu_share)
                setColorFilter(Color.WHITE)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#1E88E5"))
                }
                elevation = 12 * density
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    gravity = Gravity.BOTTOM or Gravity.START
                    marginStart = (24 * density).toInt()
                    bottomMargin = (100 * density).toInt()
                }
                setOnClickListener {
                    try {
                        if (!Shizuku.pingBinder()) {
                            Toast.makeText(activity, "Run Shizuku first", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                            Shizuku.requestPermission(1001)
                            Toast.makeText(activity, "Grant Shizuku permission", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val method = Shizuku::class.java.getDeclaredMethod(
                            "newProcess",
                            Array<String>::class.java,
                            Array<String>::class.java,
                            String::class.java
                        )
                        method.isAccessible = true
                        val process = method.invoke(null, arrayOf("sh", "-c", "cmd connectivity start-tethering wifi"), null, null) as ShizukuRemoteProcess
                        process.waitFor()
                        Toast.makeText(activity, "Shizzi Hotspot: ON", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            rootGroup.addView(shizziBtn)
        }
    }
}
