package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.TimedValue

data class Address(val datacenter: String, val nodeId: String)
data class Event(val recipients: List<Address>, val payload: Payload)
data class Payload(val origin: String, val data: ByteArray)

enum class Result { ACCEPTED, REJECTED }

interface Client {
    //блокирующий метод для чтения данных
    fun readData(): Event

    //блокирующий метод отправки данных
    fun sendData(dest: Address, payload: Payload): Result
}

interface Handler {
    val timeout: Duration

    fun performOperation()
}