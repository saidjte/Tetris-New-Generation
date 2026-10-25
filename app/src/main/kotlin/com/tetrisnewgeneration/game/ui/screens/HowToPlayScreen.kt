package com.tetrisnewgeneration.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.TngCard
import com.tetrisnewgeneration.game.ui.components.localizedText

/**
 * Bilingual "How to play" screen: controls (buttons + optional gestures),
 * scoring table and mode overview. Shown once on first launch and always
 * reachable from Home.
 */
@Composable
fun HowToPlayScreen(
    theme: ThemeDef,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(theme.backgroundTop)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 24.dp)
        ) {
            Text(
                stringResource(R.string.btn_howto),
                color = theme.textPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(14.dp))

            SectionCard(theme, localizedText("الهدف", "Goal")) {
                Text(
                    localizedText(
                        "دوّر القطع المتساقطة ورتّبها لملء صفوف كاملة تُمحى قبل أن تصل القمة. كلما صفّت صفوفاً دفعة واحدة زادت النقاط، وترتفع السرعة مع المستويات.",
                        "Rotate and place falling pieces to fill complete rows that clear before the stack reaches the top. Clearing more rows at once scores more, and speed rises with each level."
                    ),
                    color = theme.textPrimary.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            SectionCard(theme, localizedText("التحكم", "Controls")) {
                HowRow(theme, localizedText("أزرار الشاشة", "On-screen buttons"))
                Text(
                    localizedText(
                        "يسار/يمين: تحريك • ⟳/⟲: دوران • ⤓: إسقاط صلب فوري • ⇄: تبديل Hold • ▼: هبوط ناعم (استمر بالضغط)",
                        "Left/Right: move • ⟳/⟲: rotate • ⤓: hard drop • ⇄: hold • ▼: soft drop (hold)"
                    ),
                    color = theme.textPrimary.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                HowRow(theme, localizedText("إيماءات اللمس (اختيارية من الإعدادات)", "Touch gestures (optional, from Settings)"))
                Text(
                    localizedText(
                        "تعمل على كامل شاشة اللعب ويؤكدها اهتزاز:\n• نقرة في أي مكان = دوران القطعة\n• سحب أفقي = تحريك (خلية بخطوة)\n• سحب لأسفل ببطء = هبوط ناعم\n• سحب سريع لأسفل = إسقاط فوري\n• سحب لأعلى = حفظ القطعة (Hold)\nالأزرار على الشاشة تبقى تعمل في كل الأحوال.",
                        "Gestures work across the whole play screen, each confirmed by a haptic tick:\n• Tap anywhere = rotate\n• Horizontal drag = move (one cell per step)\n• Slow drag down = soft drop\n• Fast flick down = hard drop\n• Swipe up = hold\nThe on-screen buttons always remain available."
                    ),
                    color = theme.textPrimary.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            SectionCard(theme, localizedText("النقاط", "Scoring")) {
                ScoreRow(theme, localizedText("صف واحد / صفان / ثلاثة", "Single / Double / Triple"), "100 / 300 / 500")
                ScoreRow(theme, localizedText("تصفية رباعية (تتريس)", "Tetris (4 rows)"), "800")
                ScoreRow(theme, localizedText("سبين T كامل (بدون صفوف/صف/صفان)", "Full T-Spin (0/1/2 lines)"), "400 / 800 / 1200")
                ScoreRow(theme, localizedText("سبين T مصغّر (بدون صفوف/صف)", "Mini T-Spin (0/1 line)"), "100 / 200")
                ScoreRow(theme, localizedText("خلف بعض (B2B): تتريس أو سبين متتاليان", "Back-to-Back: consecutive Tetris/T-Spin"), "×1.5")
                ScoreRow(theme, localizedText("كومبو: كل صف متتالٍ إضافي", "Combo: each consecutive clear"), "+50 × المستوى × السلسلة")
                ScoreRow(theme, localizedText("مسح مثالي للوحة", "Perfect Clear"), "+800…2000")
                Spacer(Modifier.height(4.dp))
                Text(
                    localizedText(
                        "النقاط تتضاعف بمستوى اللعب الحالي.",
                        "All values are multiplied by the current level."
                    ),
                    color = theme.textPrimary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            SectionCard(theme, localizedText("أنماط اللعب", "Game modes")) {
                Text(
                    localizedText(
                        "11 نمطاً: كلاسيكي وماراثون ولعب سريع وتحديات وسباق وقت وبقاء وسرعة قصوى وألغاز واسترخاء ومغامرة وممارسة — لكل نمط هدفه وقواعده الخاصة، والمتابعة تعيد آخر جولة كما تركتها.",
                        "11 modes: Classic, Marathon, Quick Play, Challenges, Time Attack, Survival, Speed, Puzzles, Zen, Adventure and Practice — each with its own goal and rules. Continue restores your last round exactly as you left it."
                    ),
                    color = theme.textPrimary.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                com.tetrisnewgeneration.game.ui.components.PrimaryActionButton(
                    text = stringResource(R.string.summary_home),
                    theme = theme,
                    onClick = onBack
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SectionCard(
    theme: ThemeDef,
    title: String,
    content: @Composable () -> Unit
) {
    TngCard(theme = theme, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            title,
            color = theme.accent,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun HowRow(theme: ThemeDef, label: String) {
    Text(
        label,
        color = theme.textPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
}

@Composable
private fun ScoreRow(theme: ThemeDef, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = theme.textPrimary.copy(alpha = 0.85f),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(
            value,
            color = theme.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
