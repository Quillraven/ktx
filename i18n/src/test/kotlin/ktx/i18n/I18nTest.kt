package ktx.i18n

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import ktx.i18n.BundleEnum.key
import ktx.i18n.BundleEnum.keyWithArgument
import org.mockito.kotlin.mock
import java.io.File

/**
 * Tests internationalization-related classes and functions stored in *i18n.kt*.
 */
class I18nTest : FunSpec({
  val bundleContent = """
key=Value.
keyWithArgument=Value with {0} argument.
"""
  val bundleFile = FileHandle(File.createTempFile("nls", ".properties"))
  var bundle: I18NBundle = mock()

  beforeTest {
    bundleFile.writeString(bundleContent, false)
    bundle = I18NBundle.createBundle(bundleFile.sibling(bundleFile.nameWithoutExtension()))
    BundleEnum.i18nBundle = bundle
  }

  test("should access bundle lines with brace operator") {
    bundle["key"] shouldBe "Value."
  }

  test("should format bundle lines with brace operator and arguments") {
    bundle["keyWithArgument", 1] shouldBe "Value with 1 argument."
  }

  test("should access values of BundleLine instances with brace operator") {
    bundle[key] shouldBe "Value."
  }

  test("should format values of BundleLine instances with brace operator and arguments") {
    bundle[keyWithArgument, 1] shouldBe "Value with 1 argument."
  }

  test("should find BundleLine value with nls property") {
    key.nls shouldBe "Value."
  }

  test("should format BundleLine value using nls method with arguments") {
    keyWithArgument.nls(1) shouldBe "Value with 1 argument."
  }

  test("should find BundleLine value with invocation syntax") {
    key() shouldBe "Value."
  }

  test("should format BundleLine value with invocation syntax and arguments") {
    keyWithArgument(1) shouldBe "Value with 1 argument."
  }

  test("should prohibit bundle property access when not overridden") {
    shouldThrow<NotImplementedError> {
      val bundleLine = object : BundleLine {}

      bundleLine.bundle
    }
  }

  afterTest {
    bundleFile.delete()
  }
})

/** For [BundleLine] tests. */
@Suppress("ktlint:standard:enum-entry-name-case")
internal enum class BundleEnum : BundleLine {
  /** "Value." */
  key,

  /** "Value with {0} argument." */
  keyWithArgument,

  ;

  override val bundle: I18NBundle
    get() = i18nBundle!!

  companion object {
    var i18nBundle: I18NBundle? = null
  }
}
