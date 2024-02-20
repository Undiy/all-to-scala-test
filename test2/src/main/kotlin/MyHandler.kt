package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration

class MyHandler(
    override val timeout: Duration,
    private val client: Client
) : Handler {
    override fun performOperation() {

        val events = MutableSharedFlow<Event>()
        val payloads = MutableSharedFlow<Pair<Address, Payload>>()

        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            while(isActive) {
                events.emit(client.readData())
            }
        }

        runBlocking {
            events.onEach { event ->
                println("Got new event: $event")
                event.recipients.onEach { address ->
                    payloads.emit(address to event.payload)
                }
            }.launchIn(scope)

            payloads.onEach { (address, payload) ->
                scope.launch {
                    val result = client.sendData(address, payload)
                    if (result == Result.REJECTED) {
                        delay(timeout)
                        payloads.emit(address to payload)
                    }
                }
            }.collect()
        }
    }
}