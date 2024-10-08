package cord.eoeo.momentwo.ui.model

data class TextFieldDialogItem(
    val titleText: String,
    val description: String,
    val onConfirm: (String) -> Unit,
    val initialText: String = "",
)
