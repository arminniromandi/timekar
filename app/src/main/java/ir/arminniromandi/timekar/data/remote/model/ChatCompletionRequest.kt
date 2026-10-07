package ir.arminniromandi.timekar.data.remote.model



import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val response_format: ResponseFormat
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val type: String
)