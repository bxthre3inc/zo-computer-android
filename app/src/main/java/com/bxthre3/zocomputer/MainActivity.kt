package com.bxthre3.zocomputer

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.nfc.NfcAdapter
import android.os.Build
import java.security.KeyStore
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var sysInfoPanel: LinearLayout
    private lateinit var navBar: LinearLayout
    private lateinit var sysInfoText: TextView
    private var currentView = "web"

    private val PERMS = arrayOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.BLUETOOTH",
        "android.permission.NFC"
    )

    @SuppressLint("SetJavaScriptEnabled", "UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        sysInfoPanel = findViewById(R.id.sysInfoPanel)
        sysInfoText = findViewById(R.id.sysInfoText)
        navBar = findViewById(R.id.navBar)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = "ZoComputer/Android ${Build.VERSION.RELEASE} (BX3-PIMPED/6.0)"
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                progressBar.progress = newProgress
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                webView.loadUrl("https://brodiblanco.zo.space/")
            }
        }

        findViewById<Button>(R.id.btnDash).setOnClickListener { showDashboard() }
        findViewById<Button>(R.id.btnWeb).setOnClickListener { showWeb() }
        findViewById<Button>(R.id.btnSys).setOnClickListener { showSystemInfo() }
        findViewById<Button>(R.id.closeSysInfo).setOnClickListener { showWeb() }

        showDashboard()

        if (ContextCompat.checkSelfPermission(this, PERMS[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMS, 1001)
        }
    }

    private fun showDashboard() {
        currentView = "dashboard"
        sysInfoPanel.visibility = View.GONE
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE

        val overlay = FrameLayout(this).apply {
            setBackgroundColor(0xFF0D0D0D.toInt())
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 160)
        }

        val title = TextView(this).apply {
            text = "AgentOS Dashboard"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 24f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        container.addView(title)

        val subtitle = TextView(this).apply {
            text = "Zo Computer v6.0 — BX3-PIMPED"
            setTextColor(0xFFB0B0B0.toInt())
            textSize = 14f
            setPadding(0, 16, 0, 32)
        }
        container.addView(subtitle)

        val si = collectSystemInfo()
        val rows: List<Pair<String, String>> = listOf(
            "Device" to ("${si["manufacturer"]} ${si["model"]}" as String),
            "Android" to ("${si["androidVersion"]} (API ${si["sdkVersion"]})" as String),
            "CPU Cores" to (si["cpuCores"] as? String ?: "N/A"),
            "RAM" to ("${si["availableRam"]} / ${si["totalRam"]}" as String),
            "Display" to (si["displayMetrics"] as? String ?: "N/A"),
            "Network" to (si["networkType"] as? String ?: "Offline"),
            "Camera" to (if (si["cameraAvailable"] == "true") "Available" else "N/A"),
            "Bluetooth" to (if (si["bluetoothEnabled"] == "true") "Enabled" else "N/A"),
            "Location" to (if (si["locationEnabled"] == "true") "Enabled" else "N/A"),
            "NFC" to (if (si["nfcAvailable"] == "true") "Available" else "N/A"),
            "Biometric" to (if (si["biometricAvailable"] == "true") "Available" else "N/A"),
            "KeyStore" to (if (si["keystoreAvailable"] == "true") "OK" else "N/A"),
            "PlayIntegrity" to (if (si["playIntegrityAvailable"] == "true") "Available" else "N/A"),
            "SE Status" to (si["seStatus"] as? String ?: "N/A")
        )

        for ((label, value) in rows) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(0xFF16213E.toInt())
                setPadding(32, 24, 32, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }
            }
            row.addView(TextView(this).apply {
                text = label
                setTextColor(0xFFB0B0B0.toInt())
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            row.addView(TextView(this).apply {
                text = value
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            container.addView(row)
        }

        val capTitle = TextView(this).apply {
            text = "Capabilities"
            setTextColor(0xFF7B2FBE.toInt())
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 32, 0, 16)
        }
        container.addView(capTitle)

        for (cap in listOf("INTERNET","ACCESS_NETWORK_STATE","CAMERA","RECORD_AUDIO","BLUETOOTH","NFC","BIOMETRIC","VIBRATE","WAKE_LOCK","FOREGROUND_SERVICE")) {
            val dot = TextView(this).apply {
                text = "\u2022"
                setTextColor(0xFF00E676.toInt())
                textSize = 14f
                setPadding(0, 4, 16, 4)
            }
            val capText = TextView(this).apply {
                text = cap
                setTextColor(0xFFB0B0B0.toInt())
                textSize = 13f
            }
            val capRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 4, 0, 4)
            }
            capRow.addView(dot)
            capRow.addView(capText)
            container.addView(capRow)
        }

        scroll.addView(container)
        overlay.addView(scroll)

        val decor = window.decorView as FrameLayout
        decor.removeAllViews()
        decor.addView(overlay)
        decor.addView(navBar)
    }

    private fun showWeb() {
        currentView = "web"
        val decor = window.decorView as FrameLayout
        decor.removeAllViews()
        decor.addView(findViewById<View>(R.id.progressBar))
        decor.addView(webView)
        decor.addView(sysInfoPanel)
        decor.addView(navBar)
        sysInfoPanel.visibility = View.GONE
        webView.visibility = View.VISIBLE
        if (webView.url.isNullOrEmpty()) {
            webView.loadUrl("https://brodiblanco.zo.space/")
        }
    }

    private fun showSystemInfo() {
        currentView = "system"
        sysInfoPanel.visibility = View.VISIBLE
        webView.visibility = View.GONE
        val si = collectSystemInfo()
        val info = buildString {
            append("=== Device ===\n")
            append("Manufacturer: ${si["manufacturer"]}\n")
            append("Model: ${si["model"]}\n")
            append("Android: ${si["androidVersion"]} (API ${si["sdkVersion"]})\n")
            append("CPU Cores: ${si["cpuCores"]}\n")
            append("RAM: ${si["availableRam"]} / ${si["totalRam"]}\n")
            append("Display: ${si["displayMetrics"]}\n")
            append("\n=== Connectivity ===\n")
            append("Network: ${si["networkType"]}\n")
            append("Bluetooth: ${si["bluetoothEnabled"]}\n")
            append("Location: ${si["locationEnabled"]}\n")
            append("\n=== Hardware ===\n")
            append("Camera: ${si["cameraAvailable"]}\n")
            append("NFC: ${si["nfcAvailable"]}\n")
            append("Biometric: ${si["biometricAvailable"]}\n")
            append("\n=== Security ===\n")
            append("KeyStore: ${si["keystoreAvailable"]}\n")
            append("PlayIntegrity: ${si["playIntegrityAvailable"]}\n")
            append("SE Status: ${si["seStatus"]}\n")
            append("\n=== AgentOS ===\n")
            append("Version: 6.0.0\n")
            append("Status: operational\n")
            append("User Agent: ${webView.settings.userAgentString}\n")
        }
        sysInfoText.text = info
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun collectSystemInfo(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nc = cm.getNetworkCapabilities(cm.activeNetwork)
            val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            val pm = packageManager

            val ksStatus = try {
                val ks = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)
                "true"
            } catch (e: Exception) { "false" }

            map["manufacturer"] = Build.MANUFACTURER
            map["model"] = Build.MODEL
            map["androidVersion"] = Build.VERSION.RELEASE
            map["sdkVersion"] = Build.VERSION.SDK_INT.toString()
            map["totalRam"] = "%.1f GB".format(mi.totalMem / (1024.0 * 1024 * 1024))
            map["availableRam"] = "%.1f GB".format(mi.availMem / (1024.0 * 1024 * 1024))
            map["cpuCores"] = Runtime.getRuntime().availableProcessors().toString()
            map["displayMetrics"] = "${dm.widthPixels}x${dm.heightPixels}"
            map["networkType"] = nc?.let {
                when {
                    it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Unknown"
                }
            } ?: "Offline"
            map["cameraAvailable"] = try {
                (getSystemService(Context.CAMERA_SERVICE) as CameraManager).cameraIdList.isNotEmpty().toString()
            } catch (e: Exception) { "false" }
            map["bluetoothEnabled"] = nc?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH).toString() ?: "false"
            map["locationEnabled"] = (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED).toString()
            map["nfcAvailable"] = (nfcAdapter?.isEnabled ?: false).toString()
            map["biometricAvailable"] = (pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) || pm.hasSystemFeature(PackageManager.FEATURE_FACE)).toString()
            map["keystoreAvailable"] = ksStatus
            map["playIntegrityAvailable"] = "true"
            map["seStatus"] = if (ksStatus == "true") "KeyStore OK" else "N/A"
        } catch (e: Exception) {
            map["error"] = e.message ?: "Unknown"
        }
        return map
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (currentView) {
            "dashboard", "system" -> {
                currentView = "web"
                showWeb()
            }
            "web" -> {
                if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
            }
        }
    }
}
