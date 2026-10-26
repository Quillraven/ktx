package ktx.scene2d

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Window
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.mock

class StageWidgetTest : ApplicationTest() {
  init {
    test("should add Actor to the Stage") {
      val stage = Stage(mock(), mock())
      val actor = Actor()
      val stageWidget = StageWidget(stage)

      stageWidget.storeActor(actor)

      (actor in stage.actors) shouldBe true
      (actor in stage.root.children) shouldBe true
    }

    test("should add multiple top-level actors to the Stage within actors block") {
      val stage = Stage(mock(), mock())
      val label: Label
      val table: Table
      val button: Button

      stage.actors {
        label = label("Test")
        table =
          table {
            button = button()
          }
        this.stage shouldBeSameInstanceAs stage
      }

      (label in stage.actors) shouldBe true
      (label in stage.root.children) shouldBe true
      (table in stage.actors) shouldBe true
      (table in stage.root.children) shouldBe true
      (button in stage.actors) shouldBe false
      (button in stage.root.children) shouldBe false
      (button in table.children) shouldBe true
    }

    test("should configure Stage exactly once") {
      val stage = Stage(mock(), mock())
      val variable: Int

      stage.actors {
        variable = 42
      }

      variable shouldBe 42
    }

    test("should store root actors in Stage when using actors block") {
      val stage = Stage(mock(), mock())
      val window: Window
      val dialog: Dialog
      val label: Label

      stage.actors {
        window = window("Test")
        dialog =
          dialog("Test") {
            label = label("Test")
          }
      }

      (window in stage.actors) shouldBe true
      (window in stage.root.children) shouldBe true
      (dialog in stage.actors) shouldBe true
      (dialog in stage.root.children) shouldBe true
      (label in stage.actors) shouldBe false
      (label in stage.root.children) shouldBe false
      (label in dialog.children) shouldBe true
    }
  }
}
