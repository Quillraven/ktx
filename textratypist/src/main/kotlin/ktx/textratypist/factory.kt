package ktx.textratypist

import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node
import com.github.tommyettinger.textra.TextraLabel
import com.github.tommyettinger.textra.TypingLabel
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @param text will be displayed on the label.
 * @param style name of the widget style. Defaults to [defaultStyle].
 * @param skin [Skin] instance that contains the widget style. Defaults to [Scene2DSkin.defaultSkin].
 * @param init will be invoked with the widget as "this". Consumes actor container (usually a [Cell] or [Node]) that
 * contains the widget. Might consume the actor itself if this group does not keep actors in dedicated containers.
 * Inlined.
 * @return a [TextraLabel] instance added to this group.
 */
@OptIn(ExperimentalContracts::class)
inline fun <S> KWidget<S>.textraLabel(
  text: String,
  style: String = defaultStyle,
  skin: Skin = Scene2DSkin.defaultSkin,
  init: (@Scene2dDsl TextraLabel).(S) -> Unit = {},
): TextraLabel {
  contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
  return actor(TextraLabel(text, skin, style), init)
}

/**
 * @param text will be displayed on the label.
 * @param style name of the widget style. Defaults to [defaultStyle].
 * @param skin [Skin] instance that contains the widget style. Defaults to [Scene2DSkin.defaultSkin].
 * @param init will be invoked with the widget as "this". Consumes actor container (usually a [Cell] or [Node]) that
 * contains the widget. Might consume the actor itself if this group does not keep actors in dedicated containers.
 * Inlined.
 * @return a [TypingLabel] instance added to this group.
 */
@OptIn(ExperimentalContracts::class)
inline fun <S> KWidget<S>.typingLabel(
  text: String,
  style: String = defaultStyle,
  skin: Skin = Scene2DSkin.defaultSkin,
  init: (@Scene2dDsl TypingLabel).(S) -> Unit = {},
): TypingLabel {
  contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
  return actor(TypingLabel(text, skin, style), init)
}
