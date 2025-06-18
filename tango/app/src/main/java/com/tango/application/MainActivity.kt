package com.tango.application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tango.application.databinding.ActivityMainBinding
import com.tango.application.ui.LoginActivity
import com.tango.application.utils.PreferenceManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        // ログインチェック
        if (!preferenceManager.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        setupUI()
        setupClickListeners()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupUI() {
        // ユーザー情報を表示
        val username = preferenceManager.getUsername() ?: "ユーザー"
        binding.tvUsername.text = "${username}さん"
        
        // ツールバーの設定
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupClickListeners() {
        // ログアウトボタン
        binding.btnLogout.setOnClickListener {
            performLogout()
        }
        
        // クイックアクションカード
        binding.cardStudy.setOnClickListener {
            Toast.makeText(this, "学習機能は準備中です", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardProgress.setOnClickListener {
            Toast.makeText(this, "進捗機能は準備中です", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardSettings.setOnClickListener {
            Toast.makeText(this, "設定機能は準備中です", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardProfile.setOnClickListener {
            showProfileInfo()
        }
    }
    
    private fun showProfileInfo() {
        val username = preferenceManager.getUsername() ?: "不明"
        val email = preferenceManager.getEmail() ?: "不明"
        val userId = preferenceManager.getUserId()
        
        val message = "ユーザーID: $userId\nユーザー名: $username\nメール: $email"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun performLogout() {
        // 認証情報をクリア
        preferenceManager.clearAuthData()
        
        Toast.makeText(this, "ログアウトしました", Toast.LENGTH_SHORT).show()
        
        // ログイン画面へ遷移
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}