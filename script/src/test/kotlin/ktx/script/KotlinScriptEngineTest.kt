package ktx.script

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.reflect.KClass
import com.badlogic.gdx.utils.Array as GdxArray

/**
 * Tests the [KotlinScriptEngine].
 */
class KotlinScriptEngineTest : FunSpec({
  val engine = KotlinScriptEngine()

  beforeSpec {
    Gdx.files = Lwjgl3Files()
  }

  afterSpec {
    Gdx.files = null
  }

  test("should evaluate Kotlin scripts") {
    // When:
    val result = engine.evaluateAs<String>("\"42\"")

    // Then:
    result shouldBe "42"
  }

  test("should import class") {
    // When:
    engine.import("ktx.script.Data")

    // Then:
    engine.evaluateAs<Data>("""Data("test")""") shouldBe Data("test")
  }

  test("should import class with alias") {
    // When:
    engine.import("ktx.script.Data", alias = "Alias")

    // Then:
    engine.evaluateAs<Data>("""Alias("test")""") shouldBe Data("test")
  }

  test("should import classes") {
    // When:
    engine.importAll("ktx.script.Data")

    // Then:
    engine.evaluateAs<Data>("""Data("test")""") shouldBe Data("test")
  }

  test("should import classes with aliases") {
    // When:
    engine.importAll("ktx.script.Data as Alias")

    // Then:
    engine.evaluateAs<Data>("""Alias("test")""") shouldBe Data("test")
  }

  test("should import classes from iterable") {
    // When:
    engine.importAll(listOf("ktx.script.Data"))

    // Then:
    engine.evaluateAs<Data>("""Data("test")""") shouldBe Data("test")
  }

  test("should import classes with aliases from iterable") {
    // When:
    engine.importAll(listOf("ktx.script.Data as Alias"))

    // Then:
    engine.evaluateAs<Data>("""Alias("test")""") shouldBe Data("test")
  }

  test("should set script package") {
    // When:
    engine.setPackage("ktx.script")

    // Then: should access classes without an import:
    engine.evaluateAs<KClass<*>>("KotlinScriptEngineTest::class") shouldBe KotlinScriptEngineTest::class
  }

  test("should evaluate script") {
    // When:
    val result = engine.evaluate("42")

    // Then:
    result shouldBe 42
  }

  test("should throw exception if unable to evaluate script") {
    // Expect:
    shouldThrow<ScriptEngineException> {
      engine.evaluate("import")
    }
  }

  test("should evaluate script from file") {
    // Given:
    val file = Gdx.files.classpath("ktx/script/script.kts")

    // When:
    val result = engine.evaluate(file)

    // Then:
    result shouldBe "test"
  }

  test("should throw exception if unable to evaluate script from file") {
    // Given:
    val file = Gdx.files.classpath("ktx/script/broken.script")

    // Expect:
    shouldThrow<ScriptEngineException> {
      engine.evaluate(file)
    }
  }

  test("should evaluate script and return its result") {
    // When:
    val result = engine.evaluateAs<Int>("42")

    // Then:
    result shouldBe 42
  }

  test("should throw exception if the script result does not match the expected type") {
    // Expect:
    shouldThrow<ClassCastException> {
      engine.evaluateAs<String>("1")
    }
  }

  test("should evaluate script from file and return its result") {
    // Given:
    val file = Gdx.files.classpath("ktx/script/script.kts")

    // When:
    val result = engine.evaluateAs<String>(file)

    // Then:
    result shouldBe "test"
  }

  test("should throw exception if the result of script from file does not match the expected type") {
    // Given:
    val file = Gdx.files.classpath("ktx/script/script.kts")

    // Expect:
    shouldThrow<ClassCastException> {
      engine.evaluateAs<Int>(file)
    }
  }

  test("should execute script with custom functions, lambdas and classes") {
    // Given:
    engine.setPackage("ktx.script")
    engine.import("com.badlogic.gdx.utils.Array", alias = "GdxArray")

    // When:
    val result =
      engine.evaluate(
        """
        class Helper {
          fun toArray(vararg objects: Any) = GdxArray.with(*objects)
        }
        fun data(text: String) = Data(text)
        val lambda: () -> String = { "test" }

        Helper().toArray(data(lambda()), data(lambda()), data(lambda()))
        """.trimIndent(),
      )

    // Then:
    result shouldBe GdxArray.with(Data("test"), Data("test"), Data("test"))
  }

  test("should execute script with a receiver") {
    // Given:
    val receiver = Data(text = "")

    // When:
    engine.evaluateOn(
      receiver,
      """
      text = "test"
      """.trimIndent(),
    )

    // Then:
    receiver.text shouldBe "test"
  }

  test("should execute script with a receiver and an import") {
    // Given:
    val receiver = Data(text = "")

    // When:
    engine.evaluateOn(
      receiver,
      """
      import com.badlogic.gdx.Gdx

      text = "test"
      """.trimIndent(),
    )

    // Then:
    receiver.text shouldBe "test"
  }

  test("should execute script from a file with a receiver") {
    // Given:
    val receiver = Data(text = "")
    val file = Gdx.files.classpath("ktx/script/receiver")

    // When:
    engine.evaluateOn(receiver, file)

    // Then:
    receiver.text shouldBe "test"
  }

  test("should assign context variable") {
    // Given:
    val variable = Data("test")

    // When:
    engine["variable"] = variable

    // Then:
    engine.get<Any>("variable") shouldBeSameInstanceAs variable
    engine.evaluateAs<String>("variable.text") shouldBe "test"
  }

  test("should allow to modify context variable") {
    // Given:
    val variable = Data("test")
    engine["variable"] = variable

    // When:
    engine.evaluate(
      """
      variable.text = "new"
      """.trimIndent(),
    )

    // Then:
    variable.text shouldBe "new"
  }

  test("should remove context variable") {
    // Given:
    val variable = Data("test")
    engine["variable"] = variable

    // When:
    val removed = engine.remove("variable")

    // Then:
    engine.get<Any>("variable").shouldBeNull()
    removed shouldBeSameInstanceAs variable
  }

  test("should not throw an exception when removing an absent variable") {
    // When:
    val result = engine.remove("absent")

    // Then:
    result.shouldBeNull()
  }
})

data class Data(
  var text: String,
)
