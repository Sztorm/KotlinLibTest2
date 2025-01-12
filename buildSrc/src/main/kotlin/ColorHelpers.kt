data class Color(val r: UByte, val g: UByte, val b: UByte, val a: UByte)

data class InterpolatedColor(val color: Color, val t: Double)

@Suppress("SpellCheckingInspection")
fun inverseLerp(a: Double, b: Double, t: Double) = (t - a) / (b - a)

@Suppress("SpellCheckingInspection")
fun lerp(colors: List<InterpolatedColor>, t: Double): Color {
    if (colors.isEmpty()) {
        throw IllegalArgumentException("colors must not be empty.")
    }
    if (colors.size == 1) {
        return colors[0].color
    }
    val bIndex: Int = colors
        .indexOfFirst { it.t >= t }
        .let { if (it <= 0) 1 else it }
    val aIndex: Int = bIndex - 1
    val tAB: Double = inverseLerp(colors[aIndex].t, colors[bIndex].t, t)

    val (ar: UByte, ag: UByte, ab: UByte, aa: UByte) = colors[aIndex].color
    val (br: UByte, bg: UByte, bb: UByte, ba: UByte) = colors[bIndex].color

    return Color(
        (ar.toDouble() + (br.toInt() - ar.toInt()).toDouble() * tAB).toUInt().toUByte(),
        (ag.toDouble() + (bg.toInt() - ag.toInt()).toDouble() * tAB).toUInt().toUByte(),
        (ab.toDouble() + (bb.toInt() - ab.toInt()).toDouble() * tAB).toUInt().toUByte(),
        (aa.toDouble() + (ba.toInt() - aa.toInt()).toDouble() * tAB).toUInt().toUByte()
    )
}

fun Color.formatRgb() = "rgb($r, $g, $b)"