package ktx.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests general [Actor] utilities.
 */
class ActorsTest : FunSpec({
  val floatTolerance = 0.00001f

  test("should report if actor on stage is shown") {
    val stage = getMockStage()
    val actor = Actor()

    stage.addActor(actor)

    actor.stage.shouldNotBeNull()
    actor.isShown() shouldBe true
  }

  test("should report if actor without stage is shown") {
    val actor = Actor()

    actor.isShown() shouldBe false
  }

  test("should report if null actor is shown") {
    val actor: Actor? = null

    actor.isShown() shouldBe false
  }

  test("should set position with ints") {
    val actor = Actor()

    actor.setPosition(10, 20)

    actor.x shouldBe (10f plusOrMinus floatTolerance)
    actor.y shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should center actor with normalization") {
    val actor = Actor()
    actor.setSize(100f, 100f)
    val newSize = 201f

    actor.centerPosition(width = newSize, height = newSize, normalize = true)

    actor.x shouldBe (50f plusOrMinus floatTolerance)
    actor.y shouldBe (50f plusOrMinus floatTolerance)
  }

  test("should center actor without normalization") {
    val actor = Actor()
    actor.setSize(100f, 100f)
    val newSize = 201f

    actor.centerPosition(width = newSize, height = newSize, normalize = false)

    actor.x shouldBe (50.5f plusOrMinus floatTolerance)
    actor.y shouldBe (50.5f plusOrMinus floatTolerance)
  }

  test("should check if actor is in group with in operator") {
    val actor = Actor()
    val group = Group()

    (actor in group) shouldBe false
    group.addActor(actor)
    (actor in group) shouldBe true
    group.removeActor(actor)
    (actor in group) shouldBe false
  }

  test("should add actors to group with +=") {
    val actor = Actor()
    val group = Group()

    group += actor

    actor.parent.shouldNotBeNull()
    (actor in group) shouldBe true
    (actor in group.children) shouldBe true
  }

  test("should remove actors from group with -=") {
    val actor = Actor()
    val group = Group()
    group.addActor(actor)

    group -= actor

    actor.parent.shouldBeNull()
    (actor in group) shouldBe false
    (actor in group.children) shouldBe false
  }

  test("should check if actor is on stage with in operator") {
    val actor = Actor()
    val stage = getMockStage()

    (actor in stage) shouldBe false
    stage.addActor(actor)
    (actor in stage) shouldBe true
    actor.remove()
    (actor in stage) shouldBe false
  }

  test("should add actors to stage with +=") {
    val actor = Actor()
    val stage = getMockStage()

    stage += actor

    actor.parent.shouldNotBeNull()
    (actor in stage) shouldBe true
    (actor in stage.root) shouldBe true
    stage.root.hasChildren() shouldBe true
    actor.parent shouldBeSameInstanceAs stage.root
  }

  test("should remove actors from stage with -=") {
    val actor = Actor()
    val stage = getMockStage()
    stage.addActor(actor)

    stage -= actor

    actor.parent.shouldBeNull()
    (actor in stage) shouldBe false
    (actor in stage.root) shouldBe false
    stage.root.hasChildren() shouldBe false
  }

  test("should modify actor's color alpha value") {
    val actor = Actor()
    actor.color = Color(1f, 1f, 1f, 1f)
    val color = actor.color

    actor.alpha = 0.5f

    color.a shouldBe (0.5f plusOrMinus floatTolerance)
    actor.alpha shouldBe (0.5f plusOrMinus floatTolerance)
  }

  test("should modify stage root actor's color alpha value") {
    val stage = getMockStage()
    stage.root.color = Color(1f, 1f, 1f, 1f)
    val color = stage.root.color

    stage.alpha = 0.5f

    color.a shouldBe (0.5f plusOrMinus floatTolerance)
    stage.alpha shouldBe (0.5f plusOrMinus floatTolerance)
  }

  test("should report valid alpha constants") {
    MIN_ALPHA shouldBe 0f
    MAX_ALPHA shouldBe 1f
  }

  test("should change keyboard focus") {
    val stage = getMockStage()
    val actor = Actor()
    stage.addActor(actor)

    actor.setKeyboardFocus()
    actor shouldBeSameInstanceAs stage.keyboardFocus

    actor.setKeyboardFocus(false)
    actor shouldNotBe stage.keyboardFocus
    stage.keyboardFocus.shouldBeNull()

    val newFocus = Actor()
    stage.keyboardFocus = newFocus
    actor.setKeyboardFocus(false) // Should not clear or change stage's focused actor if not currently focused.
    stage.keyboardFocus.shouldNotBeNull()
    stage.keyboardFocus shouldBeSameInstanceAs newFocus
  }

  test("should change scroll focus") {
    val stage = getMockStage()
    val actor = Actor()
    stage.addActor(actor)

    actor.setScrollFocus()
    actor shouldBe stage.scrollFocus

    actor.setScrollFocus(false)
    actor shouldNotBe stage.scrollFocus
    stage.scrollFocus.shouldBeNull()

    val newFocus = Actor()
    stage.scrollFocus = newFocus
    actor.setScrollFocus(false) // Should not clear or change stage's focused actor if not currently focused.
    stage.scrollFocus.shouldNotBeNull()
    stage.scrollFocus shouldBe newFocus
  }

  test("should keep actor within parent's bounds") {
    val parent = Group()
    val actor = Actor()
    actor.setSize(100f, 100f)
    actor.setPosition(4000f, 4000f)
    parent.setSize(800f, 600f)

    parent.addActor(actor)
    (actor.x + actor.width > parent.width) shouldBe true
    (actor.y + actor.height > parent.height) shouldBe true

    actor.keepWithinParent()
    (actor.x + actor.width > parent.width) shouldBe false
    (actor.y + actor.height > parent.height) shouldBe false
    (actor.x < 0f) shouldBe false
    (actor.y < 0f) shouldBe false
    (actor.x + actor.width) shouldBe (parent.width plusOrMinus floatTolerance)
    (actor.y + actor.height) shouldBe (parent.height plusOrMinus floatTolerance)

    actor.setPosition(-4000f, -4000f)
    actor.keepWithinParent()
    (actor.x + actor.width > parent.width) shouldBe false
    (actor.y + actor.height > parent.height) shouldBe false
    (actor.x < 0f) shouldBe false
    (actor.y < 0f) shouldBe false
    actor.x shouldBe (0f plusOrMinus floatTolerance)
    actor.y shouldBe (0f plusOrMinus floatTolerance)
  }

  test("should keep actor within stage bounds") {
    val stage = getMockStage()
    val actor = Actor()
    actor.setSize(100f, 100f)
    actor.setPosition(4000f, 4000f)

    stage.addActor(actor)
    (actor.x + actor.width > stage.width) shouldBe true
    (actor.y + actor.height > stage.height) shouldBe true

    actor.keepWithinParent()
    (actor.x + actor.width > stage.width) shouldBe false
    (actor.y + actor.height > stage.height) shouldBe false
    (actor.x < 0f) shouldBe false
    (actor.y < 0f) shouldBe false
    (actor.x + actor.width) shouldBe (stage.width plusOrMinus floatTolerance)
    (actor.y + actor.height) shouldBe (stage.height plusOrMinus floatTolerance)

    actor.setPosition(-4000f, -4000f)
    actor.keepWithinParent()
    (actor.x + actor.width > stage.width) shouldBe false
    (actor.y + actor.height > stage.height) shouldBe false
    (actor.x < 0f) shouldBe false
    (actor.y < 0f) shouldBe false
    actor.x shouldBe (0f plusOrMinus floatTolerance)
    actor.y shouldBe (0f plusOrMinus floatTolerance)
  }
})
