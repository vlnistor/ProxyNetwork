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
    withContext(Dispatchers.IO) {
        // Create output file
        File(output).createNewFile()
    }
    val proxies = addresses.map { InputServiceFactory.createInputService("$it/api/") }

    val file = File(output)
    AsyncRunner(proxies.map { it.first }, input) { file.appendText(it) }.run()

    // Remove trailing newline
    withContext(Dispatchers.IO) {
        RandomAccessFile(file, "rw").use {
            if(it.length() >= 1){
                it.setLength(it.length() - 1)
            }
        }
    }
    /**
     * Interestingly (or annoyingly if you were debugging this at 11pm), retrofit seems to block unless we forcefully close
     */
    proxies.forEach{
        it.second()
    }
}
