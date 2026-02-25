package com.poolshark.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.poolshark.domain.model.CalibrationData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "calibration")

@Singleton
class CalibrationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val HOMOGRAPHY_KEY = stringPreferencesKey("homography_matrix")
    private val CORNERS_KEY = stringPreferencesKey("table_corners")
    private val THRESHOLDS_KEY = stringPreferencesKey("confidence_thresholds")

    val calibrationData: Flow<CalibrationData> = context.dataStore.data.map { prefs ->
        val homography = prefs[HOMOGRAPHY_KEY]?.split(",")
            ?.map { it.toFloat() }?.toFloatArray() ?: FloatArray(9)
        val corners = prefs[CORNERS_KEY]?.split(";")?.map {
            val (x, y) = it.split(","); x.toFloat() to y.toFloat()
        } ?: emptyList()
        val thresholds = prefs[THRESHOLDS_KEY]?.split(";")?.associate {
            val (k, v) = it.split(":"); k to v.toFloat()
        } ?: emptyMap()
        CalibrationData(homography, thresholds, corners)
    }

    suspend fun save(data: CalibrationData) {
        context.dataStore.edit { prefs ->
            prefs[HOMOGRAPHY_KEY] = data.homographyMatrix.joinToString(",")
            prefs[CORNERS_KEY] = data.tableCorners.joinToString(";") { "${it.first},${it.second}" }
            prefs[THRESHOLDS_KEY] = data.confidenceThresholds.entries.joinToString(";") { "${it.key}:${it.value}" }
        }
    }
}
