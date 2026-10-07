package ir.arminniromandi.timekar.data.repository

import ir.arminniromandi.timekar.data.remote.ApiService
import ir.arminniromandi.timekar.data.remote.NetworkResult
import ir.arminniromandi.timekar.data.remote.mapper.TaskDtoMapper.toCreateTaskRequest
import ir.arminniromandi.timekar.data.remote.mapper.TaskDtoMapper.toDomain
import ir.arminniromandi.timekar.data.remote.model.ChatCompletionResponse
import ir.arminniromandi.timekar.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AiTaskRepository(
    private val apiService: ApiService
) {
    suspend fun sendRequest(userInput: String): Flow<NetworkResult<TaskItem>> = flow {
        // گام 1: نشون دادن Loading
        emit(NetworkResult.Loading)

        try {
            // گام 2: ساخت Request
            val request = userInput.toCreateTaskRequest()

            // گام 3: فرستادن به API
            val response = apiService.createTask(request)

            // گام 4: چک کردن پاسخ
            if (response.isSuccessful) {
                val task = response.body()?.toDomain()

                if (task != null) {
                    // موفقیت آمیز
                    emit(NetworkResult.Success(task))
                } else {
                    // پاسخ null بود یا parse نشد
                    emit(NetworkResult.Error("Failed to parse task from response"))
                }
            } else {
                // خطای HTTP
                emit(
                    NetworkResult.Error(
                        message = response.message() ?: "Unknown error",
                        code = response.code()
                    )
                )
            }

        } catch (e: Exception) {
            // خطای شبکه یا دیگر مشکلات
            emit(
                NetworkResult.Error(
                    message = e.message ?: "Network error occurred"
                )
            )
        }
    }

}

