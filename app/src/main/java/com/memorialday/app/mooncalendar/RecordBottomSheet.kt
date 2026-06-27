// 绵绵月历 - 记录页面
package com.memorialday.app.mooncalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.*

private val Pink = Color(0xFFFF6B9D)
private val Symptoms = listOf("腰酸","胸痛","头痛","乏力","腹胀","长痘","水肿","乳房胀痛","怕冷","食欲差")

@Composable
fun RecordBottomSheet(
    date: Date,
    existingRecord: DailyRecord?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var isPeriod by remember { mutableStateOf(existingRecord?.isPeriod ?: false) }
    var flow by remember { mutableStateOf(existingRecord?.flow ?: FlowLevel.NONE) }
    var pain by remember { mutableStateOf(existingRecord?.painLevel ?: PainLevel.NONE) }
    var selectedSymptoms by remember { mutableStateOf(existingRecord?.symptoms ?: emptyList<String>()) }
    var mood by remember { mutableStateOf(existingRecord?.mood ?: Mood.PEACEFUL) }
    var sleepHours by remember { mutableStateOf(existingRecord?.sleepHours ?: 7.5) }
    var exercised by remember { mutableStateOf(existingRecord?.exercised ?: false) }
    var waterCups by remember { mutableStateOf(existingRecord?.waterCups ?: 8) }
    var notes by remember { mutableStateOf(existingRecord?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            color = Color.White) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)
            ) {
                // 顶部
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("记录今日", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = Color.Gray) }
                }

                Spacer(Modifier.height(16.dp))

                // 经期
                Text("经期状态", fontSize = 13.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("今天来姨妈了吗？", fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = isPeriod, onCheckedChange = { isPeriod = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Pink, checkedTrackColor = Pink.copy(alpha = 0.3f)))
                }

                if (isPeriod) {
                    Spacer(Modifier.height(8.dp))
                    Text("流量", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowLevel.entries.filter { it != FlowLevel.NONE }.forEach { f ->
                            FilterChip(f.name, f == flow, { flow = f }, Pink)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("痛经", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PainLevel.entries.filter { it != PainLevel.NONE }.forEach { p ->
                            val labels = mapOf(PainLevel.MILD to "轻", PainLevel.MODERATE to "中", PainLevel.SEVERE to "重")
                            FilterChip(labels[p] ?: p.name, p == pain, { pain = p }, Pink)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 症状
                Text("身体症状", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Symptoms.chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { symptom ->
                            val selected = selectedSymptoms.contains(symptom)
                            Surface(
                                modifier = Modifier.clickable {
                                    selectedSymptoms = if (selected) selectedSymptoms - symptom else selectedSymptoms + symptom
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) Pink else Color(0xFFF2EFEC)
                            ) {
                                Text(symptom, fontSize = 12.sp, color = if (selected) Color.White else Color(0xFF2C2C2C),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(16.dp))

                // 心情
                Text("今日心情", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Mood.entries.forEach { m ->
                        Column(
                            modifier = Modifier.clickable { mood = m },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(m.emoji, fontSize = if (mood == m) 28.sp else 22.sp)
                            Text(m.label, fontSize = 10.sp, color = if (mood == m) Pink else Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 生活习惯
                Text("生活习惯", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("睡眠", fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Text("${sleepHours.toInt()}小时", fontSize = 14.sp, color = Pink)
                }
                Slider(value = sleepHours.toFloat(), onValueChange = { sleepHours = it.toDouble() },
                    valueRange = 4f..12f, colors = SliderDefaults.colors(thumbColor = Pink, activeTrackColor = Pink))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("运动", fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = exercised, onCheckedChange = { exercised = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Pink, checkedTrackColor = Pink.copy(alpha = 0.3f)))
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("喝水", fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Text("${waterCups}杯", fontSize = 14.sp, color = Pink)
                    IconButton(onClick = { if (waterCups < 20) waterCups++ }) { Text("+", fontSize = 18.sp, color = Pink) }
                    IconButton(onClick = { if (waterCups > 0) waterCups-- }) { Text("-", fontSize = 18.sp, color = Color.Gray) }
                }

                Spacer(Modifier.height(16.dp))

                // 备注
                Text("想说点什么", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    placeholder = { Text("今天有什么特别的吗...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(10.dp))

                Spacer(Modifier.height(20.dp))

                // 保存
                Button(
                    onClick = {
                        val record = DailyRecord(
                            date = date, isPeriod = isPeriod, flow = flow,
                            painLevel = pain, symptoms = selectedSymptoms, mood = mood,
                            sleepHours = sleepHours, exercised = exercised, waterCups = waterCups, notes = notes
                        )
                        MoonCalendarStorage.saveRecord(record)
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink)
                ) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit, accentColor: Color) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accentColor else Color(0xFFF2EFEC)
    ) {
        Text(label, fontSize = 13.sp, color = if (selected) Color.White else Color(0xFF2C2C2C),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp))
    }
}
