package com.example.marcacion.data.dto.dataSource

import android.content.Context
import android.content.SharedPreferences
import com.example.marcacion.data.utils.Constants.ID_USER

fun saveIdUser(context: Context, token: String?) {

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE)

    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    editor.putString(ID_USER, token)

    editor.apply()
}

fun getIdUser(context: Context): String? {

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE)

    return sharedPreferences.getString(ID_USER, null)
}
