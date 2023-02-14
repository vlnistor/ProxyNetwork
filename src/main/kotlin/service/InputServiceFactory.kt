package service

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory



class InputServiceFactory {

    companion object{

        @OptIn(ExperimentalSerializationApi::class)
        fun createInputService(address: String): Pair<InputService, () -> Unit> {
            val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder()
                        .header("Accept", "application/json")
                    val request = builder.build()
                    chain.proceed(request)
                }
                .build()

            val contentType = "application/json".toMediaType()


            val retrofit = Retrofit.Builder()
                .baseUrl(address)
                .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build()
            return Pair(retrofit.create(InputService::class.java)){ httpClient.dispatcher.executorService.shutdown() }
        }
    }
}