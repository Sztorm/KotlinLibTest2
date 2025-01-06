import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.file.*

abstract class GenerateBadges : DefaultTask() {
    @get:InputFile
    abstract val testCoverageInput: RegularFileProperty

    @get:OutputFile
    abstract val testsStatusInput: RegularFileProperty

    @get:OutputFile
    abstract val testCoverageOutput: RegularFileProperty

    @get:OutputFile
    abstract val testsStatusOutput: RegularFileProperty

    private fun getTestCoverageResult(reportFile: List<String>): String =
        if (reportFile.size >= 3) {
            val methodCoverageLine = reportFile[reportFile.size - 3]
            val regex = Regex("""<counter type="METHOD" missed="(\d+)" covered="(\d+)"/>""")
            val match = regex.matchEntire(methodCoverageLine)

            when {
                match == null -> "Unknown"
                else -> {
                    val missed = match.groupValues[1].toInt()
                    val covered = match.groupValues[2].toInt()
                    val total = missed + covered
                    val result = (covered.toDouble() / total.toDouble()) * 100.0
                    val symbols = DecimalFormatSymbols(Locale.US)
                    val df = DecimalFormat("#.#", symbols)
                    "${df.format(result)}%"
                }
            }
        } else "Unknown"

    @Suppress("SpellCheckingInspection")
    private fun getTestCoverageBadge(coverageResult: String): String {
        val result =
            """
            <svg xmlns="http://www.w3.org/2000/svg" width="140" height="20">
                <linearGradient id="smooth" x2="0" y2="100%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                </linearGradient>
                <mask id="round">
                    <rect width="140" height="20" rx="3" fill="#fff"/>
                </mask>
                <g mask="url(#round)">
                    <rect width="90" height="20" fill="#555"/>
                    <rect width="50" height="20" x="90" fill="rgb(68, 204, 17)"/>
                    <rect width="140" height="20" fill="url(#smooth)"/>
                </g>
                <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
                    <text x="45" y="15" fill="#010101" fill-opacity=".3">Test Coverage</text>
                    <text x="45" y="14">Test Coverage</text>
                    <text x="116" y="15" fill="#010101" fill-opacity=".3">$coverageResult</text>
                    <text x="116" y="14">$coverageResult</text>
                </g>
            </svg>
            """.trimIndent()

        return result
    }

    private fun getTestsStatusResult(reportFile: String): String {
        val regex = Regex("""<div class="infoBox" id="failures">\s+<div class="counter">(\d+)</div>""")
        val match = regex.matchEntire(reportFile)

        return when {
            match == null -> "Failing"
            else -> {
                val failures = match.groupValues[1].toInt()

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
            <svg xmlns="http://www.w3.org/2000/svg" width="85" height="20">
                <linearGradient id="smooth" x2="0" y2="100%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                </linearGradient>
                <mask id="round">
                    <rect width="85" height="20" rx="3" fill="#fff"/>
                </mask>
                <g mask="url(#round)">
                    <rect width="35" height="20" fill="#555"/>
                    <rect width="50" height="20" x="35" fill="$statusColor"/>
                    <rect width="85" height="20" fill="url(#smooth)"/>
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
        val testCoverageBadge = testCoverageInput.get().asFile
            .readLines()
            .let(::getTestCoverageResult)
            .let(::getTestCoverageBadge)
        val testsStatusBadge = testsStatusInput.get().asFile
            .readText()
            .let(::getTestsStatusResult)
            .let(::getTestsStatusBadge)

        testCoverageOutput.get().asFile.writeText(testCoverageBadge)
        testsStatusOutput.get().asFile.writeText(testsStatusBadge)
    }
}