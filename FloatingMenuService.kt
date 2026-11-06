// FloatingMenuService.kt
package com.example.aimbotmenu

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import java.io.DataOutputStream
import java.net.Socket

class FloatingMenuService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        floatingView = LayoutInflater.from(this).inflate(R.layout.menu_layout, null)
        
        params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 0
            y = 300
            gravity = Gravity.CENTER
        }

        windowManager?.addView(floatingView, params)
        setupDragAndDrop()
        setupButtons()
    }

    private fun setupDragAndDrop() {
        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val xDiff = event.rawX - startX
                    val yDiff = event.rawY - startY
                    params?.apply {
                        x = initialX + xDiff.toInt()
                        y = initialY + yDiff.toInt()
                    }
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtons() {
        val btnToggle = floatingView?.findViewById<ImageButton>(R.id.btn_toggle)
        btnToggle?.setOnClickListener {
            toggleAimbot()
        }
    }

    private fun toggleAimbot() {
        try {
            val socket = Socket("127.0.0.1", 8080) // Si usas túnel ADB local
            val out = DataOutputStream(socket.getOutputStream())
            // Alternativa si no hay túnel local: usar adb shell directamente
            // Runtime.getRuntime().exec("adb shell input tap ...")
            
            out.writeBytes("AIM_ON\n")
            out.flush()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
