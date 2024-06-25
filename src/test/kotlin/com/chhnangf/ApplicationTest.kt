package com.chhnangf

import com.chhnangf.model.Priority
import com.chhnangf.model.SerialTask
import com.chhnangf.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlin.test.*

//class ApplicationTest {
//    @Test
//    fun testRoot() = testApplication {
//        client.get("/").apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("Hello World!", bodyAsText())
//        }
//    }
//}

class ApplicationTest {
    @Test
    fun tasksCanBeFoundByPriority() = testApplication {
        install(ContentNegotiation) {
            json()
        }
        val client = createClient {

        }

        val response = client.get("/tasks/byPriority/Medium")
        val results = response.body<List<SerialTask>>()

        assertEquals(HttpStatusCode.OK, response.status)

        val expectedTaskNames = listOf("gardening", "painting")
        val actualTaskNames = results.map(SerialTask::name)
        assertContentEquals(expectedTaskNames, actualTaskNames)
    }

    @Test
    fun invalidPriorityProduces400() = testApplication {
        val response = client.get("/tasks/byPriority/Invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }


    @Test
    fun unusedPriorityProduces404() = testApplication {
        val response = client.get("/tasks/byPriority/Vital")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun newTasksCanBeAdded() = testApplication {
        val client = createClient {
//            install(ContentNegotiation) {
//                json()
//            }
        }

        val task = SerialTask("swimming", "Go to the beach", Priority.Low)
        val response1 = client.post("/tasks") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(task)
        }
        assertEquals(HttpStatusCode.NoContent, response1.status)

        val response2 = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response2.status)

        val taskNames = response2
            .body<List<SerialTask>>()
            .map { it.name }

        assertContains(taskNames, "swimming")
    }
}