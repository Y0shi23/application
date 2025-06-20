package com.tango.application

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.tango.application.databinding.ActivityMainBinding
import com.tango.application.databinding.HistoryItemLayoutBinding
import com.tango.application.ui.LoginActivity
import com.tango.application.utils.PreferenceManager

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var drawerLayout: DrawerLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        setupToolbar()
        setupNavigationDrawer()
        setupUI()
        loadLearningHistory()
        updateLearningStats()
        setupMenuClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "WordMaster"
    }
    
    private fun setupNavigationDrawer() {
        drawerLayout = binding.drawerLayout
        val navigationView = binding.navView
        
        // DrawerToggleを設定
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // NavigationViewのリスナーを設定
        navigationView.setNavigationItemSelectedListener(this)
        
        // ヘッダーのユーザー名を設定
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById<TextView>(R.id.nav_username)
        navUsername.text = preferenceManager.getUsername()
    }
    
    private fun setupUI() {
        val username = preferenceManager.getUsername()
        binding.tvUsername.text = "こんにちは、$username"
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                Toast.makeText(this, "ホーム", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_study -> {
                Toast.makeText(this, "学習メニュー", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_history -> {
                Toast.makeText(this, "学習履歴", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_statistics -> {
                Toast.makeText(this, "統計・分析", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "設定", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_help -> {
                Toast.makeText(this, "ヘルプ", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about -> {
                showAboutDialog()
            }
            R.id.nav_profile -> {
                showProfileDialog()
            }
            R.id.nav_logout -> {
                performLogout()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("WordMasterについて")
            .setMessage("WordMaster v1.0\n\n英単語学習アプリ\n効率的な単語学習をサポートします。")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showProfileDialog() {
        val username = preferenceManager.getUsername()
        
        AlertDialog.Builder(this)
            .setTitle("プロフィール")
            .setMessage("ユーザー名: $username\nレベル: 中級者\n学習日数: 23日\n学習単語数: 1,247語")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun performLogout() {
        AlertDialog.Builder(this)
            .setTitle("ログアウト確認")
            .setMessage("本当にログアウトしますか？")
            .setPositiveButton("はい") { _, _ ->
                preferenceManager.clearAuthData()
                navigateToLogin()
            }
            .setNegativeButton("いいえ", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun loadLearningHistory() {
        val historyData = listOf(
            Triple("新規単語学習", "15単語学習 (正答率: 87%)", "2時間前"),
            Triple("復習", "8単語復習 (正答率: 92%)", "昨日 18:30"),
            Triple("弱点強化", "12単語練習 (正答率: 78%)", "昨日 16:15"),
            Triple("確認テスト", "20問テスト (正答率: 85%)", "2日前")
        )

        val historyContainer = binding.layoutHistoryContainer
        historyContainer.removeAllViews()

        historyData.take(4).forEach { (title, description, time) ->
            val historyItemView = createHistoryItemView(title, description, time)
            historyContainer.addView(historyItemView.root)
        }
    }

    private fun createHistoryItemView(title: String, description: String, time: String): HistoryItemLayoutBinding {
        val historyItemBinding = HistoryItemLayoutBinding.inflate(layoutInflater)

        // アイコンを学習タイプに応じて設定
        historyItemBinding.historyIcon.text = when (title) {
            "新規単語学習" -> "📚"
            "復習" -> "🔄"
            "弱点強化" -> "💪"
            "確認テスト" -> "📝"
            else -> "📖"
        }

        historyItemBinding.historyTitle.text = title
        historyItemBinding.historyDescription.text = description
        historyItemBinding.historyTime.text = time

        return historyItemBinding
    }

    private fun updateLearningStats() {
        binding.tvLearnedWords.text = "1,247"
        binding.tvAverageAccuracy.text = "89%"
        binding.tvConsecutiveDays.text = "23日"
        binding.tvMonthlyHours.text = "42時間"
    }

    private fun setupMenuClickListeners() {
        binding.layoutNewWords.setOnClickListener {
            Toast.makeText(this, "新規単語学習を開始します", Toast.LENGTH_SHORT).show()
        }

        binding.layoutReview.setOnClickListener {
            Toast.makeText(this, "復習を開始します", Toast.LENGTH_SHORT).show()
        }

        binding.layoutWeakness.setOnClickListener {
            Toast.makeText(this, "弱点強化を開始します", Toast.LENGTH_SHORT).show()
        }

        binding.layoutTest.setOnClickListener {
            Toast.makeText(this, "確認テストを開始します", Toast.LENGTH_SHORT).show()
        }

        binding.tvViewAllHistory.setOnClickListener {
            Toast.makeText(this, "すべての学習履歴を表示します", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}