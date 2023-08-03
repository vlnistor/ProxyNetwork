# Task


The task is to maximize throughput over a proxy network. I am given 3 command line arguments pointing to 3 unique files.
1. **Proxy file path** - This file contains a proxy url on every line. I can issue up to 30 concurrent requests to each of these proxies.
2. **Code file path** - There is a code on every line. To _solve_ a code I must send a request to any of the proxies with the code.
3. **Solution file path** - This file should contain a code-solution pair on every line. (The order need not be the same as the code file path.

**Considerations**
1. Proxies will respond to the solution of a code with a status code of 200 and a body containing the solution.
2. Issuing any more than 30 concurent requests to a given proxy runs the risk of being blocked. Being blocked is equivalent to the proxy only responding with status code 503.
3. Even without being blocked, a proxy may sometimes fail to respond correctly. The status code will be 503.
4. If any of the proxies responds with anything but a 503 (fail) or a 200 (success) the whole application must be shut down immediately.
5. The code should be written as to minimize data loss.


**Below is an implementation of the above constraints.**

# How to run

 1. `chmod +x build.sh`
 2. `./build.sh`
 3. `cd untitled-1.0-SNAPSHOT/bin`
 4. `./untitled input1 input2 input3`

# Thought process

### My thought process is summarized as follows:

1. Put all the input in a single channel 
2. Spawn parallel masters for every available proxy.
3. Each master spawns as many as 30 parallel slaves (at a time) to call the proxy and handle the response.
4. Each slave is responsible for issuing a request to the proxy and output the response to an output channel
5. The output channel is consumed by a single process that writes the output to a file - The reason behind having a single process consuming the output channel is because handling concurrent writes to a file was left out of the scope of this solution.


### Self critique

1. I had to call `coroutineContext.cancelChildren()` in the `run` of `AsyncRunner` as I didn't have an elegant way to close the channels. If I did not do this, the function would have never returned as the code reading from the input channel would be blocking forever. Additionally, I only get to this part
of the code after the process consuming the output channel has finished reading the number of codes I instantiate `AsyncRunner` with. I feel this could be improved with a more elegant solution but due to time restraints I chose to leave it as is.
2. The channels themselves have an `UNLIMITED` buffer size. This can easily become a problem with large input sets. The challenge was that if I had a buffer size, then I would've had to
come up with a smart solution that puts items on the input channel while also consuming. This can easily give rise to some race conditions and due to the time restraint I chose to leave it out of scope.
3. I have a single process writing the output to the file. Could I have improved the amount of information retained when an error is thrown with multiple processes?  
4. The tests I use to check no more than 30 concurrent requests can exist are based on the assumption that the 1 second delay is enough for at least 1 more request to come through. On slow systems this assumption breaks down. I would
have liked to have a more elegant test with more time.
5. The tests checking if the input-output pair is correct only checks this by the number of lines. This could easily have been done on the actual values.
6. I had to write 2 instances of the mock service used in the tests. Namely, `MockInputService` and `MockInputServiceRecovers`. 
This is a bit hard to read and a bit unintuitive.
7. The project is called `untitled`.






