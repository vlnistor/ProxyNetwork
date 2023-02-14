package service

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MockInputServiceRecovers: InputService {

    var alternate = AtomicBoolean(false)
    var totalRequests = AtomicInteger(0)

    override suspend fun call(input: String): Response<APIResponse> {
        totalRequests.incrementAndGet()
        return if(alternate.get()){
            alternate.set(false)
            Response.success(APIResponse("information"))
        } else {
            alternate.set(true)
            Response.error(503, "Service Unavailable".toResponseBody("text/plain".toMediaTypeOrNull()!!))
        }
    }


}