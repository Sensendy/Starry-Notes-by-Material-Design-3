package com.starryweaver.notes.ui.privacy

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

object PrivacyPolicy {

    const val UPDATED = "2026年10月4日"

    val sections: List<Pair<String, String>> = listOf(
        "一、信息收集" to
            "本应用不收集、不上传任何个人信息。您的笔记、标签与全部设置仅保存在本机应用私有目录中，不会发送到任何服务器。",
        "二、网络与第三方" to
            "本应用不含联网功能，也未集成任何广告、统计分析或第三方服务 SDK。",
        "三、权限说明" to
            "本应用不申请敏感权限。导入与导出通过系统文件选择器完成，仅由您主动授权对单个文件的访问。",
        "四、数据删除" to
            "卸载本应用，或在系统设置中清除应用数据，即可删除全部本地数据。",
        "五、政策更新" to
            "本政策如有调整，将在应用内更新上述日期并重新提示。",
    )
}

@Composable
fun PrivacyPolicyBody(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "更新日期：${PrivacyPolicy.UPDATED}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrivacyPolicy.sections.forEach { (heading, body) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(heading, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 首次启动的隐私政策弹窗：不可通过返回键或点击外部关闭，必须做出选择。 */
@Composable
fun PrivacyPolicyGateDialog(onAgree: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = { Text("隐私政策") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                PrivacyPolicyBody()
            }
        },
        confirmButton = {
            TextButton(onClick = onAgree) { Text("同意并继续") }
        },
        dismissButton = {
            TextButton(onClick = { (context as? Activity)?.finish() }) { Text("不同意") }
        },
    )
}
