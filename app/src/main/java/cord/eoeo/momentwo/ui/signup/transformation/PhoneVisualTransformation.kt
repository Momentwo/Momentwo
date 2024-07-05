package cord.eoeo.momentwo.ui.signup.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var out = ""

        for (i in text.text.indices) {
            out += text.text[i]
            if (i == 2) out += "-"
            if (i == 6) out += "-"
        }

        return TransformedText(AnnotatedString(out), phoneNumberOffsetMapping)
    }

    private val phoneNumberOffsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            when (offset) {
                in 0..2 -> offset
                in 3..6 -> offset + 1
                else -> offset + 2
            }

        override fun transformedToOriginal(offset: Int): Int =
            when (offset) {
                in 0..2 -> offset
                in 3..6 -> offset - 1
                else -> offset - 2
            }
    }
}
