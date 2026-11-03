package com.pharmalibrary.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF102A43)
private val Teal = Color(0xFF0F8B8D)
private val Pale = Color(0xFFF3F7F8)
private val Muted = Color(0xFF627D98)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PharmaLibraryApp() }
    }
}

@Composable
fun PharmaLibraryApp() {
    var selectedTab by remember { mutableStateOf(0) }
    var pageTitle by remember { mutableStateOf<String?>(null) }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Navy,
            secondary = Teal,
            background = Pale,
            surface = Color.White
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Pale,
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        NavigationBarItem(
                            selected = selectedTab == 0 && pageTitle == null,
                            onClick = { selectedTab = 0; pageTitle = null },
                            icon = { Icon(Icons.Default.Home, null) },
                            label = { Text("الرئيسية") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1 && pageTitle == null,
                            onClick = { selectedTab = 1; pageTitle = null },
                            icon = { Icon(Icons.Default.MenuBook, null) },
                            label = { Text("المكتبة") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2 && pageTitle == null,
                            onClick = { selectedTab = 2; pageTitle = null },
                            icon = { Icon(Icons.Default.AdminPanelSettings, null) },
                            label = { Text("الإدارة") }
                        )
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    when {
                        pageTitle != null -> DetailPlaceholder(pageTitle!!, onBack = { pageTitle = null })
                        selectedTab == 0 -> HomeScreen(
                            onOpenLibrary = { selectedTab = 1 },
                            onOpenPage = { pageTitle = it }
                        )
                        selectedTab == 1 -> LibraryScreen(onOpenPage = { pageTitle = it })
                        else -> AdminIntroScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onOpenLibrary: () -> Unit, onOpenPage: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Navy),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.LocalPharmacy, null, tint = Color.White, modifier = Modifier.size(27.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("PHARMA LIBRARY", fontWeight = FontWeight.ExtraBold, color = Navy, fontSize = 18.sp)
                    Text("مكتبتك العلمية في علوم الصيدلة", color = Muted, fontSize = 12.sp)
                }
                Surface(color = Color(0xFFE1F3F1), shape = CircleShape) {
                    Text("نسخة تأسيسية", color = Teal, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Navy).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("مرحباً بك في", color = Color(0xFFB8D8DE), fontSize = 14.sp)
                Text("PHARMA LIBRARY", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("مصدر واحد للمحاضرات والملخصات والأسئلة والمراجع الصيدلانية.", color = Color.White, lineHeight = 22.sp)
                Button(onClick = onOpenLibrary, colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                    Text("تصفح المكتبة")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowBack, null)
                }
            }
        }
        item { SectionHeading("استكشف المكتبة", "اختر القسم الذي تريد الوصول إليه") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard("السنوات الدراسية", "تنظيم حسب المستوى", Icons.Default.School, Modifier.weight(1f)) { onOpenPage("السنوات الدراسية") }
                FeatureCard("المواد", "جميع المقررات", Icons.Default.MenuBook, Modifier.weight(1f)) { onOpenPage("المواد الدراسية") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard("الأسئلة", "تدرب واختبر نفسك", Icons.Default.Quiz, Modifier.weight(1f)) { onOpenPage("بنك الأسئلة") }
                FeatureCard("الملخصات", "مراجعة مركزة", Icons.Default.Description, Modifier.weight(1f)) { onOpenPage("الملخصات") }
            }
        }
        item { SectionHeading("أحدث الإضافات", "ستظهر هنا أحدث المحاضرات المنشورة") }
        item { EmptyState("لا توجد محاضرات منشورة حالياً", "عند ربط قاعدة البيانات ستظهر المواد والمحاضرات التي ينشرها فريق الإدارة.") }
        item {
            Text("صُممت المكتبة لتسهيل الوصول إلى المحتوى الدراسي لطلاب الصيدلة.", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
        }
    }
}

@Composable
private fun LibraryScreen(onOpenPage: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("المكتبة الدراسية", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy) }
        item { Text("تصفح المحتوى وفق التسلسل الأكاديمي", color = Muted) }
        item { LibraryRow("السنوات الدراسية", "ابدأ باختيار السنة", Icons.Default.School) { onOpenPage("السنوات الدراسية") } }
        item { LibraryRow("الفصول الدراسية", "الفصل الأول والفصل الثاني", Icons.Default.DateRange) { onOpenPage("الفصول الدراسية") } }
        item { LibraryRow("المواد الدراسية", "المقررات والمحاضرات", Icons.Default.MenuBook) { onOpenPage("المواد الدراسية") } }
        item { LibraryRow("بنك الأسئلة", "أسئلة وتدريبات للمراجعة", Icons.Default.Quiz) { onOpenPage("بنك الأسئلة") } }
        item { LibraryRow("الملخصات والترجمات", "محتوى مساعد للمذاكرة", Icons.Default.Translate) { onOpenPage("الملخصات والترجمات") } }
        item { EmptyState("المكتبة بانتظار المحتوى", "سيتم تحميل المحتوى من قاعدة البيانات السحابية بعد إتمام الربط.") }
    }
}

@Composable
private fun AdminIntroScreen() {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("إدارة المكتبة", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = Teal, modifier = Modifier.size(36.dp))
                Text("منطقة خاصة بالمالك والمشرفين", fontWeight = FontWeight.Bold, color = Navy, fontSize = 17.sp)
                Text("ستتضمن لوحة الإدارة تسجيل دخول آمن، وإدارة السنوات والفصول والمواد والدفعات والمحاضرات والملفات، مع صلاحيات مستقلة لكل مشرف.", color = Muted, lineHeight = 22.sp)
                Text("لم يتم تفعيل تسجيل الدخول بعد. لن نضع بيانات دخول تجريبية أو كلمات مرور ثابتة داخل التطبيق.", color = Color(0xFF9A6700), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DetailPlaceholder(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowForward, contentDescription = "رجوع") }
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
        EmptyState("سيتم تجهيز هذا القسم", "هذا القسم جزء من هيكل التطبيق، وسيُربط بقاعدة البيانات عند استكمال المرحلة التالية.")
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(subtitle, fontSize = 12.sp, color = Muted)
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(icon, null, tint = Teal, modifier = Modifier.size(25.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LibraryRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = Color.White) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFE8F4F3)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Teal)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Navy)
                Text(subtitle, fontSize = 11.sp, color = Muted)
            }
            Icon(Icons.Default.ChevronLeft, null, tint = Muted)
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.LibraryBooks, null, tint = Color(0xFF9FB3C8), modifier = Modifier.size(38.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Navy, textAlign = TextAlign.Center)
            Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
    }
}
