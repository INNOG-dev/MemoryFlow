package fr.innog.memoryflow.data.parser

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.innog.memoryflow.data.local.model.Answer

object QuizParser {
    private val gson = Gson()

    fun parse(json : String?) : List<Answer>
    {
        if(json.isNullOrEmpty()) return emptyList()

        return try {
            val type = object : TypeToken<List<Answer>>() {}.type
            gson.fromJson(json, type)
        }
        catch (e : Exception)
        {
            emptyList()
        }
    }

}