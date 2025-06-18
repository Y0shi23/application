package com.tango.application.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tango.application.MainActivity
import com.tango.application.data.LoginRequest
import com.tango.application.databinding.ActivityLoginBinding
import com.tango.application.network.NetworkClient
import com.tango.application.utils.PreferenceManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        // 既にログインしている場合はメイン画面へ
        if (preferenceManager.isLoggedIn()) {
            navigateToMain()
            return
        }
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }
        
        binding.tvRegisterLink.setOnClickListener {
            // TODO: 登録画面への遷移を実装
            Toast.makeText(this, "新規登録機能は後で実装します", Toast.LENGTH_SHORT).show()
        }
        
        binding.tvForgotPassword.setOnClickListener {
            // TODO: パスワードリセット機能を実装
            Toast.makeText(this, "パスワードリセット機能は後で実装します", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun performLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        // バリデーション
        if (username.isEmpty()) {
            binding.tilUsername.error = "ユーザー名を入力してください"
            binding.etUsername.requestFocus()
            return
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.error = "パスワードを入力してください"
            binding.etPassword.requestFocus()
            return
        }
        
        if (password.length < 6) {
            binding.tilPassword.error = "パスワードは6文字以上で入力してください"
            binding.etPassword.requestFocus()
            return
        }
        
        // エラーメッセージをクリア
        binding.tilUsername.error = null
        binding.tilPassword.error = null
        
        // ローディング表示
        showLoading(true)
        
        // ログイン処理
        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(username, password)
                val response = NetworkClient.apiService.login(loginRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    
                    // 認証情報を保存
                    preferenceManager.saveAuthData(
                        token = authResponse.token,
                        userId = authResponse.user.id,
                        username = authResponse.user.username,
                        email = authResponse.user.email
                    )
                    
                    showLoading(false)
                    
                    // 成功メッセージを表示
                    Toast.makeText(this@LoginActivity, "ログインに成功しました", Toast.LENGTH_SHORT).show()
                    
                    navigateToMain()
                    
                } else {
                    showLoading(false)
                    val errorMessage = when (response.code()) {
                        401 -> "ユーザー名またはパスワードが間違っています"
                        404 -> "ユーザーが見つかりません"
                        500 -> "サーバーエラーが発生しました"
                        else -> "ログインに失敗しました (${response.code()})"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                showLoading(false)
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "ネットワークに接続できません。接続を確認してください。"
                    e.message?.contains("timeout") == true -> 
                        "接続がタイムアウトしました。再度お試しください。"
                    else -> "ネットワークエラーが発生しました: ${e.message}"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etUsername.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.tvRegisterLink.isEnabled = !isLoading
        binding.tvForgotPassword.isEnabled = !isLoading
        
        // ローディング中はボタンのテキストを変更
        binding.btnLogin.text = if (isLoading) "ログイン中..." else "ログイン"
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 