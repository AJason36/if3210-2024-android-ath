package com.ath.bondoman.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

object NumberFormatUtils {
    fun formatNumberField(field: EditText) {
        field.addTextChangedListener(object : TextWatcher {
            var current = ""

            override fun afterTextChanged(s: Editable?) {
                field.removeTextChangedListener(this)

                val cleanString = s.toString().replace(",", "")

                // Check if the string is empty
                if (cleanString.isEmpty()) {
                    current = ""
                    field.setText("")
                }
                // Check if the last character is a "."
                else if (cleanString.last() == '.') {
                    current = cleanString
                    field.setText(cleanString)
                    field.setSelection(cleanString.length)
                } else {
                    val parts = cleanString.split(".")
                    val formatted = StringBuilder(parts[0].reversed().chunked(3).joinToString(",").reversed())
                    if (parts.size > 1) {
                        formatted.append(".${parts[1]}")
                    }

                    current = formatted.toString()
                    field.setText(formatted.toString())
                    field.setSelection(formatted.length)
                }

                field.addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun formatNumberString(numberString: String): String {
        val cleanString = numberString.replace(",", "")
        val parts = cleanString.split(".")
        val formatted = StringBuilder(parts[0].reversed().chunked(3).joinToString(",").reversed())
        if (parts.size > 1 && parts[1].toInt() != 0) {
            formatted.append(".${parts[1].trimEnd { it == '0' }}")
        }
        return formatted.toString()
    }

    fun removeThousandsSeparator(value: String): String {
        return value.replace(",", "")
    }
}



