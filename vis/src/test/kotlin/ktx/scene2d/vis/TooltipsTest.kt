package ktx.scene2d.vis

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import ktx.scene2d.ApplicationTest
import ktx.scene2d.TOLERANCE

class TooltipsTest : ApplicationTest() {
  init {
    test("should create Tooltip") {
      val actor = Actor()
      val content = VisTable()

      val tooltip = actor.visTooltip(content)

      content shouldBe tooltip.content
    }

    test("should create Tooltip with init block") {
      val actor = Actor()
      var initInvoked: Boolean

      val tooltip =
        actor.visTooltip(VisTable()) {
          initInvoked = true
          fadeTime = 0.5f
        }

      tooltip.fadeTime shouldBe (0.5f plusOrMinus TOLERANCE)
      initInvoked shouldBe true
    }

    test("should create Tooltip with Label") {
      val actor = Actor()
      val text = "text tooltip"

      val tooltip = actor.visTextTooltip(text, Align.right)

      (tooltip.content as VisLabel).text.toString() shouldBe text
      (tooltip.content as VisLabel).labelAlign shouldBe Align.right
    }

    test("should create text Tooltip with init block") {
      val actor = Actor()
      val text = "text tooltip"
      var initInvoked: Boolean

      val tooltip =
        actor.visTextTooltip(text) {
          initInvoked = true
          fadeTime = 0.5f
        }

      tooltip.fadeTime shouldBe (0.5f plusOrMinus TOLERANCE)
      (tooltip.content as VisLabel).text.toString() shouldBe text
      initInvoked shouldBe true
    }
  }
}
