package org.example

import kotlin.time.Duration.Companion.milliseconds

fun main() {
    println("Start")

    val client = object : Client {
        override fun getApplicationStatus1(id: String): Response {
            Thread.sleep(1000)
            return Response.RetryAfter(0.milliseconds)
        }

        override fun getApplicationStatus2(id: String): Response {
            Thread.sleep(5000)
//            return Response.RetryAfter(1000.milliseconds)
//            return Response.Failure(RuntimeException("Boom!"))
            return Response.Success("status: OK", id)
        }
    }

    val handler = MyHandler(client)

    println(handler.performOperation("42"))
}