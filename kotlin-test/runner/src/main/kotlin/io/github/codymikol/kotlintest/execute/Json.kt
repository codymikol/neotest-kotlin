package io.github.codymikol.kotlintest.execute

/**
 * Serializes a [RunReport] to JSON.
 *
 * The runner is placed on the classpath of the project under test, so it must not bring
 * along a JSON library (such as Jackson) that could conflict with the project's own version.
 * The format is small and fixed, so it is written by hand.
 */
internal fun RunReport.toJson(): String =
    joinToString(separator = ",", prefix = "[", postfix = "]") { it.toJson() }

internal fun TestResult.toJson(): String =
    jsonObject(
        "className" to className.toJsonString(),
        "id" to id.toJsonString(),
        "duration" to duration.inWholeNanoseconds.toString(),
        "status" to status.toJson(),
    )

internal fun TestStatus.toJson(): String =
    when (this) {
        is TestStatus.Success -> jsonObject("type" to type.name.toJsonString())
        is TestStatus.Ignored ->
            jsonObject(
                "type" to type.name.toJsonString(),
                "reason" to reason.toJsonString(),
            )
        is TestStatus.Failure ->
            jsonObject(
                "type" to type.name.toJsonString(),
                "stackTrace" to stackTrace.toJsonString(),
                "error" to (
                    error?.let {
                        jsonObject(
                            "message" to it.message.toJsonString(),
                            "lineNumber" to (it.lineNumber?.toString() ?: "null"),
                            "filename" to it.filename.toJsonString(),
                        )
                    } ?: "null"
                    ),
            )
    }

private fun jsonObject(vararg fields: Pair<String, String>): String =
    fields.joinToString(separator = ",", prefix = "{", postfix = "}") { (key, value) ->
        "${key.toJsonString()}:$value"
    }

private const val LAST_CONTROL_CHARACTER = 0x1F

internal fun String?.toJsonString(): String {
    if (this == null) {
        return "null"
    }

    return buildString(length + 2) {
        append('"')
        for (char in this@toJsonString) {
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                else ->
                    if (char.code <= LAST_CONTROL_CHARACTER) {
                        append("\\u").append(char.code.toString(radix = 16).padStart(length = 4, padChar = '0'))
                    } else {
                        append(char)
                    }
            }
        }
        append('"')
    }
}
