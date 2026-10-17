package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PrescriptionSummaryEntity
import com.example.engine.ClinicalRulesEngine
import com.example.model.Language
import com.example.model.PrescriptionItem
import com.example.model.SavedPrescriptionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PrescriptionRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("samjha_rx_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getDatabase(context)
    private val summaryDao = database.prescriptionSummaryDao()

    companion object {
        private const val KEY_PIN = "local_lock_pin"
        private const val KEY_IS_LOCKED = "is_account_locked"
        private const val KEY_LANGUAGE = "patient_preferred_language"
        private const val KEY_SAVED_PRESCRIPTIONS = "saved_prescriptions_json"
        private const val KEY_FIRST_LAUNCH = "first_launch_done"
    }

    fun isFirstLaunch(): Boolean {
        val first = !prefs.getBoolean(KEY_FIRST_LAUNCH, false)
        if (first) {
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply()
        }
        return first
    }

    // --- Language Preference ---
    fun getSavedLanguage(): Language {
        val code = prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code) ?: Language.ENGLISH.code
        return Language.values().firstOrNull { it.code == code } ?: Language.ENGLISH
    }

    fun setLanguage(language: Language) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    // --- Local Lock Management ---
    fun hasLockPin(): Boolean {
        val pin = prefs.getString(KEY_PIN, null)
        return !pin.isNullOrBlank()
    }

    fun setLockPin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_IS_LOCKED, false)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val saved = prefs.getString(KEY_PIN, null)
        return saved == pin
    }

    fun isAppLocked(): Boolean {
        return hasLockPin() && prefs.getBoolean(KEY_IS_LOCKED, false)
    }

    fun setAppLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOCKED, locked).apply()
    }

    fun removeLockPin() {
        prefs.edit()
            .remove(KEY_PIN)
            .putBoolean(KEY_IS_LOCKED, false)
            .apply()
    }

    // --- Saved Prescriptions Storage (Room backed with SharedPreferences fallback) ---
    fun getSavedPrescriptions(): List<SavedPrescriptionRecord> {
        val jsonStr = prefs.getString(KEY_SAVED_PRESCRIPTIONS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<SavedPrescriptionRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val itemsJson = obj.getJSONArray("items")
                val itemsList = mutableListOf<PrescriptionItem>()
                for (j in 0 until itemsJson.length()) {
                    val itObj = itemsJson.getJSONObject(j)
                    itemsList.add(
                        PrescriptionItem(
                            id = itObj.optString("id", "it_$j"),
                            brandName = itObj.optString("brandName", ""),
                            genericName = itObj.optString("genericName", ""),
                            strength = itObj.optString("strength", ""),
                            dose = itObj.optString("dose", ""),
                            frequency = itObj.optString("frequency", ""),
                            duration = itObj.optString("duration", ""),
                            specialInstructions = itObj.optString("specialInstructions", ""),
                            confidence = itObj.optDouble("confidence", 0.95).toFloat(),
                            isConfirmed = itObj.optBoolean("isConfirmed", true),
                            hasAmbiguity = itObj.optBoolean("hasAmbiguity", false),
                            ambiguityReason = if (itObj.has("ambiguityReason") && !itObj.isNull("ambiguityReason")) itObj.getString("ambiguityReason") else null
                        )
                    )
                }

                val checks = ClinicalRulesEngine.evaluate(itemsList, com.example.model.PatientContext())
                val isBlocked = ClinicalRulesEngine.isScheduleBlocked(itemsList, checks)

                list.add(
                    SavedPrescriptionRecord(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        timestamp = obj.getLong("timestamp"),
                        items = itemsList,
                        checks = checks,
                        scheduleBlocked = isBlocked,
                        patientLanguage = Language.values().firstOrNull { it.code == obj.optString("language", "en") } ?: Language.ENGLISH,
                        pharmacistNotes = obj.optString("pharmacistNotes", ""),
                        isPharmacistSigned = obj.optBoolean("isPharmacistSigned", false)
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun savePrescription(record: SavedPrescriptionRecord) = withContext(Dispatchers.IO) {
        // 1. Save to Room database (excluded from cloud backup)
        val itemsArr = JSONArray()
        record.items.forEach { item ->
            val itObj = JSONObject()
            itObj.put("id", item.id)
            itObj.put("brandName", item.brandName)
            itObj.put("genericName", item.genericName)
            itObj.put("strength", item.strength)
            itObj.put("dose", item.dose)
            itObj.put("frequency", item.frequency)
            itObj.put("duration", item.duration)
            itObj.put("specialInstructions", item.specialInstructions)
            itObj.put("confidence", item.confidence)
            itObj.put("isConfirmed", item.isConfirmed)
            itObj.put("hasAmbiguity", item.hasAmbiguity)
            itObj.put("ambiguityReason", item.ambiguityReason ?: "")
            itemsArr.put(itObj)
        }

        val checksArr = JSONArray()
        record.checks.forEach { check ->
            val cObj = JSONObject()
            cObj.put("ruleId", check.ruleId)
            cObj.put("severity", check.severity.name)
            cObj.put("status", check.status.name)
            cObj.put("title", check.title)
            checksArr.put(cObj)
        }

        val entity = PrescriptionSummaryEntity(
            id = record.id,
            title = record.title,
            timestamp = record.timestamp,
            itemsJson = itemsArr.toString(),
            checksJson = checksArr.toString(),
            scheduleBlocked = record.scheduleBlocked,
            patientLanguage = record.patientLanguage.code,
            pharmacistNotes = record.pharmacistNotes,
            isPharmacistSigned = record.isPharmacistSigned
        )
        summaryDao.insertSummary(entity)

        // 2. Synchronize prefs cache
        val existing = getSavedPrescriptions().toMutableList()
        existing.removeAll { it.id == record.id }
        existing.add(0, record)
        persistPrescriptions(existing)
    }

    suspend fun deletePrescription(id: String) = withContext(Dispatchers.IO) {
        summaryDao.deleteSummaryById(id)
        val existing = getSavedPrescriptions().toMutableList()
        existing.removeAll { it.id == id }
        persistPrescriptions(existing)
    }

    suspend fun clearAllPrescriptions() = withContext(Dispatchers.IO) {
        summaryDao.deleteAllSummaries()
        prefs.edit().remove(KEY_SAVED_PRESCRIPTIONS).apply()
    }

    private fun persistPrescriptions(list: List<SavedPrescriptionRecord>) {
        val array = JSONArray()
        list.forEach { rec ->
            val obj = JSONObject()
            obj.put("id", rec.id)
            obj.put("title", rec.title)
            obj.put("timestamp", rec.timestamp)
            obj.put("language", rec.patientLanguage.code)
            obj.put("pharmacistNotes", rec.pharmacistNotes)
            obj.put("isPharmacistSigned", rec.isPharmacistSigned)

            val itemsArr = JSONArray()
            rec.items.forEach { item ->
                val itObj = JSONObject()
                itObj.put("id", item.id)
                itObj.put("brandName", item.brandName)
                itObj.put("genericName", item.genericName)
                itObj.put("strength", item.strength)
                itObj.put("dose", item.dose)
                itObj.put("frequency", item.frequency)
                itObj.put("duration", item.duration)
                itObj.put("specialInstructions", item.specialInstructions)
                itObj.put("confidence", item.confidence)
                itObj.put("isConfirmed", item.isConfirmed)
                itObj.put("hasAmbiguity", item.hasAmbiguity)
                itObj.put("ambiguityReason", item.ambiguityReason ?: "")
                itemsArr.put(itObj)
            }
            obj.put("items", itemsArr)
            array.put(obj)
        }
        prefs.edit().putString(KEY_SAVED_PRESCRIPTIONS, array.toString()).apply()
    }
}
