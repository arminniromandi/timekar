package ir.arminniromandi.timekar.data.remote

import ir.arminniromandi.timekar.data.remote.model.ChatCompletionRequest
import ir.arminniromandi.timekar.data.remote.model.ChatCompletionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API Service interface for network requests
 * Following Interface Segregation Principle - keeping API definitions minimal and focused
 */
interface ApiService {

    @POST("chat/completions")
    suspend fun createTask(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

}
