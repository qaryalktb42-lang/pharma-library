package com.pharmalibrary.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
private val repository = LocalLibraryRepository()

private sealed class Route {
    data object Home : Route()
    data object Library : Route()
    data object Downloads : Route()
    data object Admin : Route()
    data class Semesters(val yearId: String) : Route()
    data class Subjects(val semesterId: String) : Route()
    data class Batches(val subjectId: String) : Route()
    data class Lectures(val batchId: String) : Route()
    data class LectureDetail(val lectureId: String) : Route()
    data class SearchResults(val query: String) : Route()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PharmaLibraryApp() }
    }
}

@Composable
fun PharmaLibraryApp() {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    var searchText by rememberSaveable { mutableStateOf("") }
    MaterialTheme(colorScheme = lightColorScheme(primary = Navy, secondary = Teal, background = Pale, surface = Color.White)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Pale,
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        NavItem(route is Route.Home, "الرئيسية", Icons.Default.Home) { route = Route.Home }
                        NavItem(route is Route.Library || route is Route.Semesters || route is Route.Subjects || route is Route.Batches || route is Route.Lectures || route is Route.LectureDetail, "المكتبة", Icons.Default.MenuBook) { route = Route.Library }
                        NavItem(route is Route.Downloads, "التنزيلات", Icons.Default.Download) { route = Route.Downloads }
                        NavItem(route is Route.Admin, "الإدارة", Icons.Default.AdminPanelSettings) { route = Route.Admin }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (val current = route) {
                        Route.Home -> HomeScreen(searchText, { searchText = it }, { route = Route.SearchResults(searchText) }, { route = it })
                        Route.Library -> LibraryScreen { route = it }
                        Route.Downloads -> DownloadsScreen { repository.toggleDownloaded(it) }
                        Route.Admin -> AdminScreen()
                        is Route.SearchResults -> SearchResultsScreen(current.query) { route = Route.LectureDetail(it) }
                        is Route.Semesters -> SemesterScreen(current.yearId, { route = it }, { route = Route.Library })
                        is Route.Subjects -> SubjectScreen(current.semesterId, { route = it }, { route = Route.Semesters(repository.semester(current.semesterId).yearId) })
                        is Route.Batches -> BatchScreen(current.subjectId, { route = it }, { route = Route.Subjects(repository.subject(current.subjectId).semesterId) })
                        is Route.Lectures -> LectureScreen(current.batchId, { route = it }, { route = Route.Batches(repository.batch(current.batchId).subjectId) })
                        is Route.LectureDetail -> LectureDetailScreen(current.lectureId) { route = Route.Lectures(repository.lectures.first { it.id == current.lectureId }.batchId) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(selected: Boolean, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    NavigationBarItem(selected = selected, onClick = onClick, icon = { Icon(icon, null) }, label = { Text(label) })
}

@Composable
private fun HomeScreen(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, open: (Route) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { BrandHeader() }
        item {
            OutlinedTextField(value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), placeholder = { Text("ابحث عن مادة أو محاضرة أو ملف") }, leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = { IconButton(onClick = onSearch) { Icon(Icons.Default.ArrowForward, "بحث") } })
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Navy).padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مكتبتك العلمية في علوم الصيدلة", color = Color(0xFFB8D8DE), fontSize = 14.sp)
                Text("تعلم، راجع، وتقدم", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("محتوى منظم حسب السنوات والفصول والمواد والدفعات.", color = Color.White, lineHeight = 22.sp)
                Button(onClick = { open(Route.Library) }, colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text("تصفح المكتبة"); Spacer(Modifier.width(6.dp)); Icon(Icons.Default.ArrowBack, null) }
            }
        }
        item { SectionHeading("الوصول السريع", "اختر القسم الذي تريد الوصول إليه") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { FeatureCard("السنوات", "المستويات الدراسية", Icons.Default.School, Modifier.weight(1f)) { open(Route.Library) }; FeatureCard("المواد", "المقررات والمحاضرات", Icons.Default.MenuBook, Modifier.weight(1f)) { open(Route.Library) } } }
        item { SectionHeading("أحدث المحاضرات", "محتوى مضاف حديثاً") }
        items(repository.lectures.take(3)) { lecture -> LectureCard(lecture) { open(Route.LectureDetail(lecture.id)) } }
        item { SectionHeading("عن التطبيق", "نسخة محلية قابلة للربط السحابي لاحقاً") }
        item { InfoCard("يعمل دون حساب", "يمكن للطالب تصفح المحتوى محلياً. عند ربط Supabase ستتحدث المكتبة من الخادم دون إصدار APK جديد.", Icons.Default.Cloud) }
    }
}

@Composable
private fun LibraryScreen(open: (Route) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { PageTitle("المكتبة الدراسية", "اختر السنة ثم الفصل والمادة والدفعة") }
        items(repository.years) { year -> LibraryRow(year.title, year.description, Icons.Default.School) { open(Route.Semesters(year.id)) } }
        item { InfoCard("تنظيم أكاديمي واضح", "المحتوى مرتب بالشكل: السنة ← الفصل ← المادة ← الدفعة ← المحاضرة ← الملفات.", Icons.Default.AccountTree) }
    }
}

