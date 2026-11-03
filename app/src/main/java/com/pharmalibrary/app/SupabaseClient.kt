package com.pharmalibrary.app

import com.pharmalibrary.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

object SupabaseConnection {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()

    val client: SupabaseClient? by lazy {
        if (!isConfigured) return@lazy null
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Postgrest)
        }
    }
}

@Serializable
data class StudyYearRemote(
    val id: String,
    val title: String,
    val description: String? = null,
    val sort_order: Int = 0,
    val is_visible: Boolean = true
)

class SupabaseLibraryRepository {
    suspend fun loadVisibleYears(): Result<List<StudyYearRemote>> {
        val supabase = SupabaseConnection.client
            ?: return Result.failure(IllegalStateException("Supabase is not configured"))
        return runCatching {
            supabase.from("study_years").select {
                filter { eq("is_visible", true) }
            }.decodeList<StudyYearRemote>()
        }
    }
}
