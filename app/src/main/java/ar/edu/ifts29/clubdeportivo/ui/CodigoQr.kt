package ar.edu.ifts29.clubdeportivo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Código QR dibujado con Compose a partir de [texto]. Se pinta en negro sobre blanco, con
 * margen, para que cualquier lector lo tome desde la pantalla del teléfono.
 */
@Composable
fun CodigoQr(texto: String, modifier: Modifier = Modifier) {
    val matriz = remember(texto) {
        QRCodeWriter().encode(
            texto,
            BarcodeFormat.QR_CODE,
            0,
            0,
            mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 0,
            ),
        )
    }
    Canvas(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        val modulos = matriz.width
        val lado = size.minDimension / modulos
        for (y in 0 until modulos) {
            for (x in 0 until modulos) {
                if (matriz.get(x, y)) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x * lado, y * lado),
                        size = Size(lado + 0.5f, lado + 0.5f),
                    )
                }
            }
        }
    }
}
