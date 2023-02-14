package runner

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import service.MockInputService
import service.MockInputServiceRecovers
import java.lang.IllegalStateException

class AsyncRunnerTest{

    private suspend fun delayUntil(f: () -> Boolean) = coroutineScope{
        while(!f()) delay(1000)
    }

    @Test
    fun `a proxy cannot have more concurrent requests than allowed - one proxy given to the runner`() = runBlocking {
        val proxy = MockInputService()
        val codesOfHold = mutableListOf<String>()
        repeat(60){
            codesOfHold.add("hold")
        }
        launch {
            AsyncRunner(listOf(proxy), codesOfHold){}.run()
        }
        delayUntil{proxy.activeRequests.get() == MAX_CONCURRENT_REQUESTS}
        // Give 1 more second to ensure more requests can come across
        delay(1000)
        assert(proxy.activeRequests.get() == MAX_CONCURRENT_REQUESTS)
        this.coroutineContext.cancelChildren()
    }

    @Test
    fun `a proxy cannot have more concurrent requests than allowed - two proxies given to the runner`() = runBlocking {
        val proxyOne = MockInputService()
        val proxyTwo = MockInputService()
        val codesOfHold = mutableListOf<String>()
        repeat(120){
            codesOfHold.add("hold")
        }
        launch {
            AsyncRunner(listOf(proxyOne, proxyTwo), codesOfHold){}.run()
        }
        delayUntil{proxyOne.activeRequests.get() == MAX_CONCURRENT_REQUESTS}
        delayUntil{proxyTwo.activeRequests.get() == MAX_CONCURRENT_REQUESTS}
        // Give 1 more second to ensure more requests can come across
        delay(1000)
        assert(proxyOne.activeRequests.get() == MAX_CONCURRENT_REQUESTS)
        assert(proxyTwo.activeRequests.get() == MAX_CONCURRENT_REQUESTS)
        this.coroutineContext.cancelChildren()
    }

    @Test
    fun `a 503 results in the message being queued back`() = runBlocking {
        val proxy = MockInputServiceRecovers()
        val codesOfHold = mutableListOf("test1","test2")
        AsyncRunner(listOf(proxy), codesOfHold){}.run()
        // First should bounce back
        // Second should pass
        // Third will bounce back again
        // Fourth will pass
        assert(proxy.totalRequests.get() == 4)
    }

    @Test
    fun `an unknown response code results in an exception being thrown`(): Unit = runBlocking{
        val proxy = MockInputService()
        val codesOfHold = mutableListOf("fail")

        val error = assertThrows <IllegalStateException> {
            runBlocking {
                AsyncRunner(listOf(proxy), codesOfHold){}.run()
            }
        }
        assert(error.message!! == "Got unexpected response code 500 for fail")

    }

    @Test
    fun `single proxy writes correctly`() = runBlocking{
        val proxy = MockInputService()
        val codesOfHold = mutableListOf<String>()
        val output = mutableListOf<String>()
        repeat(10){
            codesOfHold.add("good")
        }
        AsyncRunner(listOf(proxy), codesOfHold){
            output.add(it)
        }.run()
        assert(output.size == 10)
    }

    @Test
    fun `two proxies write correctly`() = runBlocking {
        val proxyOne = MockInputService()
        val proxyTwo = MockInputService()
        val codesOfHold = mutableListOf<String>()
        val output = mutableListOf<String>()
        repeat(10){
            codesOfHold.add("good")
        }
        AsyncRunner(listOf(proxyOne, proxyTwo), codesOfHold){
            output.add(it)
        }.run()
        assert(output.size == 10)
    }

}