package com.starryweaver.notes.ui.settings

import android.os.Build
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.data.SettingsRepository
import com.starryweaver.notes.data.ThemeColor
import com.starryweaver.notes.data.ThemeMode
import com.starryweaver.notes.ui.theme.LocalThemeReveal
import com.starryweaver.notes.ui.theme.accentColors
import com.starryweaver.notes.ui.theme.captureToImageBitmap
import com.starryweaver.notes.ui.theme.displayName

private val themeModeOptions = listOf(
    ThemeMode.SYSTEM to "跟随系统",
    ThemeMode.LIGHT to "浅色",
    ThemeMode.DARK to "深色",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    repository: NoteRepository,
    settings: SettingsRepository,
    onBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(repository, settings))
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val customColorInt by viewModel.customThemeColor.collectAsState()

    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorActive = dynamicColor && dynamicColorSupported
    val customColor = Color(customColorInt)

    val view = LocalView.current
    val reveal = LocalThemeReveal.current

    var customDialog by remember { mutableStateOf(false) }
    var modeOrigin by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("外观") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection("主题") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .onGloballyPositioned { coords ->
                            val size = coords.size
                            modeOrigin = coords.positionInRoot() +
                                Offset(size.width / 2f, size.height / 2f)
                        },
                ) {
                    Text("主题模式", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeModeOptions.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = {
                                    val snapshot = captureToImageBitmap(view)
                                    reveal.requestReveal(modeOrigin, snapshot)
                                    viewModel.setThemeMode(mode)
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = themeModeOptions.size,
                                ),
                            ) {
                                Text(label)
                            }
                        }
                    }
                }

                HorizontalDivider()

                SettingRow(
                    title = "动态取色",
                    subtitle = if (dynamicColorSupported) {
                        "跟随壁纸配色（Android 12+）"
                    } else {
                        "需要 Android 12 及以上"
                    },
                ) {
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = viewModel::setDynamicColor,
                        enabled = dynamicColorSupported,
                    )
                }

                HorizontalDivider()

                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text("主题色", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (dynamicColorActive) {
                            "动态取色已开启，将优先使用壁纸配色"
                        } else {
                            "选择应用强调色"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ThemeColor.entries.forEach { color ->
                            ThemeSwatch(
                                label = color.displayName(),
                                swatchColor = if (color == ThemeColor.CUSTOM) {
                                    customColor
                                } else {
                                    accentColors(color, dark = false, customSeed = customColor).primary
                                },
                                selected = themeColor == color,
                                enabled = !dynamicColorActive,
                                onClick = {
                                    if (color == ThemeColor.CUSTOM) {
                                        customDialog = true
                                    } else {
                                        viewModel.setThemeColor(color)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (customDialog) {
        CustomColorDialog(
            initial = customColor,
            onConfirm = { picked ->
                viewModel.setCustomThemeColor(picked.toArgb())
                viewModel.setThemeColor(ThemeColor.CUSTOM)
                customDialog = false
            },
            onDismiss = { customDialog = false },
        )
    }
}
