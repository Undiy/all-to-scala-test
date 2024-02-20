package org.example

import kotlin.time.Duration

sealed interface Response {
    data class Success(val applicationStatus: String, val applicationId: String) : Response
    data class RetryAfter(val delay: Duration) : Response
    data class Failure(val ex: Throwable) : Response
}

sealed interface ApplicationStatusResponse {
    data class Failure(val lastRequestTime: Duration?, val retriesCount: Int): ApplicationStatusResponse
    data class Success(val id: String, val status: String): ApplicationStatusResponse
}

fun interface Handler {
    fun performOperation(id: String): ApplicationStatusResponse
}

interface Client {
    //блокирующий вызов сервиса 1 для получения статуса заявки
    fun getApplicationStatus1(id: String): Response

    //блокирующий вызов сервиса 2 для получения статуса заявки
    fun getApplicationStatus2(id: String): Response
}