@Composable
private fun SemesterScreen(yearId: String, open: (Route) -> Unit, back: () -> Unit) {
    val year = repository.year(yearId)
    DetailListHeader(year.title, "الفصول الدراسية المتاحة", back)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { Text(year.description, color = Muted, modifier = Modifier.padding(bottom = 4.dp)) }
        items(repository.semesters.filter { it.yearId == yearId }) { semester -> LibraryRow(semester.title, semester.description, Icons.Default.DateRange) { open(Route.Subjects(semester.id)) } }
        if (repository.semesters.none { it.yearId == yearId }) item { EmptyState("لا توجد فصول بعد", "ستظهر الفصول عند إضافتها محلياً أو بعد ربط قاعدة البيانات.") }
    }
}

@Composable
private fun SubjectScreen(semesterId: String, open: (Route) -> Unit, back: () -> Unit) {
    val semester = repository.semester(semesterId)
    DetailListHeader(semester.title, "المواد الدراسية", back)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        items(repository.subjects.filter { it.semesterId == semesterId }) { subject -> LibraryRow(subject.title, subject.lecturer, Icons.Default.MenuBook) { open(Route.Batches(subject.id)) } }
    }
}

@Composable
private fun BatchScreen(subjectId: String, open: (Route) -> Unit, back: () -> Unit) {
    val subject = repository.subject(subjectId)
    DetailListHeader(subject.title, subject.description, back)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { Text("المحاضر: ${subject.lecturer}", color = Muted) }
        items(repository.batches.filter { it.subjectId == subjectId }) { batch -> LibraryRow(batch.title, "${repository.lecturesForBatch(batch.id).size} محاضرات متاحة", Icons.Default.Groups) { open(Route.Lectures(batch.id)) } }
    }
}

@Composable
private fun LectureScreen(batchId: String, open: (Route) -> Unit, back: () -> Unit) {
    val batch = repository.batch(batchId)
    val subject = repository.subject(batch.subjectId)
    DetailListHeader(batch.title, subject.title, back)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(repository.lecturesForBatch(batchId)) { lecture -> LectureCard(lecture) { open(Route.LectureDetail(lecture.id)) } }
        if (repository.lecturesForBatch(batchId).isEmpty()) item { EmptyState("لا توجد محاضرات بعد", "سيظهر المحتوى هنا عند إضافته.") }
    }
}

@Composable
private fun SearchResultsScreen(query: String, open: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageTitle("نتائج البحث", "نتائج مطابقة لـ: $query") }
        val results = repository.search(query)
        items(results) { lecture -> LectureCard(lecture) { open(lecture.id) } }
        if (results.isEmpty()) item { EmptyState("لم نجد نتائج", "جرّب اسم مادة أو محاضرة آخر، ويمكن البحث دون تشكيل.") }
    }
}

