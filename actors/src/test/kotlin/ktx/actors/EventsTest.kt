package ktx.actors

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type.keyDown
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type.keyTyped
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type.keyUp
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchDown
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchUp
import com.badlogic.gdx.scenes.scene2d.ui.Tree
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent.Type.keyboard
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent.Type.scroll
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.mock
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tests events and listeners utilities.
 */
@Suppress("UNUSED_PARAMETER", "UNUSED_ANONYMOUS_PARAMETER") // Unused lambda parameters showcase the listeners API.
class EventsTest : FunSpec({
  test("should attach ChangeListener") {
    val actor = Actor()
    var changed = false

    val listener = actor.onChange { changed = true }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    actor.fire(ChangeEvent())
    changed shouldBe true
  }

  test("should attach ChangeListener consuming ChangeEvent") {
    val actor = Actor()
    var changed = false

    val listener = actor.onChangeEvent { event -> changed = true }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    actor.fire(ChangeEvent())
    changed shouldBe true
  }

  test("should attach ClickListener") {
    val actor = Actor()

    val listener = actor.onClick { }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach ClickListener consuming InputEvent") {
    val actor = Actor()

    val listener = actor.onClickEvent { event -> }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach ClickListener consuming InputEvent with local coordinates") {
    val actor = Actor()

    val listener = actor.onClickEvent { event, x, y -> }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach ClickListener triggered on enter events") {
    val actor = Actor()

    val listener = actor.onEnter { }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach InputListener triggered on exit events") {
    val actor = Actor()

    val listener = actor.onExit { }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach ClickListener consuming onEnter events with local coordinates") {
    val actor = Actor()

    val listener = actor.onEnterEvent { event, x, y -> }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach ClickListener consuming onExit events with local coordinates") {
    val actor = Actor()

    val listener = actor.onExitEvent { event, x, y -> }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach ChangeListener to a Tree consuming Selection with the selected Nodes") {
    val style = mock<Tree.TreeStyle>()
    style.plus = mock()
    style.minus = mock()
    val tree = Tree<SampleNode, Any>(style)

    val listener = tree.onSelectionChange {}

    listener.shouldNotBeNull()
    (listener in tree.listeners) shouldBe true
  }

  test("should invoke attached ChangeListener when tree Selection changes") {
    val style = mock<Tree.TreeStyle>()
    style.plus = mock()
    style.minus = mock()
    val tree = Tree<SampleNode, Any>(style)
    tree.selection.setProgrammaticChangeEvents(true)
    val nodes = Array(3) { SampleNode() }
    val selected = SampleNode()
    nodes.forEach(tree::add)
    tree.add(selected)
    val executions = AtomicInteger()
    val selectedNodes = mutableListOf<SampleNode>()
    tree.selection.clear()
    tree.onSelectionChange { selection ->
      executions.incrementAndGet()
      selectedNodes.clear()
      selectedNodes.addAll(selection.items())
    }

    tree.selection.add(selected)

    executions.get() shouldBe 1
    selectedNodes.size shouldBe 1
    selectedNodes.first() shouldBeSameInstanceAs selected
  }

  test("should attach InputListener for touchDown") {
    val actor = Actor()

    val listener = actor.onTouchDown { }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach InputListener for touchUp") {
    val actor = Actor()

    val listener = actor.onTouchUp { }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach InputListener consuming InputEvent for touch events") {
    val actor = Actor()

    val listener =
      actor.onTouchEvent(
        onDown = { event -> },
        onUp = { event -> },
      )

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach InputListener consuming InputEvent for touch events with local coordinates") {
    val actor = Actor()

    val listener =
      actor.onTouchEvent(
        onDown = { event, x, y -> },
        onUp = { event, x, y -> },
      )

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach InputListener consuming InputEvent for touch events with local coordinates, pointer and button") {
    val actor = Actor()

    val listener =
      actor.onTouchEvent(
        onDown = { event, x, y, pointer, button -> },
        onUp = { event, x, y, pointer, button -> },
      )

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
  }

  test("should attach InputListener and trigger touchDown event") {
    val actor = Actor()
    var result = false

    val listener = actor.onTouchEvent { event -> result = event.type == touchDown }
    listener.touchDown(InputEvent().apply { type = touchDown }, 0f, 0f, 0, 0)

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    result shouldBe true
  }

  test("should attach InputListener and trigger touchUp event") {
    val actor = Actor()
    var result = false

    val listener = actor.onTouchEvent { event -> result = event.type == touchUp }
    listener.touchDown(InputEvent().apply { type = touchUp }, 0f, 0f, 0, 0)

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    result shouldBe true
  }

  test("should attach InputListener and trigger touchDown event with local coordinates, pointer and button") {
    val actor = Actor()
    var result = false

    val listener = actor.onTouchEvent { event, x, y, pointer, button -> result = event.type == touchDown }
    listener.touchDown(InputEvent().apply { type = touchDown }, 0f, 0f, 0, 0)

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    result shouldBe true
  }

  test("should attach InputListener and trigger touchUp event with local coordinates, pointer and button") {
    val actor = Actor()
    var result = false

    val listener = actor.onTouchEvent { event, x, y, pointer, button -> result = event.type == touchUp }
    listener.touchDown(InputEvent().apply { type = touchUp }, 0f, 0f, 0, 0)

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    result shouldBe true
  }

  test("should attach InputListener and trigger touchDown event with local coordinates") {
    val actor = Actor()
    var result = false

    val listener = actor.onTouchEvent { event, x, y -> result = event.type == touchDown }
    listener.touchDown(InputEvent().apply { type = touchDown }, 0f, 0f, 0, 0)

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    result shouldBe true
  }

  test("should attach InputListener and trigger touchUp event with local coordinates") {
    val actor = Actor()
    var result = false

    val listener = actor.onTouchEvent { event, x, y -> result = event.type == touchUp }
    listener.touchDown(InputEvent().apply { type = touchUp }, 0f, 0f, 0, 0)

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    result shouldBe true
  }

  test("should attach key listener") {
    val actor = Actor()
    var typed: Char? = null

    val listener = actor.onKey { key -> typed = key }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    typed.shouldBeNull()
    val event = InputEvent()
    event.character = 'a'
    event.type = keyTyped
    actor.fire(event)
    typed shouldBe 'a'
  }

  test("should attach key listener consuming InputEvent") {
    val actor = Actor()
    var typed: Char? = null

    val listener = actor.onKeyEvent { event, key -> typed = key }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    typed.shouldBeNull()
    val event = InputEvent()
    event.character = 'a'
    event.type = keyTyped
    actor.fire(event)
    typed shouldBe 'a'
  }

  test("should attach key down listener") {
    val actor = Actor()
    var pressed: Int? = null

    val listener = actor.onKeyDown { keyCode -> pressed = keyCode }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    pressed.shouldBeNull()
    val event = InputEvent()
    event.keyCode = Keys.A
    event.type = keyDown
    actor.fire(event)
    pressed shouldBe Keys.A
  }

  test("should attach key down listener consuming InputEvent") {
    val actor = Actor()
    var pressed: Int? = null

    val listener = actor.onKeyDownEvent { event, keyCode -> pressed = keyCode }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    pressed.shouldBeNull()
    val event = InputEvent()
    event.keyCode = Keys.A
    event.type = keyDown
    actor.fire(event)
    pressed shouldBe Keys.A
  }

  test("should attach key up listener") {
    val actor = Actor()
    var released: Int? = null

    val listener = actor.onKeyUp { keyCode -> released = keyCode }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true

    released.shouldBeNull()
    val event = InputEvent()
    event.keyCode = Keys.A
    event.type = keyUp
    actor.fire(event)
    released shouldBe Keys.A
  }

  test("should attach key up listener consuming InputEvent") {
    val actor = Actor()
    var released: Int? = null

    val listener = actor.onKeyUpEvent { event, keyCode -> released = keyCode }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true

    released.shouldBeNull()
    val event = InputEvent()
    event.keyCode = Keys.A
    event.type = keyUp
    actor.fire(event)
    released shouldBe Keys.A
  }

  test("should attach scroll focus listener") {
    val actor = Actor()
    var focused = false

    val listener = actor.onScrollFocus { focused = it }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    val event = FocusEvent()
    event.type = scroll
    event.isFocused = true
    actor.fire(event)
    focused shouldBe true
  }

  test("should attach scroll focus listener consuming FocusEvent") {
    val actor = Actor()
    var focused = false

    val listener = actor.onScrollFocusEvent { event -> focused = event.isFocused }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    val event = FocusEvent()
    event.type = scroll
    event.isFocused = true
    actor.fire(event)
    focused shouldBe true
  }

  test("should attach keyboard focus listener") {
    val actor = Actor()
    var focused = false

    val listener = actor.onKeyboardFocus { focused = it }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    val event = FocusEvent()
    event.type = keyboard
    event.isFocused = true
    actor.fire(event)
    focused shouldBe true
  }

  test("should attach keyboard focus listener consuming FocusEvent") {
    val actor = Actor()
    var focused = false

    val listener = actor.onKeyboardFocusEvent { event -> focused = event.isFocused }

    listener.shouldNotBeNull()
    (listener in actor.listeners) shouldBe true
    val event = FocusEvent()
    event.type = keyboard
    event.isFocused = true
    actor.fire(event)
    focused shouldBe true
  }
})

class SampleNode : Tree.Node<SampleNode, Any, Actor>(mock())

@Suppress("unused", "ClassName")
class `should extend KtxInputListener with no methods overridden` : KtxInputListener() {
  // Guarantees all KtxInputListener methods are optional to implement.
}
