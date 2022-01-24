package kr.co.greentech.dataloggerapp.util.objects

import android.content.Context
import android.content.SharedPreferences
import kr.co.greentech.dataloggerapp.application.DataLogApplication

/**
 *
 * 데이터 저장 및 로드 클래스
 *
 */
object PreferenceManager {

    const val PREFERENCES_NAME = "rebuild_preference"
    const val DEFAULT_VALUE_STRING = ""
    const val DEFAULT_VALUE_BOOLEAN = false
    const val DEFAULT_VALUE_INT = -1
    const val DEFAULT_VALUE_LONG = -1L
    const val DEFAULT_VALUE_FLOAT = -1f
    
    lateinit var context: DataLogApplication
    
    fun setApplicationContext(context: DataLogApplication) {
        this.context = context
    }

    private fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    /**
     * String 값 저장
     * @param context
     * @param key
     * @param value
     */
    fun setString(key: String?, value: String?) {
        val prefs = getPreferences()
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * boolean 값 저장
     * @param context
     * @param key
     * @param value
     */
    fun setBoolean(key: String?, value: Boolean) {
        val prefs = getPreferences()
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * int 값 저장
     * @param context
     * @param key
     * @param value
     */
    fun setInt(key: String?, value: Int) {
        val prefs = getPreferences()
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * long 값 저장
     * @param context
     * @param key
     * @param value
     */
    fun setLong(key: String?, value: Long) {
        val prefs = getPreferences()
        val editor = prefs.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    /**
     * float 값 저장
     * @param context
     * @param key
     * @param value
     */
    fun setFloat(key: String?, value: Float) {
        val prefs = getPreferences()
        val editor = prefs.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    /**
     * String 값 로드
     * @param context
     * @param key
     * @return
     */
    fun getString(key: String?): String {
        val prefs = getPreferences()
        val data = prefs.getString(key, DEFAULT_VALUE_STRING)
        return data ?: PreferenceManager.DEFAULT_VALUE_STRING
    }

    /**
     * boolean 값 로드
     * @param context
     * @param key
     * @return
     */
    fun getBoolean(key: String?, default: Boolean = DEFAULT_VALUE_BOOLEAN): Boolean {
        val prefs = getPreferences()
        return prefs.getBoolean(key, default)
    }

    /**
     * int 값 로드
     * @param context
     * @param key
     * @return
     */
    fun getInt(key: String?): Int {
        val prefs = getPreferences()
        return prefs.getInt(key, DEFAULT_VALUE_INT)
    }

    /**
     * long 값 로드
     * @param context
     * @param key
     * @return
     */
    fun getLong(key: String?): Long {
        val prefs = getPreferences()
        return prefs.getLong(key, DEFAULT_VALUE_LONG)
    }

    /**
     * float 값 로드
     * @param context
     * @param key
     * @return
     */
    fun getFloat(key: String?): Float {
        val prefs = getPreferences()
        return prefs.getFloat(key, DEFAULT_VALUE_FLOAT)
    }

    /**
     * 키 값 삭제
     * @param context
     * @param key
     */
    fun removeKey(key: String?) {
        val prefs = getPreferences()
        val edit = prefs.edit()
        edit.remove(key)
        edit.apply()
    }

    /**
     * 모든 저장 데이터 삭제
     * @param context
     */
    fun clear() {
        val prefs = getPreferences()
        val edit = prefs.edit()
        edit.clear()
        edit.apply()
    }
}