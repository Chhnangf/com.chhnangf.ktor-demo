package com.chhnangf.model

object SerialTaskRepository {
    private val tasks = mutableListOf(
        SerialTask("learn", "Ktor-routing", Priority.Vital),
        SerialTask("project", "Ktor-routing", Priority.High),
        SerialTask("data", "Ktor-Task", Priority.Medium),
    )

    fun allTasks(): List<SerialTask> = tasks

    fun tasksByPriority(priority: Priority) = tasks.filter { task -> task.priority == priority }

    fun taskByName(name: String) = tasks.find { task -> task.name.equals(name, true) }

    fun addTask(task: SerialTask) {
        if (taskByName(task.name) != null) {
            throw IllegalArgumentException("Task with name ${task.name} already exists")
        }
        tasks.add(task)
    }

    fun removeTask(name: String) : Boolean {
        return tasks.removeIf { it.name == name }
    }
}