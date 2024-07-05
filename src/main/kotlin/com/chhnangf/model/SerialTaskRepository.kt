package com.chhnangf.model

object SerialTaskRepository {
    private val taskList = mutableListOf(
        PhotoObject(
            124,
            "Task 1",
            "feedback",
            "??",
            "0*0",
            "null",
            "null",
            "https://images.metmuseum.org/CRDImages/ep/original/DT1567.jpg",
            "https://images.metmuseum.org/CRDImages/ep/web-additional/DT1567.jpg",
            "null",
            "null",
            "2024-6-26"
            ),
        PhotoObject(
            125,
            "Task 2",
            "feedback",
            "??",
            "0*0",
            "null",
            "null",
            "https://images.metmuseum.org/CRDImages/ep/original/DT1567.jpg",
            "https://images.metmuseum.org/CRDImages/ep/web-additional/DT1567.jpg",
            "null",
            "null",
            "2024-6-26"
        ),
    )

    fun allTasks(): List<PhotoObject> = taskList

//    fun tasksByPriority(priority: Priority) = tasks.filter { task -> task.priority == priority }

//    fun taskByName(name: String) = tasks.find { task -> task.name.equals(name, true) }
//
    fun addTask(task: PhotoObject) {
    taskList.add(task)
    }

    fun addListTask(tasks: List<PhotoObject>) {
        taskList.addAll(tasks)
    }

//    fun removeTask(name: String) : Boolean {
//        return tasks.removeIf { it.name == name }
//    }
}