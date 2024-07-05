package cord.eoeo.momentwo.ui.signup.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var out = ""

        for (i in text.text.indices) {
            out += text.text[i]
            if (i == 3) out += "-"
            if (i == 5) out += "-"
        }

        return TransformedText(AnnotatedString(out), phoneNumberOffsetMapping)
    }

    private val phoneNumberOffsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            when (offset) {
                in 0..3 -> offset
                in 4..5 -> offset + 1
                else -> offset + 2
            }

        override fun transformedToOriginal(offset: Int): Int =
            when (offset) {
                in 0..3 -> offset
                in 4..5 -> offset - 1
                else -> offset - 2
            }
    }
}
