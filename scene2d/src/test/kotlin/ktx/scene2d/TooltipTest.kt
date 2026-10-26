package ktx.scene2d

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests extension methods that allow to add [Tooltip] instances to all actors.
 */
class TooltipTest : ApplicationTest() {
  init {
    test("should add TextTooltip") {
      val actor = Actor()

      val tooltip = actor.textTooltip("Test.")

      tooltip.shouldNotBeNull()
      (tooltip in actor.listeners) shouldBe true
      tooltip.actor.text.toString() shouldBe "Test."
    }

    test("should add TextTooltip with init block") {
      val actor = Actor()
      val variable: Int

      val tooltip =
        actor.textTooltip("Test.") {
          // Changing Label color:
          color = Color.BLUE
          variable = 42
        }

      tooltip.shouldNotBeNull()
      (tooltip in actor.listeners) shouldBe true
      tooltip.actor.text.toString() shouldBe "Test."
      tooltip.actor.color shouldBe Color.BLUE
      variable shouldBe 42
    }

    test("should add Tooltip with init block") {
      val actor = Actor()
      val variable: Int

      val tooltip =
        actor.tooltip {
          // Changing Table color:
          color = Color.BLUE
          // Adding child to Table content:
          label("Test.")
          variable = 42
        }

      tooltip.shouldNotBeNull()
      (tooltip in actor.listeners) shouldBe true
      (tooltip.actor.children.first() as Label).text.toString() shouldBe "Test."
      tooltip.actor.color shouldBe Color.BLUE
      variable shouldBe 42
    }
  }
}
