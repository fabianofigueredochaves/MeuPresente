package com.example.meupresente.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Uma VisualTransformation para formatar a entrada de data no formato DD/MM/YYYY.
 * A entrada interna (raw) é apenas numérica (DDMMYYYY).
 * A saída visual (transformed) insere os caracteres '/'.
 */
class DateInputVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Limita a entrada a no máximo 8 dígitos (DDMMYYYY) para o formato raw.
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""

        // Constrói a string formatada visualmente.
        // DD/MM/YYYY
        // 0123456789
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) { // Após o 2º (índice 1) e o 4º (índice 3) dígito, insere '/'
                out += "/"
            }
        }

        // OffsetMapping é crucial para o cursor funcionar corretamente.
        // Ele mapeia as posições do offset do texto original para o texto transformado e vice-versa.
        val dateOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Para offset original 0-1 (dia): offset + 0 slashes
                if (offset <= 1) return offset
                // Para offset original 2-3 (mês): offset + 1 slash
                if (offset <= 3) return offset + 1 // Adiciona 1 pelo primeiro '/'
                // Para offset original 4-8 (ano): offset + 2 slashes
                if (offset <= 8) return offset + 2 // Adiciona 2 pelos dois '/'
                return 10 // Posição máxima no texto transformado (DD/MM/YYYY é 10 caracteres)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Para offset transformado 0-2 (DD/): offset + 0 slashes
                if (offset <= 2) return offset
                // Para offset transformado 3-5 (MM/): offset - 1 slash
                if (offset <= 5) return offset - 1 // Remove 1 pelo primeiro '/'
                // Para offset transformado 6-10 (YYYY): offset - 2 slashes
                if (offset <= 10) return offset - 2 // Remove 2 pelos dois '/'
                return 8 // Posição máxima no texto original (DDMMYYYY é 8 caracteres)
            }
        }

        return TransformedText(AnnotatedString(out), dateOffsetTranslator)
    }
}