@Composable
private fun LectureDetailScreen(lectureId: String, back: () -> Unit) {
    val lecture = repository.lectures.first { it.id == lectureId }
    val subject = repository.subject(lecture.subjectId)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { DetailListHeader("المحاضرة ${lecture.number}", lecture.title, back) }
        item { InfoCard(lecture.description, "${subject.title} • ${lecture.updatedAt}", Icons.Default.MenuBook) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("أقسام المحتوى", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Navy, modifier = Modifier.weight(1f))
                IconButton(onClick = { repository.toggleFavorite(lecture.id) }) { Icon(if (repository.isFavorite(lecture.id)) Icons.Default.Star else Icons.Default.StarBorder, "مفضلة", tint = Color(0xFFE0A100)) }
            }
        }
        items(lecture.assets) { asset -> AssetRow(asset, repository.isDownloaded(asset.id)) { repository.toggleDownloaded(asset.id) } }
        item { Text("المحتوى المحلي تجريبي حالياً. بعد ربط Supabase ستظهر الملفات الفعلية وتعمل المعاينة والتنزيل من التخزين السحابي.", color = Color(0xFF8A5A00), fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 8.dp)) }
    }
}

@Composable
private fun DownloadsScreen(onDelete: (String) -> Unit) {
    val saved = repository.lectures.flatMap { it.assets }.filter { repository.isDownloaded(it.id) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { PageTitle("التنزيلات", "الملفات المحفوظة على الهاتف") }
        if (saved.isEmpty()) item { EmptyState("لا توجد ملفات محفوظة", "اضغط أيقونة التنزيل بجانب ملف من صفحة المحاضرة ليظهر هنا.") }
        items(saved) { asset -> AssetRow(asset, true) { onDelete(asset.id) } }
        item { InfoCard("وضع العمل دون اتصال", "هذه الصفحة مجهزة لحفظ الملفات محلياً وإدارتها. الملفات الفعلية ستُنزّل من Supabase Storage بعد الربط.", Icons.Default.Storage) }
    }
}

@Composable
private fun AdminScreen() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { PageTitle("لوحة الإدارة", "إدارة محلية مؤقتة قبل تفعيل المصادقة السحابية") }
        item { InfoCard("وضع المالك غير متصل", "لا يوجد تسجيل دخول أو صلاحيات حقيقية في الوضع المحلي. لن يتم حفظ تغييرات الإدارة على خادم قبل ربط Supabase وRLS.", Icons.Default.AdminPanelSettings) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatCard("السنوات", repository.years.size.toString(), Modifier.weight(1f)); StatCard("المواد", repository.subjects.size.toString(), Modifier.weight(1f)); StatCard("المحاضرات", repository.lectures.size.toString(), Modifier.weight(1f)) } }
        item { AdminAction("إدارة السنوات والفصول", "الهيكل الأكاديمي جاهز للربط بعمليات الإضافة والتعديل والحذف.", Icons.Default.AccountTree) }
        item { AdminAction("إدارة الملفات", "رفع PDF والصور والصوت والفيديو سيُفعّل مع Storage.", Icons.Default.UploadFile) }
        item { AdminAction("المشرفون والصلاحيات", "سيتم تطبيقها على مستوى RLS والخادم، لا بمجرد إخفاء الأزرار.", Icons.Default.Groups) }
    }
}

