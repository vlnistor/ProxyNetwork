import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import runner.AsyncRunner
import service.InputServiceFactory
import java.io.File
import java.io.RandomAccessFile

fun main(args: Array<String>) = runBlocking {

    val input = File(args[0]).readLines()
    val addresses = File(args[1]).readLines()
    val output = args[2]

    createOutputfile(output)
    val proxies = addresses.map { InputServiceFactory.createInputService("$it/api/") }
    val file = File(output)

    AsyncRunner(proxies.map { it.inputService }, input) { file.appendText(it) }.run()

    removeTrailingLine(file)
    /**
     * Retrofit hangs unless we forcefully close
     */
    proxies.forEach{
        it.shutdown()
    }
}

suspend fun createOutputfile(filename: String){
    withContext(Dispatchers.IO) {
        File(filename).createNewFile()
    }
}

suspend fun removeTrailingLine(file: File){
    withContext(Dispatchers.IO) {
        RandomAccessFile(file, "rw").use {
            if (it.length() >= 1) {
                it.setLength(it.length() - 1)
            }
        }
    }
}
