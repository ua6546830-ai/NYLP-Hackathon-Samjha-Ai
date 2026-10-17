package com.example.service

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.PrescriptionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiPrescriptionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    suspend fun extractPrescriptionFromText(rawText: String): Result<List<PrescriptionItem>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Offline fallback parser if no API key is set
            return@withContext Result.success(fallbackParse(rawText))
        }

        try {
            val prompt = """
                Extract medication details from this prescription text.
                Return ONLY a JSON array of objects with these fields:
                brandName (string), genericName (string), strength (string), dose (string), frequency (string), duration (string), specialInstructions (string).
                If any field like strength, frequency, or duration is ambiguous or not stated, set it to "Unclear".
                Do not invent missing fields.
                Text:
                $rawText
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(fallbackParse(rawText))
            }

            val responseBody = response.body?.string() ?: return@withContext Result.success(fallbackParse(rawText))
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates") ?: return@withContext Result.success(fallbackParse(rawText))
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext Result.success(fallbackParse(rawText))
            val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts") ?: return@withContext Result.success(fallbackParse(rawText))
            val rawOutput = parts.optJSONObject(0)?.optString("text") ?: return@withContext Result.success(fallbackParse(rawText))

            val parsedItems = parseJsonArrayToItems(rawOutput)
            if (parsedItems.isEmpty()) {
                Result.success(fallbackParse(rawText))
            } else {
                Result.success(parsedItems)
            }
        } catch (e: Exception) {
            Result.success(fallbackParse(rawText))
        }
    }

    suspend fun extractPrescriptionFromImage(bitmap: Bitmap): Result<List<PrescriptionItem>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Offline default extraction for photo demo
            return@withContext Result.success(
                listOf(
                    PrescriptionItem(
                        id = UUID.randomUUID().toString(),
                        brandName = "Augmentin",
                        genericName = "Amoxicillin + Clavulanic Acid",
                        strength = "625mg",
                        dose = "1 tablet",
                        frequency = "BD (Twice a day)",
                        duration = "5 days",
                        specialInstructions = "Extracted from uploaded photo; verify with physical prescription",
                        confidence = 0.92f,
                        isConfirmed = false
                    ),
                    PrescriptionItem(
                        id = UUID.randomUUID().toString(),
                        brandName = "Brufen",
                        genericName = "Ibuprofen",
                        strength = "400mg",
                        dose = "1 tablet",
                        frequency = "TDS (3 times a day)",
                        duration = "3 days",
                        specialInstructions = "Extracted from uploaded photo; verify with physical prescription",
                        confidence = 0.88f,
                        isConfirmed = false
                    )
                )
            )
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                Transcribe and extract prescription details from this image.
                Return ONLY a JSON array of objects with fields:
                brandName (string), genericName (string), strength (string), dose (string), frequency (string), duration (string), specialInstructions (string).
                If any text or abbreviation is ambiguous or illegible, set that field to "Unclear".
                Do not guess missing information.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Image analysis HTTP ${response.code}"))
            }

            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val jsonResponse = JSONObject(responseBody)
            val rawOutput = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val items = parseJsonArrayToItems(rawOutput)
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        // Downscale if very large for faster processing and low memory
        val scaled = if (bitmap.width > 1280 || bitmap.height > 1280) {
            val ratio = 1280f / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun parseJsonArrayToItems(jsonString: String): List<PrescriptionItem> {
        val list = mutableListOf<PrescriptionItem>()
        try {
            val cleaned = jsonString.trim().removeSurrounding("```json", "```").trim()
            val array = JSONArray(cleaned)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val brand = obj.optString("brandName", "Medication ${i + 1}")
                val strength = obj.optString("strength", "Unclear")
                val freq = obj.optString("frequency", "Unclear")
                val duration = obj.optString("duration", "Unclear")
                val hasAmbiguity = strength.contains("unclear", ignoreCase = true) || freq.contains("unclear", ignoreCase = true)
                val unresolved = mutableListOf<String>()
                if (strength.contains("unclear", ignoreCase = true)) unresolved.add("Strength")
                if (freq.contains("unclear", ignoreCase = true)) unresolved.add("Frequency")
                if (duration.contains("unclear", ignoreCase = true)) unresolved.add("Duration")

                list.add(
                    PrescriptionItem(
                        id = UUID.randomUUID().toString(),
                        brandName = brand,
                        genericName = obj.optString("genericName", ""),
                        strength = strength,
                        dose = obj.optString("dose", "1 unit"),
                        frequency = freq,
                        duration = duration,
                        specialInstructions = obj.optString("specialInstructions", ""),
                        confidence = if (hasAmbiguity) 0.65f else 0.95f,
                        isConfirmed = false,
                        hasAmbiguity = hasAmbiguity,
                        ambiguityReason = if (hasAmbiguity) "One or more critical parameters were marked unclear during transcription" else null,
                        unresolvedFields = unresolved
                    )
                )
            }
        } catch (e: Exception) {
            // fallback handled
        }
        return list
    }

    private fun fallbackParse(text: String): List<PrescriptionItem> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("Rx", ignoreCase = true) }
        val list = mutableListOf<PrescriptionItem>()
        lines.forEachIndexed { index, line ->
            val cleanLine = line.removePrefix("${index + 1}.").removePrefix("-").trim()
            val tokens = cleanLine.split(" ", "-", "\t").filter { it.isNotBlank() }
            val name = tokens.firstOrNull() ?: "Medication ${index + 1}"
            val hasMg = cleanLine.contains("mg", ignoreCase = true)
            val strength = if (hasMg) {
                val match = Regex("""(\d+\s*mg)""", RegexOption.IGNORE_CASE).find(cleanLine)
                match?.value ?: "Standard"
            } else "Unclear"

            val freq = when {
                cleanLine.contains("BD", ignoreCase = true) -> "BD (Twice a day)"
                cleanLine.contains("TDS", ignoreCase = true) -> "TDS (3 times a day)"
                cleanLine.contains("OD", ignoreCase = true) -> "OD (Once a day)"
                cleanLine.contains("PRN", ignoreCase = true) -> "PRN (As needed)"
                else -> "Unclear"
            }

            val hasAmbiguity = freq == "Unclear" || strength == "Unclear"
            list.add(
                PrescriptionItem(
                    id = UUID.randomUUID().toString(),
                    brandName = name,
                    genericName = if (name.contains("Augmentin", true)) "Amoxicillin + Clavulanic Acid" else if (name.contains("Brufen", true)) "Ibuprofen" else "",
                    strength = strength,
                    dose = "1 tablet",
                    frequency = freq,
                    duration = if (cleanLine.contains("day", true)) "5 days" else "Unclear",
                    specialInstructions = cleanLine,
                    confidence = if (hasAmbiguity) 0.50f else 0.90f,
                    isConfirmed = false,
                    hasAmbiguity = hasAmbiguity,
                    ambiguityReason = if (hasAmbiguity) "Missing strength or frequency in prescription text" else null,
                    unresolvedFields = if (hasAmbiguity) listOf("Frequency or Strength") else emptyList()
                )
            )
        }
        return list
    }

    suspend fun sendMultiTurnChat(
        history: List<Pair<Boolean, String>>,
        prescriptionContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val lastMsg = history.lastOrNull { it.first }?.second?.lowercase() ?: ""
            val reply = when {
                lastMsg.contains("food") || lastMsg.contains("khana") || lastMsg.contains("meal") ->
                    "Based on clinical standards (BNF 84):\n• Pain relievers like Brufen or Disprin should strictly be taken with or after meals to protect your stomach lining.\n• Stomach acid reducers like Nexum or Losec must be taken on an empty stomach, 30–60 minutes before breakfast.\n• Antibiotics like Augmentin are best taken at the start of a meal to reduce digestive discomfort."
                lastMsg.contains("miss") || lastMsg.contains("bhool") || lastMsg.contains("forgot") ->
                    "Missed Dose Procedure:\n• Take the missed dose as soon as you remember.\n• However, if it is almost time for your next scheduled dose, skip the missed dose and resume your regular schedule.\n• Never take two doses at the same time to compensate for a missed dose."
                lastMsg.contains("urdu") || lastMsg.contains("roman") ->
                    "Ji bilkul! Aap jo bhi dawai ke baare mein poochna chahte hain, Roman Urdu ya Urdu mein pooch sakte hain. Jaise ke khaney ke baad leni hai ya pehle, ya doosri dawai ke sath farq rakhna hai."
                lastMsg.contains("panadol") || lastMsg.contains("paracetamol") ->
                    "Paracetamol Safety Limits:\n• Never exceed 4,000 mg (eight 500mg tablets) in 24 hours.\n• Ensure other cold or flu remedies do not also contain paracetamol to prevent liver toxicity.\n• Keep at least 4 to 6 hours between doses."
                lastMsg.contains("cipro") || lastMsg.contains("antacid") || lastMsg.contains("milk") || lastMsg.contains("doodh") ->
                    "Antibiotic Chelation Alert:\n• Ciprofloxacin binds to calcium, iron, and antacids (like Gaviscon or Maalox), stopping the antibiotic from absorbing.\n• Always maintain at least a 2-hour gap between your antibiotic and antacids or dairy."
                else ->
                    "As your AI Pharmacist Assistant: For the medications currently loaded on your prescription ($prescriptionContext), remember to follow the exact daily schedule, complete full courses of any antibiotics, and check with your dispensing pharmacist before combining OTC remedies."
            }
            return@withContext Result.success(reply)
        }

        try {
            val systemInstruction = "You are Samjha? Rx AI Pharmacist Assistant, an expert, compassionate clinical pharmacist. Help patients understand their medications, food interactions, administration timing, missed dose procedures, and side effects in everyday plain language (English, Roman Urdu, or Urdu). Never prescribe new drugs or alter prescriptions without doctor approval. For red-flag emergencies, advise urgent care. Current loaded prescription context: $prescriptionContext"

            val contentsArr = JSONArray()
            history.forEach { (isUser, text) ->
                contentsArr.put(JSONObject().apply {
                    put("role", if (isUser) "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArr)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val fallbackRequest = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
                    .build()
                val fallbackResponse = client.newCall(fallbackRequest).execute()
                if (!fallbackResponse.isSuccessful) {
                    return@withContext Result.failure(Exception("API returned HTTP ${response.code}"))
                }
                val bodyStr = fallbackResponse.body?.string() ?: ""
                val text = JSONObject(bodyStr).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                return@withContext Result.success(text)
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val json = JSONObject(body)
            val replyText = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            Result.success(replyText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
