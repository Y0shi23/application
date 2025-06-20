package com.tango.application.data

import java.util.Date

data class LearningHistory(
    val id: Long,
    val type: LearningType,
    val title: String,
    val description: String,
    val wordCount: Int,
    val correctRate: Int,
    val timestamp: Date,
    val duration: Int // 学習時間（分）
)

enum class LearningType(val displayName: String, val icon: String) {
    NEW_WORDS("新規単語学習", "📝"),
    REVIEW("復習セッション", "🔄"),
    TEST("確認テスト", "🎯"),
    WEAKNESS_TRAINING("弱点強化", "💪")
}

// 学習履歴のサンプルデータを生成するヘルパー関数
object LearningHistoryHelper {
    
    fun getSampleHistory(): List<LearningHistory> {
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        val oneDay = 24 * oneHour
        
        return listOf(
            LearningHistory(
                id = 1,
                type = LearningType.NEW_WORDS,
                title = "新規単語学習",
                description = "15個の単語を学習 • 正答率 92%",
                wordCount = 15,
                correctRate = 92,
                timestamp = Date(now - 2 * oneHour),
                duration = 25
            ),
            LearningHistory(
                id = 2,
                type = LearningType.REVIEW,
                title = "復習セッション",
                description = "12個の単語を復習 • 正答率 87%",
                wordCount = 12,
                correctRate = 87,
                timestamp = Date(now - 6 * oneHour),
                duration = 18
            ),
            LearningHistory(
                id = 3,
                type = LearningType.TEST,
                title = "確認テスト",
                description = "20問のテスト • 正答率 95%",
                wordCount = 20,
                correctRate = 95,
                timestamp = Date(now - oneDay - 4 * oneHour),
                duration = 12
            ),
            LearningHistory(
                id = 4,
                type = LearningType.WEAKNESS_TRAINING,
                title = "弱点強化",
                description = "8個の苦手単語を集中学習 • 正答率 78%",
                wordCount = 8,
                correctRate = 78,
                timestamp = Date(now - oneDay - 8 * oneHour),
                duration = 20
            ),
            LearningHistory(
                id = 5,
                type = LearningType.NEW_WORDS,
                title = "新規単語学習",
                description = "18個の単語を学習 • 正答率 89%",
                wordCount = 18,
                correctRate = 89,
                timestamp = Date(now - 2 * oneDay - 3 * oneHour),
                duration = 30
            )
        )
    }
    
    fun formatTimeAgo(timestamp: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp.time
        val oneHour = 60 * 60 * 1000L
        val oneDay = 24 * oneHour
        
        return when {
            diff < oneDay -> "今日"
            diff < 2 * oneDay -> "昨日"
            diff < 7 * oneDay -> "${diff / oneDay}日前"
            else -> "${diff / (7 * oneDay)}週間前"
        }
    }
    
    fun formatTime(timestamp: Date): String {
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(timestamp)
    }
} 