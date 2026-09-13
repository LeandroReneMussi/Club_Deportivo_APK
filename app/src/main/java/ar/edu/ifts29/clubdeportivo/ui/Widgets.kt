package ar.edu.ifts29.clubdeportivo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.ifts29.clubdeportivo.ui.theme.Pista
import ar.edu.ifts29.clubdeportivo.ui.theme.body
import ar.edu.ifts29.clubdeportivo.ui.theme.disp

/**
 * Cuerpo scrolleable de una pantalla, con el gutter de 22dp de la maqueta.
 *
 * [pie] queda fijo abajo, fuera del scroll: es el equivalente al `margin-top:auto` que
 * el prototipo usa para empujar el botón de confirmar al fondo de la pantalla.
 */
@Composable
fun ColumnScope.CuerpoPantalla(
    pie: (@Composable () -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        content = contenido,
    )
    if (pie != null) {
        Column(Modifier.padding(horizontal = Pista.gutter).padding(top = 8.dp, bottom = 12.dp)) {
            pie()
        }
    }
}

/** `font:700 13px var(--disp);letter-spacing:1.5px;text-transform:uppercase;color:var(--mut)` */
@Composable
fun Seccion(texto: String, modifier: Modifier = Modifier, top: Dp = 22.dp) {
    Text(
        text = texto.uppercase(),
        style = disp(weight = 700, size = 13, color = Pista.mut, letterSpacing = 1.5.sp),
        modifier = modifier.padding(start = Pista.gutter, end = Pista.gutter, top = top),
    )
}

/** Tarjeta grande: `padding:18px;border-radius:var(--r);background:var(--sur)`. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    fondo: Color = Pista.sur,
    forma: RoundedCornerShape = Pista.r,
    padding: Dp = 18.dp,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fondo)
            .padding(padding),
        content = contenido,
    )
}

/** Fila de lista: `padding:14px 16px;border-radius:10px;background:var(--sur2)`. */
@Composable
fun Fila(
    modifier: Modifier = Modifier,
    fondo: Color = Pista.sur2,
    forma: RoundedCornerShape = Pista.rRow,
    onClick: (() -> Unit)? = null,
    paddingH: Dp = 16.dp,
    paddingV: Dp = 14.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    contenido: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fondo)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = paddingH, vertical = paddingV),
        verticalAlignment = verticalAlignment,
        content = contenido,
    )
}

/** Fila `clave · valor` de las listas de datos. */
@Composable
fun FilaDato(clave: String, valor: String, fondo: Color = Pista.sur) {
    Fila(fondo = fondo) {
        Text(clave, style = body(500, 14, Pista.mut), modifier = Modifier.weight(1f))
        Text(valor, style = body(600, 14))
    }
}

/** Botón lleno principal: acento sobre tinta del acento. */
@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fondo: Color = Pista.acc,
    tinta: Color = Pista.accInk,
    alto: Dp = 54.dp,
    tamano: Int = 17,
    habilitado: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(Pista.rs)
            .background(fondo)
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            texto.uppercase(),
            style = disp(700, tamano, tinta, letterSpacing = 1.sp),
            textAlign = TextAlign.Center,
        )
    }
}

/** Botón contorneado: `border:1px solid var(--line);background:transparent`. */
@Composable
fun BotonBorde(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tinta: Color = Pista.ink,
    borde: Color = Pista.line,
    alto: Dp = 50.dp,
    tamano: Int = 15,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(Pista.rs)
            .border(1.dp, borde, Pista.rs)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto.uppercase(), style = disp(700, tamano, tinta, letterSpacing = 0.8.sp))
    }
}

