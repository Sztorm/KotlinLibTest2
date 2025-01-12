import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.file.*

abstract class GenerateCoverageBadge : DefaultTask() {
    @get:InputFile
    abstract val coverageReportInput: RegularFileProperty

    @get:OutputFile
    abstract val badgeOutput: RegularFileProperty

    private fun getCoverageResult(reportFile: List<String>): CoverageResult? =
        if (reportFile.size >= 3) {
            val methodCoverageLine = reportFile[reportFile.size - 3]
            val regex = Regex("""<counter type="METHOD" missed="(\d+)" covered="(\d+)"/>""")
            val match = regex.find(methodCoverageLine)

            when {
                match == null -> null
                else -> {
                    val missed = match.groupValues[1].toInt()
                    val covered = match.groupValues[2].toInt()
                    val total = missed + covered
                    val coverageRatio = (covered.toDouble() / total.toDouble())
                    val coverageRatioPercentage = coverageRatio * 100.0
                    val symbols = DecimalFormatSymbols(Locale.US)
                    val df = DecimalFormat("#.#", symbols)
                    val formattedRatio = "${df.format(coverageRatioPercentage)}%"

                    CoverageResult(coverageRatio, formattedRatio)
                }
            }
        } else null

    @Suppress("SpellCheckingInspection")
    private fun getCoverageBadge(coverageResult: CoverageResult?): String {
        val formattedCoverage = coverageResult?.formattedRatio ?: "Unknown"
        val badgeColor = coverageResult?.ratio.let {
            when (it) {
                null -> unknownColor
                else -> lerp(coverageGradient, it)
            }.formatRgb()
        }
        val result =
            """
            <svg xmlns="http://www.w3.org/2000/svg" width="104" height="20">
                <linearGradient id="smooth" x2="0" y2="100%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                </linearGradient>
                <mask id="round">
                    <rect width="104" height="20" rx="3" fill="#fff"/>
                </mask>
                <g mask="url(#round)">
                    <rect width="64" height="20" fill="#555"/>
                    <rect width="40" height="20" x="64" fill="$badgeColor"/>
                    <rect width="104" height="20" fill="url(#smooth)"/>
                </g>
                <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
                    <text x="32" y="15" fill="#010101" fill-opacity=".3">Coverage</text>
                    <text x="32" y="14">Coverage</text>
                    <text x="84" y="15" fill="#010101" fill-opacity=".3">$formattedCoverage</text>
                    <text x="84" y="14">$formattedCoverage</text>
                </g>
            </svg>
            """.trimIndent()

        return result
    }

    @TaskAction
    fun generate() {
        val coverageBadge = coverageReportInput.get().asFile
            .readLines()
            .let(::getCoverageResult)
            .let(::getCoverageBadge)

        badgeOutput.get().asFile.writeText(coverageBadge)
    }

    data class CoverageResult(val ratio: Double, val formattedRatio: String)

    companion object {
        private val coverageGradient = listOf(
            InterpolatedColor(Color(199u, 79u, 60u, 255u), t = 0.0),
            InterpolatedColor(Color(199u, 79u, 60u, 255u), t = 0.5),
            InterpolatedColor(Color(234u, 194u, 53u, 255u), t = 0.75),
            InterpolatedColor(Color(68u, 204u, 17u, 255u), t = 1.0),
        )
        private val unknownColor = Color(155u, 155u, 155u, 255u)
    }
}