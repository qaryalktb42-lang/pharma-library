package com.pharmalibrary.app

import androidx.compose.runtime.mutableStateListOf

enum class AssetType(val label: String) {
    PDF("PDF"), SUMMARY("ملخص"), QUESTIONS("أسئلة"), TRANSLATION("ترجمة"), AUDIO("صوت"), VIDEO("فيديو"), IMAGE("صورة")
}

data class StudyYear(val id: String, val title: String, val description: String)
data class Semester(val id: String, val yearId: String, val title: String, val description: String)
data class Subject(val id: String, val semesterId: String, val title: String, val lecturer: String, val description: String)
data class AcademicBatch(val id: String, val subjectId: String, val title: String)
data class FileAsset(val id: String, val lectureId: String, val title: String, val type: AssetType, val size: String, val description: String = "")
data class Lecture(
    val id: String,
    val subjectId: String,
    val batchId: String,
    val number: Int,
    val title: String,
    val description: String,
    val updatedAt: String,
    val assets: List<FileAsset>
)

class LocalLibraryRepository {
    val years = listOf(
        StudyYear("y1", "السنة الأولى", "الأساسيات والعلوم الدوائية"),
        StudyYear("y2", "السنة الثانية", "العلوم الطبية والدوائية المتقدمة"),
        StudyYear("y3", "السنة الثالثة", "الصيدلة السريرية والتطبيقات"),
        StudyYear("y4", "السنة الرابعة", "التدريب والمواد التخصصية")
    )

    val semesters = listOf(
        Semester("s1", "y1", "الفصل الأول", "المقررات الأساسية"),
        Semester("s2", "y1", "الفصل الثاني", "مقررات السنة الأولى المتقدمة"),
        Semester("s3", "y2", "الفصل الأول", "العلوم الطبية"),
        Semester("s4", "y2", "الفصل الثاني", "علم الأدوية والعلاجيات")
    )

    val subjects = listOf(
        Subject("sub1", "s1", "الكيمياء الصيدلانية العامة", "د. أحمد علي", "مبادئ الكيمياء اللازمة لدراسة المركبات الدوائية."),
        Subject("sub2", "s1", "علم التشريح", "د. سارة محمد", "مدخل إلى تشريح جسم الإنسان ووظائف الأعضاء."),
        Subject("sub3", "s2", "الحسابات الصيدلانية", "د. عمر خالد", "الحسابات والتركيزات والتحويلات في الصيدلة."),
        Subject("sub4", "s3", "علم الأحياء الدقيقة", "د. ليان حسن", "البكتيريا والفيروسات ومبادئ التعقيم."),
        Subject("sub5", "s4", "علم الأدوية", "د. مصطفى ناصر", "آليات عمل الأدوية وتطبيقاتها العلاجية.")
    )

    val batches = listOf(
        AcademicBatch("b39", "sub1", "الدفعة 39"),
        AcademicBatch("b40", "sub1", "الدفعة 40"),
        AcademicBatch("b39-sub2", "sub2", "الدفعة 39"),
        AcademicBatch("b40-sub3", "sub3", "الدفعة 40"),
        AcademicBatch("b41-sub4", "sub4", "الدفعة 41"),
        AcademicBatch("b41-sub5", "sub5", "الدفعة 41")
    )

    val lectures = listOf(
        Lecture("l1", "sub1", "b39", 1, "مقدمة في الكيمياء العضوية", "المفاهيم الأساسية والروابط الكيميائية في المركبات الدوائية.", "03 نوفمبر 2026", listOf(
            FileAsset("f1", "l1", "المحاضرة الأصلية.pdf", AssetType.PDF, "2.4 MB"),
            FileAsset("f2", "l1", "ملخص المحاضرة", AssetType.SUMMARY, "340 KB"),
            FileAsset("f3", "l1", "أسئلة مراجعة", AssetType.QUESTIONS, "180 KB")
        )),
        Lecture("l2", "sub1", "b39", 2, "المجموعات الوظيفية", "مراجعة المجموعات الوظيفية وأهميتها في تصميم الدواء.", "02 نوفمبر 2026", listOf(
            FileAsset("f4", "l2", "المحاضرة الأصلية.pdf", AssetType.PDF, "3.1 MB"),
            FileAsset("f5", "l2", "الترجمة العربية.pdf", AssetType.TRANSLATION, "1.8 MB")
        )),
        Lecture("l3", "sub2", "b39-sub2", 1, "الجهاز الهيكلي", "العظام والمفاصل والمصطلحات التشريحية الأساسية.", "01 نوفمبر 2026", listOf(
            FileAsset("f6", "l3", "ملزمة التشريح.pdf", AssetType.PDF, "5.2 MB"),
            FileAsset("f7", "l3", "صور توضيحية", AssetType.IMAGE, "1.1 MB")
        )),
        Lecture("l4", "sub3", "b40-sub3", 1, "الجرعات والتركيزات", "تطبيقات عملية على حساب الجرعات والتركيزات.", "30 أكتوبر 2026", listOf(
            FileAsset("f8", "l4", "ملخص الحسابات.pdf", AssetType.PDF, "1.6 MB"),
            FileAsset("f9", "l4", "تسجيل صوتي", AssetType.AUDIO, "8.4 MB")
        )),
        Lecture("l5", "sub5", "b41-sub5", 1, "مقدمة في علم الأدوية", "مفاهيم الامتصاص والتوزيع والاستقلاب والإطراح.", "28 أكتوبر 2026", listOf(
            FileAsset("f10", "l5", "المحاضرة الأصلية.pdf", AssetType.PDF, "4.7 MB"),
            FileAsset("f11", "l5", "فيديو شرح", AssetType.VIDEO, "24 MB")
        ))
    )

    val favoriteIds = mutableStateListOf<String>()
    val downloadedIds = mutableStateListOf<String>()

    fun year(id: String) = years.first { it.id == id }
    fun semester(id: String) = semesters.first { it.id == id }
    fun subject(id: String) = subjects.first { it.id == id }
    fun batch(id: String) = batches.first { it.id == id }
    fun lecturesForSubject(subjectId: String) = lectures.filter { it.subjectId == subjectId }
    fun lecturesForBatch(batchId: String) = lectures.filter { it.batchId == batchId }.sortedBy { it.number }
    fun assetsForLecture(lectureId: String) = lectures.first { it.id == lectureId }.assets
    fun isFavorite(id: String) = id in favoriteIds
    fun toggleFavorite(id: String) { if (!favoriteIds.remove(id)) favoriteIds.add(id) }
    fun isDownloaded(id: String) = id in downloadedIds
    fun toggleDownloaded(id: String) { if (!downloadedIds.remove(id)) downloadedIds.add(id) }

    fun search(query: String): List<Lecture> {
        val q = normalize(query)
        if (q.isBlank()) return emptyList()
        return lectures.filter { lecture ->
            val subject = subject(lecture.subjectId)
            val batch = batch(lecture.batchId)
            listOf(lecture.title, lecture.description, lecture.number.toString(), subject.title, batch.title)
                .any { normalize(it).contains(q) }
        }
    }

    private fun normalize(value: String): String = value
        .lowercase()
        .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
        .replace("ة", "ه").replace("ى", "ي")
        .replace(Regex("[ًٌٍَُِّْـ]"), "")
}
