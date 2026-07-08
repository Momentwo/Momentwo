package cord.eoeo.momentwo.core.model

data class TextFieldDialogItem(
    val titleText: String,
    val description: String,
    val onConfirm: (String) -> Unit,
    val placeholder: String = "",
)
