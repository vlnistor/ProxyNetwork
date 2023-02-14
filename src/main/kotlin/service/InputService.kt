package service

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface InputService {
    @GET("data")
    suspend fun call(
        @Query("input") input: String
    ): Response<APIResponse>
}

@Serializable
data class APIResponse(
    val information: String
)