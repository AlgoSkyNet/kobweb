package multimodule.core.components.widgets

import androidx.compose.runtime.*
import androidx.compose.web.events.SyntheticMouseEvent
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import multimodule.core.G
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

val TextButtonStyle by ComponentStyle.base {
    Modifier
        .width(G.Ui.Width.Medium)
        .margin(10.px)
        .padding(4.px)
        .fontSize(G.Ui.Text.Medium)
}

@Composable
fun TextButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: (SyntheticMouseEvent) -> Unit) {
    Button(onClick = onClick, TextButtonStyle.toModifier().then(modifier), enabled = enabled) {
        Text(text)
    }
}