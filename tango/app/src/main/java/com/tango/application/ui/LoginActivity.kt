package com.tango.application.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tango.application.MainActivity
import com.tango.application.data.LoginRequest
import com.tango.application.data.RegisterRequest
import com.tango.application.databinding.ActivityLoginBinding
import com.tango.application.network.NetworkClient
import com.tango.application.utils.PreferenceManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager
    private var isLoginMode = true
    
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
        updateUIForMode()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }
        
        binding.tvToggleLink.setOnClickListener {
            toggleMode()
        }
        
        binding.tvForgotPassword.setOnClickListener {
            // TODO: パスワードリセット機能を実装
            Toast.makeText(this, "パスワードリセット機能は後で実装します", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleMode() {
        isLoginMode = !isLoginMode
        updateUIForMode()
        hideError()
        clearInputs()
    }
    
    private fun updateUIForMode() {
        if (isLoginMode) {
            // ログインモード
            binding.tvFormTitle?.text = "ログイン"
            binding.btnLogin.text = "ログイン"
            binding.tvToggleText?.text = "アカウントをお持ちでない方は"
            binding.tvToggleLink?.text = "新規登録"
            
            // 登録用フィールドを非表示
            binding.tilEmail?.visibility = View.GONE
            binding.tilConfirmPassword?.visibility = View.GONE
            binding.layoutLoginOptions?.visibility = View.VISIBLE
            
        } else {
            // 登録モード
            binding.tvFormTitle?.text = "新規登録"
            binding.btnLogin?.text = "アカウント作成"
            binding.tvToggleText?.text = "既にアカウントをお持ちの方は"
            binding.tvToggleLink?.text = "ログイン"
            
            // 登録用フィールドを表示
            binding.tilEmail?.visibility = View.VISIBLE
            binding.tilConfirmPassword?.visibility = View.VISIBLE
            binding.layoutLoginOptions?.visibility = View.GONE
        }
    }
    
    private fun clearInputs() {
        binding.etUsername?.setText("")
        binding.etEmail?.setText("")
        binding.etPassword?.setText("")
        binding.etConfirmPassword?.setText("")
        binding.tilUsername?.error = null
        binding.tilEmail?.error = null
        binding.tilPassword?.error = null
        binding.tilConfirmPassword?.error = null
    }
    
    private fun showError(message: String) {
        binding.tvError?.setText(message)
        binding.errorCard?.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        binding.errorCard?.visibility = View.GONE
    }
    
    private fun performLogin() {
        val username = binding.etUsername?.text?.toString()?.trim() ?: ""
        val password = binding.etPassword?.text?.toString()?.trim() ?: ""
        
        // バリデーション
        if (username.isEmpty()) {
            binding.tilUsername?.error = "ユーザー名を入力してください"
            binding.etUsername?.requestFocus()
            return
        }
        
        if (password.isEmpty()) {
            binding.tilPassword?.error = "パスワードを入力してください"
            binding.etPassword?.requestFocus()
            return
        }
        
        if (password.length < 6) {
            binding.tilPassword?.error = "パスワードは6文字以上で入力してください"
            binding.etPassword?.requestFocus()
            return
        }
        
        // エラーメッセージをクリア
        binding.tilUsername?.error = null
        binding.tilPassword?.error = null
        hideError()
        
        // ローディング表示
        showLoading(true)
        
        // ログイン処理
        lifecycleScope.launch {
            try {
                // 接続先URLをログに出力
                android.util.Log.d("TangoLogin", "接続先URL: ${NetworkClient.getCurrentBaseUrl()}")
                
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
                    android.util.Log.e("TangoLogin", "ログイン失敗: ${response.code()} - ${response.message()}")
                    showError(errorMessage)
                }
                
            } catch (e: Exception) {
                showLoading(false)
                android.util.Log.e("TangoLogin", "ログインエラー", e)
                
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "サーバーに接続できません。ネットワーク接続とサーバーURLを確認してください。"
                    e.message?.contains("timeout") == true -> 
                        "接続がタイムアウトしました。サーバーが起動しているか確認してください。"
                    e.message?.contains("Connection refused") == true -> 
                        "サーバーが応答しません。サーバーが起動しているか確認してください。"
                    e.message?.contains("No address associated with hostname") == true -> 
                        "ホスト名を解決できません。サーバーURLを確認してください。"
                    else -> "ネットワークエラーが発生しました: ${e.message}"
                }
                showError(errorMessage)
            }
        }
    }
    
    private fun performRegister() {
        val username = binding.etUsername?.text?.toString()?.trim() ?: ""
        val email = binding.etEmail?.text?.toString()?.trim() ?: ""
        val password = binding.etPassword?.text?.toString()?.trim() ?: ""
        val confirmPassword = binding.etConfirmPassword?.text?.toString()?.trim() ?: ""
        
        // バリデーション
        if (username.isEmpty()) {
            binding.tilUsername?.error = "ユーザー名を入力してください"
            binding.etUsername?.requestFocus()
            return
        }
        
        if (username.length < 3) {
            binding.tilUsername?.error = "ユーザー名は3文字以上で入力してください"
            binding.etUsername?.requestFocus()
            return
        }
        
        if (email.isEmpty()) {
            binding.tilEmail?.error = "メールアドレスを入力してください"
            binding.etEmail?.requestFocus()
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail?.error = "有効なメールアドレスを入力してください"
            binding.etEmail?.requestFocus()
            return
        }
        
        if (password.isEmpty()) {
            binding.tilPassword?.error = "パスワードを入力してください"
            binding.etPassword?.requestFocus()
            return
        }
        
        if (password.length < 6) {
            binding.tilPassword?.error = "パスワードは6文字以上で入力してください"
            binding.etPassword?.requestFocus()
            return
        }
        
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword?.error = "パスワード確認を入力してください"
            binding.etConfirmPassword?.requestFocus()
            return
        }
        
        if (password != confirmPassword) {
            binding.tilConfirmPassword?.error = "パスワードが一致しません"
            binding.etConfirmPassword?.requestFocus()
            return
        }
        
        // エラーメッセージをクリア
        binding.tilUsername?.error = null
        binding.tilEmail?.error = null
        binding.tilPassword?.error = null
        binding.tilConfirmPassword?.error = null
        hideError()
        
        // ローディング表示
        showLoading(true)
        
        // 登録処理
        lifecycleScope.launch {
            try {
                val registerRequest = RegisterRequest(username, email, password)
                val response = NetworkClient.apiService.register(registerRequest)
                
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
                    Toast.makeText(this@LoginActivity, "アカウントが作成されました", Toast.LENGTH_SHORT).show()
                    
                    navigateToMain()
                    
                } else {
                    showLoading(false)
                    val errorMessage = when (response.code()) {
                        409 -> "このユーザー名またはメールアドレスは既に使用されています"
                        400 -> "入力内容に問題があります"
                        500 -> "サーバーエラーが発生しました"
                        else -> "アカウント作成に失敗しました (${response.code()})"
                    }
                    android.util.Log.e("TangoRegister", "登録失敗: ${response.code()} - ${response.message()}")
                    showError(errorMessage)
                }
                
            } catch (e: Exception) {
                showLoading(false)
                android.util.Log.e("TangoRegister", "登録エラー", e)
                
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "サーバーに接続できません。ネットワーク接続とサーバーURLを確認してください。"
                    e.message?.contains("timeout") == true -> 
                        "接続がタイムアウトしました。サーバーが起動しているか確認してください。"
                    e.message?.contains("Connection refused") == true -> 
                        "サーバーが応答しません。サーバーが起動しているか確認してください。"
                    else -> "ネットワークエラーが発生しました: ${e.message}"
                }
                showError(errorMessage)
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin?.isEnabled = !isLoading
        binding.etUsername?.isEnabled = !isLoading
        binding.etEmail?.isEnabled = !isLoading
        binding.etPassword?.isEnabled = !isLoading
        binding.etConfirmPassword?.isEnabled = !isLoading
        binding.tvToggleLink?.isEnabled = !isLoading
        binding.tvForgotPassword?.isEnabled = !isLoading
        
        // ローディング中はボタンのテキストを変更
        binding.btnLogin?.text = if (isLoading) {
            if (isLoginMode) "ログイン中..." else "作成中..."
        } else {
            if (isLoginMode) "ログイン" else "アカウント作成"
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 