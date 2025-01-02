import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.file.*

abstract class GenerateBadges : DefaultTask() {
    @get:InputFile
    abstract val testCoverageInputFile: RegularFileProperty

    @get:OutputFile
    abstract val testCoverageBadgeOutputFile: RegularFileProperty

    private fun getTestCoverageResult(): String {
        val testCoverageReport = testCoverageInputFile.get().asFile.readLines()

        return if (testCoverageReport.size >= 3) {
            val methodCoverageLine = testCoverageReport[testCoverageReport.size - 3]
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
    }

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
                    <text x="116" y="15" fill="#010101" fill-opacity=".3">${coverageResult}</text>
                    <text x="116" y="14">${coverageResult}</text>
                </g>
            </svg>
            """.trimIndent()

        return result
    }

    @TaskAction
    fun generate() {
        testCoverageBadgeOutputFile.get().asFile.writeText(getTestCoverageBadge(getTestCoverageResult()))
    }
}