import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.file.*

abstract class GenerateTestsStatusBadge : DefaultTask() {
    @get:InputFile
    abstract val testsStatusReportInput: RegularFileProperty

    @get:OutputFile
    abstract val badgeOutput: RegularFileProperty

    private fun getTestsStatusResult(reportFile: String): String {
        val regex = Regex("""<div class="infoBox" id="failures">\s+<div class="counter">(\d+)</div>""")
        val match = regex.find(reportFile)

        return when {
            match == null -> "Failing"
            else -> {
                val failures = match.groupValues[1].toInt()
                println(match)
                println(failures)
                when (failures) {
                    0 -> "Passing"
                    else -> "Failing"
                }
            }
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun getTestsStatusBadge(statusResult: String): String {
        val statusColor = when (statusResult) {
            "Passing" -> "rgb(68, 204, 17)"
            else -> "rgb(199, 79, 60)"
        }
        val result =
            """
            <svg xmlns="http://www.w3.org/2000/svg" width="87" height="20">
                <linearGradient id="smooth" x2="0" y2="100%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                </linearGradient>
                <mask id="round">
                    <rect width="87" height="20" rx="3" fill="#fff"/>
                </mask>
                <g mask="url(#round)">
                    <rect width="35" height="20" fill="#555"/>
                    <rect width="52" height="20" x="35" fill="$statusColor"/>
                    <rect width="87" height="20" fill="url(#smooth)"/>
                </g>
                <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
                    <text x="17" y="15" fill="#010101" fill-opacity=".3">Tests</text>
                    <text x="17" y="14">Tests</text>
                    <text x="61" y="15" fill="#010101" fill-opacity=".3">$statusResult</text>
                    <text x="61" y="14">$statusResult</text>
                </g>
            </svg>
            """.trimIndent()

        return result
    }

    @TaskAction
    fun generate() {
        val testsStatusBadge = testsStatusReportInput.get().asFile
            .readText()
            .let(::getTestsStatusResult)
            .let(::getTestsStatusBadge)

        badgeOutput.get().asFile.writeText(testsStatusBadge)
    }
}