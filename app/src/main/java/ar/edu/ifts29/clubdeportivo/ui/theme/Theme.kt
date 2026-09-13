package ar.edu.ifts29.clubdeportivo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.R

/**
 * Dirección visual "1a Pista" de la maqueta.
 *
 * La maqueta declara los colores en OKLCH (`--bg: oklch(0.16 0.006 80)`, etc.); acá van
 * convertidos a sRGB, que es lo que entiende Compose. Los nombres se mantienen iguales a
 * las custom properties del prototipo para poder cruzar ambos lados con el original.
 */
object Pista {
    val bg = Color(0xFF0F0D0A)       // --bg
    val sur = Color(0xFF1A1815)      // --sur
    val sur2 = Color(0xFF151411)     // --sur2
    val ink = Color(0xFFF3F1EE)      // --ink
    val mut = Color(0xFF95928B)      // --mut
    val line = Color(0xFF282623)     // --line
    val acc = Color(0xFFB3E635)      // --acc
    val accInk = Color(0xFF131809)   // --acc-ink
    val accSoft = Color(0xFF27330E)  // --acc-soft
    val cta = Color(0xFFF3F1EE)      // --cta
    val ctaInk = Color(0xFF0F0D0A)   // --cta-ink
    val warn = Color(0xFFF9AD26)     // --warn
    val warnInk = Color(0xFF2F1E02)  // --warn-ink
    val bad = Color(0xFFFF645F)      // --bad
    val badInk = Color(0xFFFFFFFF)   // --bad-ink
    val ok = acc                     // --ok
    val okInk = accInk               // --ok-ink
    val field = Color(0xFF211F1C)    // --field

    /** Rojo destructivo del action sheet de iOS: oklch(0.62 0.22 27). */
    val iosRed = Color(0xFFEE3533)

    /** --r: radio de las tarjetas grandes. */
    val r = RoundedCornerShape(14.dp)

    /** --rs: radio de botones y campos. */
    val rs = RoundedCornerShape(8.dp)

    /** Radio de 10-12px que la maqueta usa en las filas de lista. */
    val rRow = RoundedCornerShape(10.dp)
    val rCard = RoundedCornerShape(12.dp)

    /** --ls: letter-spacing del texto de display. */
    val ls = 0.5.sp

    /** Padding horizontal del contenido: `margin: … 22px`. */
    val gutter = 22.dp
}

private val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
)

private val BarlowCondensed = FontFamily(
    Font(R.font.barlowcondensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlowcondensed_bold, FontWeight.Bold),
    Font(R.font.barlowcondensed_extrabold, FontWeight.ExtraBold),
)

/** `--font`: Barlow, el cuerpo de texto. */
fun body(
    weight: Int = 500,
    size: Int = 14,
    color: Color = Pista.ink,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
    fontFamily = Barlow,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    color = color,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
)

/** `--disp`: Barlow Condensed, los títulos y cifras. */
fun disp(
    weight: Int = 700,
    size: Int = 20,
    color: Color = Pista.ink,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = Pista.ls,
): TextStyle = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    color = color,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
)

/**
 * Etiquetas de sección: `font:600 11-12px; letter-spacing:1.2px; text-transform:uppercase`.
 */
fun label(size: Int = 12, color: Color = Pista.mut): TextStyle =
    body(weight = 600, size = size, color = color, letterSpacing = 1.2.sp)

/** Tipografía del sistema, para el tab bar y los action sheets estilo iOS. */
@Composable
fun sfText(weight: Int = 400, size: Int = 17, color: Color = Pista.ink): TextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    color = color,
)
