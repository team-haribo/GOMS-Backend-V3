package com.example.team.haribo.goms.global.log

object LogFormat {

    fun message(domain: String, event: String, vararg fields: Pair<String, Any?>): String {
        return buildString {
            append("[").append(domain).append("] ").append(event)

            fields.forEach { (key, value) ->
                if (value != null) {
                    append(" | ").append(key).append("=").append(value)
                }
            }
        }
    }

    fun transition(before: Any?, after: Any?): String {
        return "$before → $after"
    }
}