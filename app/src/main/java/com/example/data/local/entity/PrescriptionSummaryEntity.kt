package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescription_summaries")
data class PrescriptionSummaryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val timestamp: Long,
    val itemsJson: String,
    val checksJson: String,
    val scheduleBlocked: Boolean,
    val patientLanguage: String,
    val pharmacistNotes: String = "",
    val isPharmacistSigned: Boolean = false,
    val imageUri: String? = null
)
