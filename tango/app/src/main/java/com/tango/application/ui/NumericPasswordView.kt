package com.tango.application.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tango.application.R

class NumericPasswordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tvErrorMessage: TextView

    private var onPasswordChangedListener: ((String, Boolean) -> Unit)? = null

    init {
        orientation = VERTICAL
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val primaryColor = Color.parseColor("#6200EE")
        val primaryColorStateList = ColorStateList.valueOf(primaryColor)
        
        // パスワード入力フィールド
        val passwordLabel = TextView(context).apply {
            text = "数字パスワード（PIN）"
            textSize = 14f
            setTextColor(Color.parseColor("#333333"))
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
        }
        addView(passwordLabel)

        tilPassword = TextInputLayout(context, null, com.google.android.material.R.attr.textInputOutlinedStyle).apply {
            hint = "4桁の数字を入力"
            setBoxStrokeColorStateList(primaryColorStateList)
            setHintTextColor(ColorStateList.valueOf(Color.parseColor("#666666")))
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
        }
        
        etPassword = TextInputEditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            maxLines = 1
            textSize = 16f
        }
        
        tilPassword.addView(etPassword)
        addView(tilPassword)

        // 確認パスワード入力フィールド
        val confirmPasswordLabel = TextView(context).apply {
            text = "パスワード確認"
            textSize = 14f
            setTextColor(Color.parseColor("#333333"))
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
                bottomMargin = dpToPx(8)
            }
        }
        addView(confirmPasswordLabel)

        tilConfirmPassword = TextInputLayout(context, null, com.google.android.material.R.attr.textInputOutlinedStyle).apply {
            hint = "再度4桁の数字を入力"
            setBoxStrokeColorStateList(primaryColorStateList)
            setHintTextColor(ColorStateList.valueOf(Color.parseColor("#666666")))
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        }
        
        etConfirmPassword = TextInputEditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            maxLines = 1
            textSize = 16f
        }
        
        tilConfirmPassword.addView(etConfirmPassword)
        addView(tilConfirmPassword)

        // エラーメッセージ
        tvErrorMessage = TextView(context).apply {
            text = ""
            textSize = 14f
            setTextColor(Color.RED)
            visibility = GONE
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
        }
        addView(tvErrorMessage)
    }

    private fun setupListeners() {
        // パスワードフィールド用のTextWatcher
        val passwordTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                android.util.Log.d("NumericPassword", "パスワードフィールドが変更されました: ${s.toString()}")
                post { validatePasswords() }
            }
        }

        // 確認パスワードフィールド用のTextWatcher
        val confirmPasswordTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                android.util.Log.d("NumericPassword", "確認パスワードフィールドが変更されました: ${s.toString()}")
                post { validatePasswords() }
            }
        }

        etPassword.addTextChangedListener(passwordTextWatcher)
        etConfirmPassword.addTextChangedListener(confirmPasswordTextWatcher)
    }

    private fun validatePasswords() {
        val password = etPassword.text?.toString() ?: ""
        val confirmPassword = etConfirmPassword.text?.toString() ?: ""
        
        android.util.Log.d("NumericPassword", "=== validatePasswords START ===")
        android.util.Log.d("NumericPassword", "password='$password' (length=${password.length})")
        android.util.Log.d("NumericPassword", "confirmPassword='$confirmPassword' (length=${confirmPassword.length})")
        
        // 確認パスワードが入力されている場合のリアルタイムチェック
        if (confirmPassword.isNotEmpty()) {
            android.util.Log.d("NumericPassword", "確認パスワードが入力されています")
            
            // パスワードが4桁で、確認パスワードと完全に一致している場合のみ成功
            if (password.length == 4 && password == confirmPassword) {
                android.util.Log.d("NumericPassword", "✓ パスワードが一致しました")
                hideError()
                onPasswordChangedListener?.invoke(password, true)
            } else {
                // それ以外の場合（不一致、桁数違い等）は「パスワードが一致しません」を表示
                android.util.Log.d("NumericPassword", "✗ パスワードが一致しません")
                showError("パスワードが一致しません")
                onPasswordChangedListener?.invoke(password, false)
            }
            android.util.Log.d("NumericPassword", "=== validatePasswords END (confirm not empty) ===")
            return
        }
        
        // 確認パスワードが空の場合の最初のパスワードフィールドの長さチェック
        if (password.isNotEmpty() && password.length < 4) {
            android.util.Log.d("NumericPassword", "パスワードが4桁未満です")
            showError("パスワードは4桁で入力してください")
            onPasswordChangedListener?.invoke(password, false)
            android.util.Log.d("NumericPassword", "=== validatePasswords END (password too short) ===")
            return
        }
        
        // どちらかのフィールドが空の場合はエラーを非表示
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            android.util.Log.d("NumericPassword", "フィールドが空です")
            hideError()
        }
        
        onPasswordChangedListener?.invoke(password, false)
        android.util.Log.d("NumericPassword", "=== validatePasswords END (default) ===")
    }

    private fun showError(message: String) {
        android.util.Log.d("NumericPassword", "showError: '$message'")
        
        post {
            try {
                tvErrorMessage.text = message
                tvErrorMessage.visibility = VISIBLE
                
                val redColorStateList = ColorStateList.valueOf(Color.RED)
                tilConfirmPassword.setBoxStrokeColorStateList(redColorStateList)
                tilPassword.setBoxStrokeColorStateList(redColorStateList)
                
                android.util.Log.d("NumericPassword", "✓ エラーメッセージを表示しました")
            } catch (e: Exception) {
                android.util.Log.e("NumericPassword", "showError failed", e)
            }
        }
    }

    private fun hideError() {
        android.util.Log.d("NumericPassword", "hideError called")
        
        post {
            try {
                tvErrorMessage.visibility = GONE
                
                val primaryColorStateList = ColorStateList.valueOf(Color.parseColor("#6200EE"))
                tilConfirmPassword.setBoxStrokeColorStateList(primaryColorStateList)
                tilPassword.setBoxStrokeColorStateList(primaryColorStateList)
                
                android.util.Log.d("NumericPassword", "✓ エラーメッセージを非表示にしました")
            } catch (e: Exception) {
                android.util.Log.e("NumericPassword", "hideError failed", e)
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    // 外部からバリデーションを実行
    fun validatePassword(): Boolean {
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        
        when {
            password.isEmpty() -> {
                showError("パスワードを入力してください")
                etPassword.requestFocus()
                return false
            }
            password.length != 4 -> {
                showError("パスワードは4桁で入力してください")
                etPassword.requestFocus()
                return false
            }
            confirmPassword.isEmpty() -> {
                showError("確認パスワードを入力してください")
                etConfirmPassword.requestFocus()
                return false
            }
            password != confirmPassword -> {
                showError("パスワードが一致しません")
                etConfirmPassword.requestFocus()
                return false
            }
        }
        
        hideError()
        return true
    }

    // パスワードを取得
    fun getPassword(): String {
        return etPassword.text.toString()
    }

    // パスワード変更リスナーを設定
    fun setOnPasswordChangedListener(listener: (String, Boolean) -> Unit) {
        onPasswordChangedListener = listener
    }

    // エラーをクリア
    fun clearErrors() {
        hideError()
    }

    // 入力をクリア
    fun clearInputs() {
        etPassword.text?.clear()
        etConfirmPassword.text?.clear()
        hideError()
    }
} 