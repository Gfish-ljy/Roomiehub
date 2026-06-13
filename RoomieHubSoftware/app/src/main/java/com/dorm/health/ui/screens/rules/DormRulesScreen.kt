package com.dorm.health.ui.screens.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dorm.health.ui.components.AppBackground
import com.dorm.health.ui.components.GlassCard
import com.dorm.health.ui.components.dividerColor
import com.dorm.health.ui.theme.isAppInDarkTheme

private data class DormRuleSection(
    val title: String,
    val icon: ImageVector,
    val summary: String,
    val rules: List<String>
)

private val defaultRuleSections = listOf(
    DormRuleSection(
        title = "作息规范",
        icon = Icons.Outlined.Bedtime,
        summary = "保障全员休息质量，RoomieHub 会监测深夜环境活跃度",
        rules = listOf(
            "工作日 23:00 后降低音量，00:30 前熄灯休息",
            "周末 00:30 前保持安静，避免影响室友",
            "若有考试或特殊情况，提前在群内告知室友",
            "深夜不使用强光、不外放音视频"
        )
    ),
    DormRuleSection(
        title = "卫生环境",
        icon = Icons.Outlined.CleaningServices,
        summary = "共同维护整洁、通风良好的居住环境",
        rules = listOf(
            "每日整理个人床铺与桌面，垃圾及时清理",
            "每周轮流打扫地面、卫生间与阳台",
            "保持通风，空气质量较差时主动开窗换气",
            "不在宿舍内吸烟、堆放易燃杂物"
        )
    ),
    DormRuleSection(
        title = "噪音控制",
        icon = Icons.Outlined.VolumeOff,
        summary = "噪音过高将触发 APP 提醒并影响健康评分",
        rules = listOf(
            "通话、游戏请佩戴耳机，避免外放",
            "关门、收纳物品尽量轻放，减少碰撞声",
            "22:00 后不在宿舍大声讨论或喧哗",
            "乐器、音响等活动需经全体室友同意"
        )
    ),
    DormRuleSection(
        title = "用电安全",
        icon = Icons.Outlined.ElectricBolt,
        summary = "规范大功率电器使用，杜绝安全隐患",
        rules = listOf(
            "禁止使用热得快、电炉、明火等违规电器",
            "离开宿舍关闭电源、拔掉不必要的插排",
            "不私拉乱接电线，插排不超负荷使用",
            "发现线路异常立即报修并告知室友"
        )
    ),
    DormRuleSection(
        title = "公共物品与访客",
        icon = Icons.Outlined.Groups,
        summary = "尊重彼此空间，共建和谐宿舍关系",
        rules = listOf(
            "不擅自使用他人物品，借用前需征得同意",
            "访客需提前告知，22:00 后不建议留宿访客",
            "公共区域物品使用后归位，保持共享整洁",
            "有矛盾优先协商，必要时联系宿管或辅导员"
        )
    )
)

@Composable
fun DormRulesScreen() {
    val isDark = isAppInDarkTheme()

    AppBackground(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "宿舍公约",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "以下约定由本宿舍共同遵守，配合 RoomieHub 环境监测，营造健康、安静、安全的居住空间。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                modifier = Modifier.fillMaxWidth()
            )

            GlassCard(isDark = isDark) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "公约说明",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "公约内容可在宿舍会议中讨论修订；违反约定经提醒后仍不改正，按学院宿舍管理规定处理。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            defaultRuleSections.forEach { section ->
                DormRuleSectionCard(section = section, isDark = isDark)
            }

            GlassCard(isDark = isDark) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "签署确认",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "全体室友阅读并认可本公约后，即视为同意共同遵守。建议每学期初重新确认一次。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DormRuleSectionCard(
    section: DormRuleSection,
    isDark: Boolean
) {
    GlassCard(isDark = isDark) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(22.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = section.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(color = dividerColor(isDark))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                section.rules.forEachIndexed { index, rule ->
                    DormRuleItem(index = index + 1, text = rule)
                }
            }
        }
    }
}

@Composable
private fun DormRuleItem(index: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            modifier = Modifier.weight(1f)
        )
    }
}
