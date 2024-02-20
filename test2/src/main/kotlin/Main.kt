package org.example

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    val random = Random(42)
    val client = object : Client {
        override fun readData(): Event {
            Thread.sleep(5000)
            return Event(
                listOf(
                    Address("rec1", "node1"),
                    Address("rec2", "node2")
                ),
                Payload("origin1", random.nextBytes(3))
            )
        }

        override fun sendData(dest: Address, payload: Payload): Result {
            Thread.sleep(1000)
            val result = if (random.nextBoolean()) Result.ACCEPTED else Result.REJECTED
            println("Received: payload $payload at dest: $dest, result: $result")
            return result
        }
    }
    val handler = MyHandler(timeout = 200.milliseconds, client = client)

    handler.performOperation()
}