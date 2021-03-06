package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.preference.PreferenceManager
import cn.leancloud.AVUser
import com.google.android.material.snackbar.Snackbar
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivityMainBinding
import xyz.xfqlittlefan.easytest.widget.BlurBehindDialogBuilder
import okhttp3.*
import org.litepal.LitePal
import java.io.IOException
import java.net.URL

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentUser: AVUser? = null

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createDb()

        setAppBar(binding.appbar, binding.toolbar)
        binding.scroll.borderViewDelegate.setBorderVisibilityChangedListener { top, _, _, _ ->
            binding.appbar.isRaised = !top
        }

        binding.card1.setOnClickListener {
            Intent(this, SelectQuestionBankActivity::class.java).apply {
                val urlList = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    .getString("custom_source", "")!!
                    .split("\n").toMutableList()
                urlList.add("https://xfqwdsj.gitee.io/easy-test/question-bank-index.json")
                putStringArrayListExtra("urlList", ArrayList(urlList))
                startActivity(this)
            }
        }
        binding.card2.setOnClickListener {
            Snackbar.make(binding.root, R.string.coming_soon, Snackbar.LENGTH_SHORT).show()
        }
        binding.card3.setOnClickListener {
            if (currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Snackbar.make(binding.root, R.string.coming_soon, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.logout) {
                        AVUser.logOut()
                        onResume()
                    }
                    .show()
            }
        }
        binding.card4.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        /*
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navigationView.addHeaderView(headerBinding.root)
        headerBinding.toolbar.inflateMenu(R.menu.drawer_menu)
        headerBinding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.account -> {
                    if (currentUser == null) {
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {

                    }
                    return@setOnMenuItemClickListener true
                }
                R.id.logout -> {
                    AVUser.logOut()
                    onResume()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.test -> Intent(
                    this,
                    SelectQuestionBankActivity::class.java
                ).apply {
                    val urlList =
                        PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                            .getString("custom_source", "")!!
                            .split("\n").toMutableList()
                    urlList.add("https://xfqwdsj.gitee.io/easy-test/question-bank-index.json")
                    putStringArrayListExtra("urlList", ArrayList(urlList))
                    startActivity(this)
                }
                R.id.query -> startActivity(Intent(this, IDQueryActivity::class.java))
            }
            false
        }
        //setInset(MyClass.INSET_BOTTOM, binding.navigationView)
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.expanded,
            R.string.collapsed
        )
        actionBarDrawerToggle.syncState()
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
         */
        try {
            getNetworkStatus(URL("https://xfqwdsj.gitee.io/easy-test/"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return false
    }
     */

    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        currentUser = AVUser.getCurrentUser()
        if (currentUser != null) {
            binding.text32.setText(R.string.account_summary)
        } else {
            binding.text32.setText(R.string.account_summary_no_logged_in)
        }
    }

    private fun getNetworkStatus(url: URL) {
        val dialog = BlurBehindDialogBuilder(this)
            .setTitle(R.string.network_getting)
            .setMessage(R.string.please_wait)
            .setCancelable(false)
            .show()
        get(url, dialog)
    }

    private fun createDb() {
        LitePal.getDatabase()
    }

    private fun get(url: URL, dialog: Dialog) {
        val request = Request.Builder()
            .url(url)
            .removeHeader("User-Agent")
            .addHeader("User-Agent", WebView(this).settings.userAgentString)
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    dialog.cancel()
                    BlurBehindDialogBuilder(this@MainActivity)
                        .setTitle(R.string.failed)
                        .setMessage(R.string.about_to_exit)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                dialog.cancel()
                if (response.code != 200) {
                    runOnUiThread {
                        BlurBehindDialogBuilder(this@MainActivity)
                            .setTitle(R.string.failed)
                            .setMessage(R.string.about_to_exit)
                            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                                finish()
                            }
                            .setCancelable(false)
                            .show()
                    }
                }
                response.close()
            }
        })
    }
}