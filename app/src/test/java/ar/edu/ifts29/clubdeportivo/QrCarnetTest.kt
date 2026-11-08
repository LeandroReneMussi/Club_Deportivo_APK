package ar.edu.ifts29.clubdeportivo

import ar.edu.ifts29.clubdeportivo.data.DemoData
import ar.edu.ifts29.clubdeportivo.data.Fecha
import ar.edu.ifts29.clubdeportivo.data.Reloj
import ar.edu.ifts29.clubdeportivo.vm.SocioVista
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** HU-06 (v1.5): el QR del carnet, leído por un lector, devuelve exactamente los datos del carnet. */
class QrCarnetTest {

    @Before
    fun fijarReloj() = Reloj.fijar(Fecha(2026, 9, 8))

    @After
    fun soltarReloj() = Reloj.fijar(null)

    @Test
    fun `el QR del carnet se decodifica con los datos del carnet`() {
        val texto = SocioVista(DemoData.socios.first { it.id == 1 }).qrCarnet
        // La misma codificación que usa la pantalla; se lee como "código puro" (sin fondo ni foto).
        val matriz = QRCodeWriter().encode(
            texto,
            BarcodeFormat.QR_CODE,
            0,
            0,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 4),
        )
        val w = matriz.width
        val h = matriz.height
        val pixeles = IntArray(w * h) { i -> if (matriz.get(i % w, i / w)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() }
        val imagen = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(w, h, pixeles)))

        val leido = QRCodeReader().decode(
            imagen,
            mapOf(DecodeHintType.CHARACTER_SET to "UTF-8", DecodeHintType.PURE_BARCODE to true),
        ).text

        assertEquals(texto, leido)
        assertTrue(leido.contains("N° de socio: 01245"))
        assertTrue(leido.contains("DNI: 38.412.907"))
        assertTrue(leido.contains("Carnet vence: 31/03/2027"))
    }
}
