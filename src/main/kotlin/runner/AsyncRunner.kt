package runner

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.InputService
import java.util.concurrent.atomic.AtomicInteger
import org.slf4j.LoggerFactory


const val MAX_CONCURRENT_REQUESTS = 30

class AsyncRunner(
    private val proxies: List<InputService>,
    private val codes: List<String>,
    private val write: (blob: String) -> Unit,
) {

    companion object{
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    // Codes to be consumed by the proxies
    private val inputChannel: Channel<String> = Channel(Channel.UNLIMITED)
    // Codes that have been successfully processed and can be written to the output file
    private val outputChannel: Channel<Pair<String, String>> = Channel(Channel.UNLIMITED)

    private suspend fun runProxy(proxy: InputService) = coroutineScope{
        repeat(MAX_CONCURRENT_REQUESTS){
            launch {
                doWork(proxy)
            }
        }
    }

    private suspend fun doWork(proxy: InputService) {
        for(code in inputChannel){
            val response = proxy.call(code)
            when(response.code()){
                200 -> {
                    logger.debug("Got response for $code : ${response.body()!!.information}")
                    outputChannel.send(Pair(code, response.body()!!.information))
                }
                503 -> {
                    logger.error("Got 503 for $code, retrying")
                    inputChannel.send(code)
                }
                else -> {
                    throw IllegalStateException("Got unexpected response code ${response.code()} for $code")
                }
            }
        }

    }

    private suspend fun outputToFile(){
        var written = 0
        for((code, information) in outputChannel){
            written++
            write("$code $information\n")
            if(written == codes.size){
                break
            }
        }
    }

    suspend fun run() = coroutineScope{
        codes.forEach {
            inputChannel.send(it)
        }
        proxies.forEach{
            launch {
                runProxy(it)
            }
        }
        outputToFile()
        this.coroutineContext.cancelChildren()
    }
}