import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.file.*

abstract class GenerateTestsStatusBadge : DefaultTask() {
    @get:InputFile
    abstract val testsStatusReportInput: RegularFileProperty

    @get:OutputFile
    abstract val badgeOutput: RegularFileProperty

    @TaskAction
    fun generate() {
        val testsStatusBadge = testsStatusReportInput.get().asFile
            .readText()
            //.let { StatusResult.Passing }
            //.let { StatusResult.Failing }
            //.let { StatusResult.Unknown }
            .let(::getTestsStatusResult)
            .let(::getTestsStatusBadge)

        badgeOutput.get().asFile.writeText(testsStatusBadge)
    }

    companion object {
        private val passingColor = Color(68u, 204u, 17u, 255u)
        private val failingColor = Color(199u, 79u, 60u, 255u)
        private val unknownColor = Color(155u, 155u, 155u, 255u)

        private fun getTestsStatusResult(reportFile: String): TestsStatusResult {
            val regex =
                Regex("""<div class="infoBox" id="failures">\s+<div class="counter">(\d+)</div>""")
            val match = regex.find(reportFile)

            return when {
                match == null -> TestsStatusResult.Unknown
                else -> {
                    val failures = match.groupValues[1].toInt()
                    when (failures) {
                        0 -> TestsStatusResult.Passing
                        else -> TestsStatusResult.Failing
                    }
                }
            }
        }

        @Suppress("SpellCheckingInspection")
        private fun getTestsStatusBadge(statusResult: TestsStatusResult): String {
            val badgeColor = when (statusResult) {
                TestsStatusResult.Passing -> passingColor
                TestsStatusResult.Failing -> failingColor
                TestsStatusResult.Unknown -> unknownColor
            }.formatRgb()
            val formattedStatus = when (statusResult) {
                TestsStatusResult.Passing -> "Passing"
                TestsStatusResult.Failing -> "Failing"
                TestsStatusResult.Unknown -> "Unknown"
            }
            val testsWidth = 38
            val (statusWidth, statusX) = when (statusResult) {
                TestsStatusResult.Passing -> Pair(52, 64)
                TestsStatusResult.Failing -> Pair(44, 60)
                TestsStatusResult.Unknown -> Pair(60, 68)
            }
            val totalWidth = testsWidth + statusWidth
            val result =
                """
            <svg xmlns="http://www.w3.org/2000/svg" width="$totalWidth" height="20">
                <linearGradient id="smooth" x2="0" y2="100%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                </linearGradient>
                <mask id="round">
                    <rect width="$totalWidth" height="20" rx="3" fill="#fff"/>
                </mask>
                <g mask="url(#round)">
                    <rect width="$testsWidth" height="20" fill="#555"/>
                    <rect width="$statusWidth" height="20" x="$testsWidth" fill="$badgeColor"/>
                    <rect width="$totalWidth" height="20" fill="url(#smooth)"/>
                </g>
                <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
                    <text x="19" y="15" fill="#010101" fill-opacity=".3">Tests</text>
                    <text x="19" y="14">Tests</text>
                    <text x="$statusX" y="15" fill="#010101" fill-opacity=".3">$formattedStatus</text>
                    <text x="$statusX" y="14">$formattedStatus</text>
                </g>
            </svg>
            """.trimIndent()

            return result
        }
    }
}

sealed class TestsStatusResult {
    object Passing : TestsStatusResult()
    object Failing : TestsStatusResult()
    object Unknown : TestsStatusResult()
}