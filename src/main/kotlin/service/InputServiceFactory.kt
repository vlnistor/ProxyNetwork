package service

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

const val contentType = "application/json"

data class Service(
    val inputService: InputService,
    val shutdown: () -> Unit
)

class InputServiceFactory {

    companion object{

        fun createInputService(address: String): Service {
            val httpClient = httpClient()
            val retrofit = retrofit(address, httpClient)

            return Service(retrofit.create(InputService::class.java)){ httpClient.dispatcher.executorService.shutdown() }
        }

        private fun httpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder()
                        .header("Accept", "application/json")
                    val request = builder.build()
                    chain.proceed(request)
                }
                .build()
        }

        @OptIn(ExperimentalSerializationApi::class)
        private fun retrofit(address: String, httpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(address)
                .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType.toMediaType()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build()
        }
    }
}