@Composable private fun BrandHeader() { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Navy), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocalPharmacy, null, tint = Color.White, modifier = Modifier.size(27.dp)) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("PHARMA LIBRARY", fontWeight = FontWeight.ExtraBold, color = Navy, fontSize = 18.sp); Text("مكتبتك العلمية في علوم الصيدلة", color = Muted, fontSize = 12.sp) }; Surface(color = Color(0xFFE1F3F1), shape = CircleShape) { Text("محلي", color = Teal, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) } } }
@Composable private fun PageTitle(title: String, subtitle: String) { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy); Text(subtitle, color = Muted) } }
@Composable private fun DetailListHeader(title: String, subtitle: String, back: () -> Unit) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(18.dp)) { IconButton(onClick = back) { Icon(Icons.Default.ArrowForward, "رجوع") }; Column { Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Navy); Text(subtitle, color = Muted, fontSize = 12.sp) } } }
@Composable private fun SectionHeading(title: String, subtitle: String) { Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy); Text(subtitle, fontSize = 12.sp, color = Muted) } }
@Composable private fun FeatureCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) { Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 1.dp) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(icon, null, tint = Teal, modifier = Modifier.size(25.dp)); Text(title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 14.sp); Text(subtitle, color = Muted, fontSize = 11.sp) } } }
@Composable private fun LibraryRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = Color.White) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFE8F4F3)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Teal) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, color = Navy); Text(subtitle, fontSize = 11.sp, color = Muted) }; Icon(Icons.Default.ChevronLeft, null, tint = Muted) } } }
@Composable private fun LectureCard(lecture: Lecture, onClick: () -> Unit) { val subject = repository.subject(lecture.subjectId); Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(17.dp), color = Color.White) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(45.dp).clip(RoundedCornerShape(13.dp)).background(Navy), contentAlignment = Alignment.Center) { Text(lecture.number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(lecture.title, fontWeight = FontWeight.Bold, color = Navy); Text(subject.title, fontSize = 12.sp, color = Muted); Text("${lecture.assets.size} ملفات • ${lecture.updatedAt}", fontSize = 11.sp, color = Muted) }; Icon(Icons.Default.ChevronLeft, null, tint = Muted) } } }
@Composable private fun AssetRow(asset: FileAsset, downloaded: Boolean, onDownload: () -> Unit) { Surface(shape = RoundedCornerShape(15.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(assetIcon(asset.type), null, tint = Teal, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(asset.title, fontWeight = FontWeight.Bold, color = Navy); Text("${asset.type.label} • ${asset.size}", color = Muted, fontSize = 11.sp) }; IconButton(onClick = onDownload) { Icon(if (downloaded) Icons.Default.Delete else Icons.Default.CloudDownload, if (downloaded) "حذف" else "تنزيل", tint = if (downloaded) Color(0xFFB33A3A) else Teal) } } } }
private fun assetIcon(type: AssetType) = when (type) { AssetType.PDF -> Icons.Default.PictureAsPdf; AssetType.IMAGE -> Icons.Default.Image; AssetType.AUDIO -> Icons.Default.AudioFile; AssetType.VIDEO -> Icons.Default.VideoLibrary; AssetType.QUESTIONS -> Icons.Default.Quiz; AssetType.TRANSLATION -> Icons.Default.Translate; AssetType.SUMMARY -> Icons.Default.Description }
@Composable private fun InfoCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(17.dp), verticalAlignment = Alignment.Top) { Icon(icon, null, tint = Teal, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(12.dp)); Column { Text(title, fontWeight = FontWeight.Bold, color = Navy); Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 18.sp) } } } }
@Composable private fun AdminAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { LibraryRow(title, subtitle, icon) {} }
@Composable private fun StatCard(title: String, value: String, modifier: Modifier) { Surface(modifier = modifier, shape = RoundedCornerShape(15.dp), color = Color.White) { Column(Modifier.padding(13.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Teal); Text(title, fontSize = 11.sp, color = Muted) } } }
@Composable private fun EmptyState(title: String, subtitle: String) { Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(22.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.LibraryBooks, null, tint = Color(0xFF9FB3C8), modifier = Modifier.size(38.dp)); Text(title, fontWeight = FontWeight.Bold, color = Navy, textAlign = TextAlign.Center); Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp) } } }
