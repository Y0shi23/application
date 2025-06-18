package com.tango.application.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tango.application.MainActivity
import com.tango.application.data.RegisterRequest
import com.tango.application.databinding.ActivityRegisterBinding
import com.tango.application.network.NetworkClient
import com.tango.application.utils.PreferenceManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
        binding.btnRegister.setOnClickListener {
            performRegister()
        }
        
        binding.tvLoginLink.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun performRegister() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val isTermsAccepted = binding.cbTerms.isChecked
        
        // バリデーション
        if (!validateInputs(username, email, password, confirmPassword, isTermsAccepted)) {
            return
        }
        
        // エラーメッセージをクリア
        clearErrors()
        
        // ローディング表示
        showLoading(true)
        
        // 登録処理
        lifecycleScope.launch {
            try {
                // 接続先URLをログに出力
                android.util.Log.d("TangoRegister", "接続先URL: ${NetworkClient.getCurrentBaseUrl()}")
                
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
                    Toast.makeText(this@RegisterActivity, "アカウントが作成されました", Toast.LENGTH_SHORT).show()
                    
                    navigateToMain()
                    
                } else {
                    showLoading(false)
                    val errorMessage = when (response.code()) {
                        400 -> "入力データに問題があります。確認してください。"
                        409 -> "このユーザー名またはメールアドレスは既に使用されています"
                        500 -> "サーバーエラーが発生しました"
                        else -> "アカウント作成に失敗しました (${response.code()})"
                    }
                    android.util.Log.e("TangoRegister", "登録失敗: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                showLoading(false)
                android.util.Log.e("TangoRegister", "登録エラー", e)
                
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "サーバーに接続できません。ネットワーク接続とサーバーURLを確認してください。\n接続先: ${NetworkClient.getCurrentBaseUrl()}"
                    e.message?.contains("timeout") == true -> 
                        "接続がタイムアウトしました。サーバーが起動しているか確認してください。"
                    e.message?.contains("Connection refused") == true -> 
                        "サーバーが応答しません。サーバーが起動しているか確認してください。"
                    e.message?.contains("No address associated with hostname") == true -> 
                        "ホスト名を解決できません。サーバーURLを確認してください。"
                    else -> "ネットワークエラーが発生しました: ${e.message}"
                }
                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String, isTermsAccepted: Boolean): Boolean {
        var isValid = true
        
        // ユーザー名のバリデーション
        if (username.isEmpty()) {
            binding.tilUsername.error = "ユーザー名を入力してください"
            binding.etUsername.requestFocus()
            isValid = false
        } else if (username.length < 3) {
            binding.tilUsername.error = "ユーザー名は3文字以上で入力してください"
            binding.etUsername.requestFocus()
            isValid = false
        } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            binding.tilUsername.error = "ユーザー名は英数字とアンダースコアのみ使用できます"
            binding.etUsername.requestFocus()
            isValid = false
        }
        
        // メールアドレスのバリデーション
        if (email.isEmpty()) {
            binding.tilEmail.error = "メールアドレスを入力してください"
            if (isValid) binding.etEmail.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "正しいメールアドレス形式で入力してください"
            if (isValid) binding.etEmail.requestFocus()
            isValid = false
        }
        
        // パスワードのバリデーション
        if (password.isEmpty()) {
            binding.tilPassword.error = "パスワードを入力してください"
            if (isValid) binding.etPassword.requestFocus()
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "パスワードは6文字以上で入力してください"
            if (isValid) binding.etPassword.requestFocus()
            isValid = false
        } else if (!password.matches(Regex(".*[A-Za-z].*")) || !password.matches(Regex(".*[0-9].*"))) {
            binding.tilPassword.error = "パスワードは英数字を含む必要があります"
            if (isValid) binding.etPassword.requestFocus()
            isValid = false
        }
        
        // パスワード確認のバリデーション
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "パスワード確認を入力してください"
            if (isValid) binding.etConfirmPassword.requestFocus()
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "パスワードが一致しません"
            if (isValid) binding.etConfirmPassword.requestFocus()
            isValid = false
        }
        
        // 利用規約同意のバリデーション
        if (!isTermsAccepted) {
            Toast.makeText(this, "利用規約とプライバシーポリシーに同意してください", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        return isValid
    }
    
    private fun clearErrors() {
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.etUsername.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
        binding.cbTerms.isEnabled = !isLoading
        binding.tvLoginLink.isEnabled = !isLoading
        
        // ローディング中はボタンのテキストを変更
        binding.btnRegister.text = if (isLoading) "アカウント作成中..." else "アカウント作成"
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 