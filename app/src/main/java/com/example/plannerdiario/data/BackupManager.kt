package com.jsjstudios.dailyplanner.data

import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    private const val VERSION = 1

    /** Serializa todas as listas e tarefas em JSON (pretty-print). */
    fun exportToJson(lists: List<TaskList>, tasks: List<Task>): String {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val listsArr = JSONArray()
        lists.forEach { list ->
            val listObj = JSONObject()
            listObj.put("id",       list.id)
            listObj.put("name",     list.name)
            listObj.put("colorHex", list.colorHex)
            listObj.put("shape",    list.shape)

            val tasksArr = JSONArray()
            tasks.filter { it.listId == list.id }.forEach { task ->
                val t = JSONObject()
                t.put("id",             task.id)
                t.put("listId",         task.listId)
                t.put("title",          task.title)
                t.put("description",    task.description)
                t.put("isCompleted",    task.isCompleted)
                t.put("completedDate",  task.completedDate  ?: JSONObject.NULL)
                t.put("attachmentUri",  task.attachmentUri  ?: JSONObject.NULL)
                t.put("attachmentType", task.attachmentType ?: JSONObject.NULL)
                t.put("attachmentName", task.attachmentName ?: JSONObject.NULL)
                t.put("createdAt",      task.createdAt)
                tasksArr.put(t)
            }
            listObj.put("tasks", tasksArr)
            listsArr.put(listObj)
        }
        root.put("lists", listsArr)
        return root.toString(2)
    }

    /**
     * Desserializa JSON de backup.
     * Retorna (lists, tasks) onde as listas ainda carregam o `id` original
     * para que o importador possa remapear os `listId` das tarefas.
     */
    fun importFromJson(json: String): Pair<List<TaskList>, List<Task>> {
        val root = JSONObject(json)
        val listsArr = root.getJSONArray("lists")

        val lists = mutableListOf<TaskList>()
        val tasks = mutableListOf<Task>()

        for (i in 0 until listsArr.length()) {
            val lo = listsArr.getJSONObject(i)
            lists.add(
                TaskList(
                    id       = lo.getLong("id"),  // mantido para remapear tarefas
                    name     = lo.getString("name"),
                    colorHex = lo.optString("colorHex", "#EC407A"),
                    shape    = lo.optString("shape", "circle")
                )
            )

            val tasksArr = lo.getJSONArray("tasks")
            for (j in 0 until tasksArr.length()) {
                val to = tasksArr.getJSONObject(j)
                tasks.add(
                    Task(
                        id             = 0,
                        listId         = lo.getLong("id"),
                        title          = to.getString("title"),
                        description    = to.optString("description", ""),
                        isCompleted    = to.optBoolean("isCompleted", false),
                        completedDate  = to.optNullableString("completedDate"),
                        attachmentUri  = to.optNullableString("attachmentUri"),
                        attachmentType = to.optNullableString("attachmentType"),
                        attachmentName = to.optNullableString("attachmentName"),
                        createdAt      = to.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }
        return lists to tasks
    }

    // Helper: retorna null se o campo for JSONObject.NULL ou ausente
    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).ifBlank { null }
}