/** Chip de estado: `padding:5px 9px;border-radius:5px`. */
@Composable
fun Chip(
    texto: String,
    fondo: Color,
    tinta: Color,
    modifier: Modifier = Modifier,
    tamano: Int = 11,
) {
    Text(
        text = texto.uppercase(),
        style = disp(700, tamano, tinta, letterSpacing = 1.sp),
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(fondo)
            .padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

/**
 * Campo de texto de la maqueta: 52dp de alto, fondo `--field`, borde de 1px y
 * contorno de acento al enfocarse.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    alto: Dp = 52.dp,
    tamano: Int = 15,
    peso: Int = 500,
    esPassword: Boolean = false,
    tecladoNumerico: Boolean = false,
    tecladoDecimal: Boolean = false,
    tecladoEmail: Boolean = false,
    alinearCentro: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    fuenteDisplay: Boolean = false,
) {
    val interaction = remember { MutableTextFieldInteraction() }
    val enfocado by interaction.source.collectIsFocusedAsState()
    val estilo = if (fuenteDisplay) {
        disp(peso, tamano, Pista.ink, letterSpacing = 1.sp)
    } else {
        body(peso, tamano, Pista.ink)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(Pista.rs)
            .background(Pista.field)
            .border(
                width = if (enfocado) 2.dp else 1.dp,
                color = if (enfocado) Pista.acc else Pista.line,
                shape = Pista.rs,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = if (alinearCentro) Alignment.Center else Alignment.CenterStart,
    ) {
        if (valor.isEmpty()) {
            Text(
                placeholder,
                style = estilo.copy(color = Pista.mut.copy(alpha = 0.7f)),
                textAlign = if (alinearCentro) TextAlign.Center else TextAlign.Start,
            )
        }
        BasicTextField(
            value = valor,
            onValueChange = onValorChange,
            singleLine = true,
            textStyle = estilo.copy(textAlign = if (alinearCentro) TextAlign.Center else TextAlign.Start),
            cursorBrush = SolidColor(Pista.acc),
            interactionSource = interaction.source,
            visualTransformation = if (esPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = when {
                    esPassword -> KeyboardType.Password
                    tecladoNumerico -> KeyboardType.Number
                    tecladoDecimal -> KeyboardType.Decimal
                    tecladoEmail -> KeyboardType.Email
                    else -> KeyboardType.Text
                },
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction?.invoke() },
                onGo = { onImeAction?.invoke() },
                onSearch = { onImeAction?.invoke() },
                onSend = { onImeAction?.invoke() },
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Área de texto multilínea, para observaciones. */
@Composable
fun CampoLargo(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    lineas: Int = 2,
) {
    val interaction = remember { MutableTextFieldInteraction() }
    val enfocado by interaction.source.collectIsFocusedAsState()
    val estilo = body(500, 15, Pista.ink, lineHeight = 21.sp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((28 * lineas + 28).dp)
            .clip(Pista.rs)
            .background(Pista.field)
            .border(
                width = if (enfocado) 2.dp else 1.dp,
                color = if (enfocado) Pista.acc else Pista.line,
                shape = Pista.rs,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (valor.isEmpty()) {
            Text(placeholder, style = estilo.copy(color = Pista.mut.copy(alpha = 0.7f)))
        }
        BasicTextField(
            value = valor,
            onValueChange = onValorChange,
            textStyle = estilo,
            cursorBrush = SolidColor(Pista.acc),
            interactionSource = interaction.source,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Envoltorio para poder recordar un `MutableInteractionSource` sin repetirlo. */
class MutableTextFieldInteraction {
    val source = MutableInteractionSource()
}

/** Avatar circular; cae en un marcador de posición si el usuario no tiene foto. */
@Composable
fun Avatar(
    foto: Int?,
    tamano: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val base = modifier
        .size(tamano)
        .clip(CircleShape)
        .background(Pista.sur2)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    if (foto != null) {
        Image(
            painter = painterResource(foto),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = base,
        )
    } else {
        Box(base, contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = Pista.mut,
                modifier = Modifier.size(tamano * 0.6f),
            )
        }
    }
}

/** Foto tipo carnet, con esquinas redondeadas en lugar de círculo. */
@Composable
fun FotoCarnet(foto: Int?, ancho: Dp, alto: Dp, modifier: Modifier = Modifier) {
    val base = modifier
        .size(ancho, alto)
        .clip(RoundedCornerShape(10.dp))
        .background(Color.Black.copy(alpha = 0.12f))
    if (foto != null) {
        Image(
            painter = painterResource(foto),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = base,
        )
    } else {
        Box(base, contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = Pista.mut,
                modifier = Modifier.size(ancho * 0.6f),
            )
        }
    }
}

/** Barra de progreso fina del cupo de una actividad. */
@Composable
fun BarraCupo(fraccion: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Pista.line),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}

/** Columna con el gutter horizontal estándar y separación entre hijos. */
@Composable
fun ColumnaGutter(
    modifier: Modifier = Modifier,
    top: Dp = 10.dp,
    separacion: Dp = 8.dp,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Pista.gutter, end = Pista.gutter, top = top),
        verticalArrangement = Arrangement.spacedBy(separacion),
        content = contenido,
    )
}
