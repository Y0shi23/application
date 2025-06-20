package com.tango.application

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.tango.application.databinding.ActivityMainBinding
import com.tango.application.ui.LoginActivity
import com.tango.application.utils.PreferenceManager
import com.tango.application.data.LearningHistory
import com.tango.application.data.LearningHistoryHelper
import com.tango.application.data.LearningType

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        // ログインチェック
        if (!preferenceManager.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        setupUI()
        loadLearningHistory()
    }
    
    private fun setupUI() {
        // ユーザー名を表示
        val username = preferenceManager.getUsername() ?: "ユーザー"
        binding.tvUsername.text = "こんにちは、${username}さん！"
        
        // 学習統計を更新
        updateLearningStats()
        
        // メニューアイテムのクリックリスナーを設定
        setupMenuClickListeners()
    }
    
    private fun loadLearningHistory() {
        val histories = LearningHistoryHelper.getSampleHistory()
        
        // 最新の4件のみを表示
        val recentHistories = histories.take(4)
        
        recentHistories.forEach { history ->
            val historyView = createHistoryItemView(history)
            binding.layoutHistoryContainer.addView(historyView)
        }
        
        // "すべて表示" ボタンのクリックリスナー
        binding.tvViewAllHistory.setOnClickListener {
            Toast.makeText(this, "学習履歴の詳細画面を開発中です", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createHistoryItemView(history: LearningHistory): LinearLayout {
        val inflater = LayoutInflater.from(this)
        val historyItemView = inflater.inflate(R.layout.history_item_layout, null) as LinearLayout
        
        // アイコンの背景を設定
        val iconBackground = historyItemView.findViewById<TextView>(R.id.historyIcon)
        iconBackground.text = history.type.icon
        
        when (history.type) {
            LearningType.NEW_WORDS -> iconBackground.setBackgroundResource(R.drawable.study_icon_bg)
            LearningType.REVIEW -> iconBackground.setBackgroundResource(R.drawable.review_icon_bg)
            LearningType.TEST -> iconBackground.setBackgroundResource(R.drawable.test_icon_bg)
            LearningType.WEAKNESS_TRAINING -> iconBackground.setBackgroundResource(R.drawable.weakness_icon_bg)
        }
        
        // テキストの設定
        val titleText = historyItemView.findViewById<TextView>(R.id.historyTitle)
        val descriptionText = historyItemView.findViewById<TextView>(R.id.historyDescription)
        val timeText = historyItemView.findViewById<TextView>(R.id.historyTime)
        
        titleText.text = history.title
        descriptionText.text = history.description
        timeText.text = "${LearningHistoryHelper.formatTimeAgo(history.timestamp)} ${LearningHistoryHelper.formatTime(history.timestamp)}"
        
        // クリックリスナーを設定
        historyItemView.setOnClickListener {
            Toast.makeText(this, "${history.title}の詳細を表示", Toast.LENGTH_SHORT).show()
        }
        
        return historyItemView
    }
    
    private fun updateLearningStats() {
        // 学習した単語数
        binding.tvLearnedWords.text = "1,247"
        
        // 平均正答率
        binding.tvAverageAccuracy.text = "89%"
        
        // 連続学習日数
        binding.tvConsecutiveDays.text = "23日"
        
        // 今月の学習時間
        binding.tvMonthlyHours.text = "42時間"
    }
    
    private fun setupMenuClickListeners() {
        // 新規単語学習
        binding.layoutNewWords.setOnClickListener {
            Toast.makeText(this, "新規単語学習を開始します", Toast.LENGTH_SHORT).show()
        }
        
        // 復習
        binding.layoutReview.setOnClickListener {
            Toast.makeText(this, "復習セッションを開始します", Toast.LENGTH_SHORT).show()
        }
        
        // 弱点強化
        binding.layoutWeakness.setOnClickListener {
            Toast.makeText(this, "弱点強化トレーニングを開始します", Toast.LENGTH_SHORT).show()
        }
        
        // 確認テスト
        binding.layoutTest.setOnClickListener {
            Toast.makeText(this, "確認テストを開始します", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}