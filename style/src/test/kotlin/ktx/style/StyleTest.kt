package ktx.style

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip.TextTooltipStyle
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle
import com.badlogic.gdx.scenes.scene2d.ui.Tree.TreeStyle
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import ktx.style.TestEnum.TEST
import org.mockito.kotlin.mock

/**
 * Tests [Skin] building utilities.
 */
class StyleTest : FunSpec({
  test("should use valid default style name") {
    val skin = Skin()
    skin.add(defaultStyle, "Mock resource.")

    // Calling getter without resource name - should default to "default":
    val resource = skin[String::class.java]

    resource shouldNotBe null
    resource shouldBe "Mock resource."
    // If this test fails, libGDX changed name of default resources or removed the unnamed resource getter.
  }

  test("should create new skin") {
    val skin = skin()

    skin shouldNotBe null
  }

  test("should create new skin with init block") {
    val skin =
      skin {
        add("mock", "Test.")
      }

    skin shouldNotBe null
    skin.get("mock", String::class.java) shouldNotBe null
  }

  test("should configure skin exactly once") {
    val variable: Int

    skin {
      variable = 42
    }

    variable shouldBe 42
  }

  test("should create new skin with TextureAtlas") {
    val skin = skin(TextureAtlas())

    skin shouldNotBe null
  }

  test("should create new skin with TextureAtlas and init block") {
    val skin =
      skin(TextureAtlas()) {
        add("mock", "Test.")
      }

    skin shouldNotBe null
    skin.get("mock", String::class.java) shouldNotBe null
  }

  test("should configure skin with TextureAtlas exactly once") {
    val variable: Int

    skin(TextureAtlas()) {
      variable = 42
    }

    variable shouldBe 42
  }

  test("should register assets") {
    val skin = Skin()

    skin.register {
      add("mock", "Test.")
    }

    skin.get("mock", String::class.java) shouldNotBe null
  }

  test("should register assets exactly once") {
    val skin = Skin()
    val variable: Int

    skin.register {
      variable = 42
    }

    variable shouldBe 42
  }

  test("should extract resource with explicit reified type") {
    val skin = Skin()
    skin.add("mock", "Test.")

    val resource = skin.get<String>("mock")

    resource shouldBe "Test."
  }

  test("should extract resource with implicit reified type") {
    val skin = Skin()
    skin.add("mock", "Test.")

    val reified: String = skin["mock"]

    reified shouldBe "Test."
  }

  test("should extract optional resource with default style name") {
    val skin = Skin()

    skin.add(defaultStyle, "Test.")

    skin.optional<String>() shouldBe "Test."
  }

  test("should extract optional resource") {
    val skin = Skin()

    skin.add("mock", "Test.")

    skin.optional<String>("mock") shouldBe "Test."
  }

  test("should extract optional resource and return null") {
    val skin = Skin()

    skin.add("asset", "Test.")

    skin.optional<Color>("mock") shouldBe null
  }

  test("should extract resource with default style name") {
    val skin = Skin()

    skin[defaultStyle] = "Asset."

    skin.get<String>() shouldBe "Asset."
  }

  test("should add resources with brace operator") {
    val skin = Skin()

    skin["name"] = "Asset."

    skin.get<String>("name") shouldBe "Asset."
  }

  test("should add resource with brace operator with enum name") {
    val skin = Skin()

    skin[TEST] = "Test."

    skin.get<String>("TEST") shouldBe "Test."
  }

  test("should extract resource with brace operator with enum name") {
    val skin = Skin()
    skin["TEST"] = "Test."

    val resource: String = skin[TEST]

    resource shouldBe "Test."
  }

  test("should add and extract resource with brace operator with enum name") {
    val skin = Skin()

    skin[TEST] = "Test."
    val resource: String = skin[TEST]

    resource shouldBe "Test."
  }

  test("should add existing style to skin") {
    val skin = Skin()
    val existing = LabelStyle()

    skin.addStyle("mock", existing)

    val style = skin.get<LabelStyle>("mock")
    style shouldBeSameInstanceAs existing
  }

  test("should add existing style to skin with init block") {
    val skin = Skin()
    val existing = LabelStyle()

    skin.addStyle("mock", existing) {
      fontColor = Color.BLACK
    }

    val style = skin.get<LabelStyle>("mock")
    style shouldBeSameInstanceAs existing
    style.fontColor shouldBe Color.BLACK
  }

  test("should add resource with default style name") {
    val skin = Skin()

    skin.add("Test.")

    skin.get<String>() shouldBe "Test."
  }

  test("should add resource with default style name with plusAssign") {
    val skin = Skin()

    skin += "Test."

    skin.get<String>() shouldBe "Test."
  }

  test("should remove resource with default style name") {
    val skin = Skin()

    skin.add(defaultStyle, "Test.")

    skin.remove<String>()

    skin.has(defaultStyle, String::class.java) shouldBe false
  }

  test("should remove resource") {
    val skin = Skin()

    skin.add("mock", "Test.")

    skin.remove<String>("mock")

    skin.has("mock", String::class.java) shouldBe false
  }

  test("should check if resource is present") {
    val skin = Skin()

    skin.add("mock", "Test.")

    skin.has<String>("mock") shouldBe true
    skin.has<String>("mock-2") shouldBe false
  }

  test("should check if resource with default style name is present") {
    val skin = Skin()

    skin.add(defaultStyle, "Test.")

    skin.has<String>() shouldBe true
  }

  test("should return a map of all resources of a type") {
    val skin = Skin()

    skin.add("mock-1", "Test.")
    skin.add("mock-2", "Test 2.")

    skin.getAll<String>()!!.size shouldBe 2
  }

  test("should return null if resource is not in skin") {
    val skin = Skin()

    skin.getAll<String>() shouldBe null
  }

  test("should add color") {
    val skin = Skin()

    skin.color("black", red = 0f, green = 0f, blue = 0f)

    skin.getColor("black") shouldBe Color.BLACK
  }

  test("should add ButtonStyle") {
    val skin =
      skin {
        button {
          pressedOffsetX = 1f
        }
      }
    val style = skin.get<ButtonStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend ButtonStyle") {
    val skin =
      skin {
        button("base") {
          pressedOffsetX = 1f
        }
        button("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<ButtonStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add CheckBoxStyle") {
    val skin =
      skin {
        checkBox {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<CheckBoxStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend CheckBoxStyle") {
    val skin =
      skin {
        checkBox("base") {
          pressedOffsetX = 1f
        }
        checkBox("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<CheckBoxStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add ImageButtonStyle") {
    val skin =
      skin {
        imageButton {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<ImageButtonStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend ImageButtonStyle") {
    val skin =
      skin {
        imageButton("base") {
          pressedOffsetX = 1f
        }
        imageButton("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<ImageButtonStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add ImageTextButtonStyle") {
    val skin =
      skin {
        imageTextButton {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<ImageTextButtonStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend ImageTextButtonStyle") {
    val skin =
      skin {
        imageTextButton("base") {
          pressedOffsetX = 1f
        }
        imageTextButton("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<ImageTextButtonStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add LabelStyle") {
    val skin =
      skin {
        label {
          fontColor = Color.BLACK
        }
      }

    val style = skin.get<LabelStyle>(defaultStyle)
    style.fontColor shouldBe Color.BLACK
  }

  test("should extend LabelStyle") {
    val skin =
      skin {
        label("base") {
          fontColor = Color.BLACK
        }
        label("new", extend = "base") {
        }
      }

    val style = skin.get<LabelStyle>("new")
    style.fontColor shouldBe Color.BLACK
  }

  test("should add ListStyle") {
    val skin =
      skin {
        list {
          fontColorSelected = Color.BLACK
        }
      }

    val style = skin.get<ListStyle>(defaultStyle)
    style.fontColorSelected shouldBe Color.BLACK
  }

  test("should extend ListStyle") {
    val skin =
      skin {
        list("base") {
          fontColorSelected = Color.BLACK
        }
        list("new", extend = "base") {
          fontColorUnselected = Color.CYAN
        }
      }

    val style = skin.get<ListStyle>("new")
    style.fontColorSelected shouldBe Color.BLACK
    style.fontColorUnselected shouldBe Color.CYAN
  }

  test("should add ProgressBarStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        progressBar {
          background = drawable
        }
      }

    val style = skin.get<ProgressBarStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend ProgressBarStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        progressBar("base") {
          background = drawable
        }
        progressBar("new", extend = "base") {
          knob = drawable
        }
      }

    val style = skin.get<ProgressBarStyle>("new")
    style.background shouldBe drawable
    style.knob shouldBe drawable
  }

  test("should add ScrollPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        scrollPane {
          background = drawable
        }
      }

    val style: ScrollPaneStyle = skin.get()
    style.background shouldBe drawable
  }

  test("should extend ScrollPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        scrollPane("base") {
          background = drawable
        }
        scrollPane("new", extend = "base") {
          corner = drawable
        }
      }

    val style = skin.get<ScrollPaneStyle>("new")
    style.background shouldBe drawable
    style.corner shouldBe drawable
  }

  test("should add SelectBoxStyle") {
    val skin =
      skin {
        selectBox {
          fontColor = Color.CYAN
        }
      }

    val style = skin.get<SelectBoxStyle>(defaultStyle)
    style.fontColor shouldBe Color.CYAN
  }

  test("should extend SelectBoxStyle") {
    val skin =
      skin {
        list() // Necessary for copy constructor.
        scrollPane()
        selectBox("base") {
          listStyle = it[defaultStyle]
          scrollStyle = it[defaultStyle]
          fontColor = Color.CYAN
        }
        selectBox("new", extend = "base") {
          disabledFontColor = Color.BLACK
        }
      }

    val style = skin.get<SelectBoxStyle>("new")
    style.fontColor shouldBe Color.CYAN
    style.disabledFontColor shouldBe Color.BLACK
  }

  test("should add SliderStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        slider {
          background = drawable
        }
      }

    val style = skin.get<SliderStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend SliderStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        slider("base") {
          background = drawable
        }
        slider("new", extend = "base") {
          knob = drawable
        }
      }

    val style = skin.get<SliderStyle>("new")
    style.background shouldBe drawable
    style.knob shouldBe drawable
  }

  test("should add SplitPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        splitPane {
          handle = drawable
        }
      }

    val style = skin.get<SplitPaneStyle>(defaultStyle)
    style.handle shouldBe drawable
  }

  test("should extend SplitPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        splitPane("base") {
          handle = drawable
        }
        splitPane("new", extend = "base")
      }

    val style = skin.get<SplitPaneStyle>("new")
    style.handle shouldBe drawable
  }

  test("should add TextButtonStyle") {
    val skin =
      skin {
        textButton {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<TextButtonStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend TextButtonStyle") {
    val skin =
      skin {
        textButton("base") {
          pressedOffsetX = 1f
        }
        textButton("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<TextButtonStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add TextFieldStyle") {
    val skin =
      skin {
        textField {
          fontColor = Color.CYAN
        }
      }

    val style = skin.get<TextFieldStyle>(defaultStyle)
    style.fontColor shouldBe Color.CYAN
  }

  test("should extend TextFieldStyle") {
    val skin =
      skin {
        textField("base") {
          fontColor = Color.CYAN
        }
        textField("new", extend = "base") {
          disabledFontColor = Color.BLACK
        }
      }

    val style = skin.get<TextFieldStyle>("new")
    style.fontColor shouldBe Color.CYAN
    style.disabledFontColor shouldBe Color.BLACK
  }

  test("should add TextTooltipStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        textTooltip {
          background = drawable
        }
      }

    val style = skin.get<TextTooltipStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend TextTooltipStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        textTooltip("base") {
          label = it.label() // Necessary for copy constructor.
          background = drawable
        }
        textTooltip("new", extend = "base") {
          wrapWidth = 1f
        }
      }

    val style = skin.get<TextTooltipStyle>("new")
    style.background shouldBe drawable
    style.wrapWidth shouldBe 1f
  }

  test("should add TouchpadStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        touchpad {
          knob = drawable
        }
      }

    val style = skin.get<TouchpadStyle>(defaultStyle)
    style.knob shouldBe drawable
  }

  test("should extend TouchpadStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        touchpad("base") {
          knob = drawable
        }
        touchpad("new", extend = "base") {
          background = drawable
        }
      }

    val style = skin.get<TouchpadStyle>("new")
    style.knob shouldBe drawable
    style.background shouldBe drawable
  }

  test("should add TreeStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        tree {
          plus = drawable
        }
      }

    val style = skin.get<TreeStyle>(defaultStyle)
    style.plus shouldBe drawable
  }

  test("should extend TreeStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        tree("base") {
          plus = drawable
        }
        tree("new", extend = "base") {
          minus = drawable
        }
      }

    val style = skin.get<TreeStyle>("new")
    style.plus shouldBe drawable
    style.minus shouldBe drawable
  }

  test("should add WindowStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        window {
          background = drawable
        }
      }

    val style = skin.get<WindowStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend WindowStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        window("base") {
          background = drawable
        }
        window("new", extend = "base") {
          stageBackground = drawable
        }
      }

    val style = skin.get<WindowStyle>("new")
    style.background shouldBe drawable
    style.stageBackground shouldBe drawable
  }
})

/** For [StyleTest] tests. */
internal enum class TestEnum {
  TEST,
}
