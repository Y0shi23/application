package com.tango.application.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tango.application.databinding.ActivityNumericPasswordBinding

class NumericPasswordActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNumericPasswordBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNumericPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNumericPasswordView()
        setupSubmitButton()
    }
    
    private fun setupNumericPasswordView() {
        binding.numericPasswordView.setOnPasswordChangedListener { password, isValid ->
            binding.btnSubmit.isEnabled = isValid
        }
    }
    
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (binding.numericPasswordView.validatePassword()) {
                val password = binding.numericPasswordView.getPassword()
                Toast.makeText(this, "パスワードが設定されました: $password", Toast.LENGTH_SHORT).show()
                // ここで実際のパスワード設定処理を行う
            }
        }
    }
} 