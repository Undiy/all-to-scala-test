package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

private const val TIMEOUT = 15_000

class MyHandler(private val client: Client) : Handler {

    private suspend fun getStatus(
        getStatusFn: (String) -> Response,
        responses: MutableSharedFlow<TimedValue<Response>>,
        id: String
    ) {
        val timedResponse = measureTimedValue {
            getStatusFn(id)
        }
        println("got status: $timedResponse")
        responses.emit(timedResponse)
        timedResponse.value.also {
            if (it is Response.RetryAfter) {
                delay(it.delay)
                getStatus(getStatusFn, responses, id)
            }
        }
    }

    @OptIn(FlowPreview::class)
    override fun performOperation(id: String): ApplicationStatusResponse {
        val responses = MutableSharedFlow<TimedValue<Response>>()

        var lastRequestTime: Duration? = null
        var retriesCount = 0

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { getStatus(client::getApplicationStatus1, responses, id) }
        scope.launch { getStatus(client::getApplicationStatus2, responses, id) }

        return runBlocking {
            responses
                .onEach {
                    if (it.value is Response.RetryAfter) {
                        lastRequestTime = it.duration
                        retriesCount += 1
                    }
                }
                .filter {
                    it.value is Response.Success || it.value is Response.Failure
                }
                .map { (response, duration) ->
                    when (response) {
                        is Response.Success -> ApplicationStatusResponse.Success(
                            id = response.applicationId,
                            status = response.applicationStatus
                        )
                        is Response.Failure -> {
                            ApplicationStatusResponse.Failure(duration, retriesCount + 1)
                        }
                        is Response.RetryAfter -> throw RuntimeException("Response.RetryAfter should be filtered out")
                    }
                }
                .timeout(TIMEOUT.milliseconds)
                .catch { exception ->
                    if (exception is TimeoutCancellationException) {
                        emit(ApplicationStatusResponse.Failure(lastRequestTime, retriesCount))
                    } else {
                        throw exception
                    }
                }
                .first().also {
                    scope.cancel()
                }
        }
    }
}