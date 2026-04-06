package com.crispim.recentapps

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * 初期セットアップ画面。
 * 必要な権限をユーザーに案内する。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val dp = resources.displayMetrics.density
            setPadding(
                (24 * dp).toInt(), (60 * dp).toInt(),
                (24 * dp).toInt(), (40 * dp).toInt()
            )
        }
        scroll.addView(root)

        // タイトル
        root.addView(TextView(this).apply {
            text = "RecentApps セットアップ"
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (8 * resources.displayMetrics.density).toInt())
        })

        root.addView(TextView(this).apply {
            text = "カバー画面でメイン画面・カバー画面両方の最近アプリを表示します"
            textSize = 14f
            gravity = Gravity.CENTER
            alpha = 0.7f
            setPadding(0, 0, 0, (32 * resources.displayMetrics.density).toInt())
        })

        // Step 1: 使用状況へのアクセス
        addPermissionStep(
            root,
            stepNum = "1",
            title = "使用状況へのアクセス",
            description = "メイン画面・カバー画面両方のアプリ履歴を取得するために必要です。\n" +
                    "「RecentApps」をONにしてください。",
            buttonLabel = "権限を設定 →"
        ) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        // Step 2: ユーザー補助サービス
        addPermissionStep(
            root,
            stepNum = "2",
            title = "ユーザー補助サービス",
            description = "アプリ起動の追跡とオーバーレイ表示に必要です。\n" +
                    "「RecentApps」をONにしてください。",
            buttonLabel = "ユーザー補助を設定 →"
        ) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // Step 3: QuickSettingsタイルの追加方法
        addInfoStep(
            root,
            stepNum = "3",
            title = "クイック設定タイルを追加",
            description = "クイック設定パネルを開いて編集モードにし、\n" +
                    "「最近のアプリ」タイルをパネルに追加してください。\n\n" +
                    "カバー画面でも同様にタイルを追加できます。"
        )

        // 状態表示
        val statusText = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, (24 * resources.displayMetrics.density).toInt(), 0, 0)
        }
        root.addView(statusText)
        statusView = statusText

        setContentView(scroll)
    }

    private var statusView: TextView? = null

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val hasUsage = hasUsageStatsPermission()
        val hasA11y = isAccessibilityEnabled()

        statusView?.text = when {
            hasUsage && hasA11y ->
                "✅ セットアップ完了！\nカバー画面のクイック設定から\n「最近のアプリ」をタップして使ってください。"
            !hasUsage && !hasA11y ->
                "⚠️ Step 1, 2 の設定が必要です"
            !hasUsage ->
                "⚠️ Step 1 (使用状況へのアクセス) が必要です"
            else ->
                "⚠️ Step 2 (ユーザー補助) が必要です"
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(packageName, ignoreCase = true)
    }

    // -----------------------------------------------------------------------
    // UI ヘルパー
    // -----------------------------------------------------------------------

    private fun addPermissionStep(
        parent: LinearLayout,
        stepNum: String,
        title: String,
        description: String,
        buttonLabel: String,
        action: () -> Unit
    ) {
        val dp = resources.displayMetrics.density
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, (28 * dp).toInt())
        }

        section.addView(TextView(this).apply {
            text = "Step $stepNum: $title"
            textSize = 16f
            setPadding(0, 0, 0, (6 * dp).toInt())
        })
        section.addView(TextView(this).apply {
            text = description
            textSize = 13f
            alpha = 0.75f
            setPadding(0, 0, 0, (12 * dp).toInt())
        })
        section.addView(Button(this).apply {
            text = buttonLabel
            setOnClickListener { action() }
        })

        parent.addView(section)
    }

    private fun addInfoStep(
        parent: LinearLayout,
        stepNum: String,
        title: String,
        description: String
    ) {
        val dp = resources.displayMetrics.density
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, (28 * dp).toInt())
        }

        section.addView(TextView(this).apply {
            text = "Step $stepNum: $title"
            textSize = 16f
            setPadding(0, 0, 0, (6 * dp).toInt())
        })
        section.addView(TextView(this).apply {
            text = description
            textSize = 13f
            alpha = 0.75f
        })

        parent.addView(section)
    }
}
