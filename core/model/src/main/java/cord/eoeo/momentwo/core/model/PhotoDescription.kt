package cord.eoeo.momentwo.core.model

data class PhotoDescription(val title: String, val body: String) {
    fun encode(): String = if(title.isEmpty() && body.isEmpty()) "" else "$title\n$body"
    companion object { fun read(raw: String): PhotoDescription { val lines=raw.lines();return PhotoDescription(lines.first(),lines.drop(1).joinToString("\n")) } }
}
