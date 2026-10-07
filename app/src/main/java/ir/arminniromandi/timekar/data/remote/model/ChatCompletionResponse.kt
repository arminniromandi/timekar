package ir.arminniromandi.timekar.data.remote.model

import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for Task API responses
 * Following Single Responsibility Principle - handles only API data mapping
 */


@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val choices: List<Choice>
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: ResponseMessage
)

@JsonClass(generateAdapter = true)
data class ResponseMessage(
    val content: String
)