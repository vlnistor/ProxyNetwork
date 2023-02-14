package service

import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.util.concurrent.atomic.AtomicInteger

class MockInputService : InputService{

    var totalRequests = AtomicInteger(0)
    var activeRequests = AtomicInteger(0)

    override suspend fun call(input: String): Response<APIResponse> {
        totalRequests.incrementAndGet()
        activeRequests.incrementAndGet()
        val response =  when(input){
            "good" -> {
                Response.success(APIResponse("information"))
            }
            "hold" -> {
                delay(100000)
                Response.success(APIResponse("information"))
            }
            "retry" -> {
                Response.error(503, "Service Unavailable".toResponseBody("text/plain".toMediaTypeOrNull()!!))
            }
            else -> {
                Response.error(500, "Internal Server Error".toResponseBody("text/plain".toMediaTypeOrNull()!!))
            }
        }
        activeRequests.decrementAndGet()
        return response
    }

}