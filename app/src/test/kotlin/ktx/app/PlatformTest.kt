package ktx.app

import com.badlogic.gdx.Application
import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.GdxRuntimeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Tests [Platform] utilities.
 */
@Suppress("DEPRECATION") // For Applet tests.
class PlatformTest : FunSpec({
  fun fakeApp(
    platform: ApplicationType? = null,
    version: Int = 0,
  ): Application =
    mock {
      on { it.type } doReturn platform
      on { it.version } doReturn version
    }

  afterTest {
    Gdx.app = null
  }

  test("should return current platform if application is defined") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val platform = Platform.currentPlatform

    // Then:
    platform shouldBeSameInstanceAs ApplicationType.Desktop
  }

  test("should throw exception if application is undefined") {
    // Given:
    Gdx.app = null

    // Expect:
    shouldThrow<GdxRuntimeException> {
      Platform.currentPlatform
    }
  }

  test("should throw exception if application type is undefined") {
    // Given:
    Gdx.app = fakeApp(platform = null)

    // Expect:
    shouldThrow<GdxRuntimeException> {
      Platform.currentPlatform
    }
  }

  test("should check if the current platform is Applet") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Applet)

    // When:
    val isApplet = Platform.isApplet

    // Then:
    isApplet shouldBe true
  }

  test("should check if the current platform is not Applet") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.WebGL)

    // When:
    val isApplet = Platform.isApplet

    // Then:
    isApplet shouldBe false
  }

  test("should execute action if the current platform is Applet") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Applet)

    // When:
    val executed = Platform.runOnApplet { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute action if the current platform is not Applet") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.WebGL)

    // When:
    val executed = Platform.runOnApplet { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should check if the current platform is Android") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android)

    // When:
    val isAndroid = Platform.isAndroid

    // Then:
    isAndroid shouldBe true
  }

  test("should check if the current platform is not Android") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.iOS)

    // When:
    val isAndroid = Platform.isAndroid

    // Then:
    isAndroid shouldBe false
  }

  test("should execute action if the current platform is Android") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android)

    // When:
    val executed = Platform.runOnAndroid { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute action if the current platform is not Android") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.iOS)

    // When:
    val executed = Platform.runOnAndroid { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should check if the current platform is Desktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val isDesktop = Platform.isDesktop

    // Then:
    isDesktop shouldBe true
  }

  test("should check if the current platform is not Desktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.HeadlessDesktop)

    // When:
    val isDesktop = Platform.isDesktop

    // Then:
    isDesktop shouldBe false
  }

  test("should execute action if the current platform is Desktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val executed = Platform.runOnDesktop { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute action if the current platform is not Desktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.HeadlessDesktop)

    // When:
    val executed = Platform.runOnDesktop { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should check if the current platform is HeadlessDesktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.HeadlessDesktop)

    // When:
    val isHeadless = Platform.isHeadless

    // Then:
    isHeadless shouldBe true
  }

  test("should check if the current platform is not HeadlessDesktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val isHeadless = Platform.isHeadless

    // Then:
    isHeadless shouldBe false
  }

  test("should execute action if the current platform is HeadlessDesktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.HeadlessDesktop)

    // When:
    val executed = Platform.runOnHeadless { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute action if the current platform is not HeadlessDesktop") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val executed = Platform.runOnHeadless { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should check if the current platform is iOS") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.iOS)

    // When:
    val isiOS = Platform.isiOS

    // Then:
    isiOS shouldBe true
  }

  test("should check if the current platform is not iOS") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android)

    // When:
    val isiOS = Platform.isiOS

    // Then:
    isiOS shouldBe false
  }

  test("should execute action if the current platform is iOS") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.iOS)

    // When:
    val executed = Platform.runOniOS { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute action if the current platform is not iOS") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android)

    // When:
    val executed = Platform.runOniOS { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should check if the current platform is mobile on Android") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android)

    // When:
    val isMobile = Platform.isMobile

    // Then:
    isMobile shouldBe true
  }

  test("should check if the current platform is mobile on iOS") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.iOS)

    // When:
    val isMobile = Platform.isMobile

    // Then:
    isMobile shouldBe true
  }

  test("should check if the current platform is not mobile") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val isMobile = Platform.isMobile

    // Then:
    isMobile shouldBe false
  }

  test("should execute action if the current platform is mobile on iOS") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.iOS)

    // When:
    val executed = Platform.runOnMobile { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should execute action if the current platform is mobile on Android") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android)

    // When:
    val executed = Platform.runOnMobile { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute action if the current platform is not mobile") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val executed = Platform.runOnMobile { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should check if the current platform is WebGL") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.WebGL)

    // When:
    val isWebGL = Platform.isWeb

    // Then:
    isWebGL shouldBe true
  }

  test("should check if the current platform is not WebGL") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Applet)

    // When:
    val isWebGL = Platform.isWeb

    // Then:
    isWebGL shouldBe false
  }

  test("should execute action if the current platform is WebGL") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.WebGL)

    // When:
    val executed = Platform.runOnWeb { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute action if the current platform is not WebGL") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Applet)

    // When:
    val executed = Platform.runOnWeb { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should return platform version") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android, version = 20)

    // When:
    val version = Platform.version

    // Then:
    version shouldBe 20
  }

  test("should return default version on platforms without API version") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val version = Platform.version

    // Then:
    version shouldBe 0
  }

  test("should return -1 if application is not defined") {
    // Given:
    Gdx.app = null

    // When:
    val version = Platform.version

    // Then:
    version shouldBe -1
  }

  test("should execute if version is above the minimum given value") {
    // Given:
    Gdx.app = fakeApp(version = 10)

    // When:
    val executed = Platform.runOnVersion(minVersion = 5) { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should execute if version is equal to the minimum given value") {
    // Given:
    Gdx.app = fakeApp(version = 10)

    // When:
    val executed = Platform.runOnVersion(minVersion = 10) { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute if version is below the minimum given value") {
    // Given:
    Gdx.app = fakeApp(version = 10)

    // When:
    val executed = Platform.runOnVersion(minVersion = 15) { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should execute if version is below the maximum given value") {
    // Given:
    Gdx.app = fakeApp(version = 10)

    // When:
    val executed = Platform.runOnVersion(maxVersion = 15) { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should execute if version is equal to the maximum given value") {
    // Given:
    Gdx.app = fakeApp(version = 10)

    // When:
    val executed = Platform.runOnVersion(maxVersion = 10) { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute if version is above the maximum given value") {
    // Given:
    Gdx.app = fakeApp(version = 10)

    // When:
    val executed = Platform.runOnVersion(maxVersion = 5) { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should execute if platform is not specified") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val executed = Platform.runOnVersion(platform = null) { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should execute if platform is equal to the given value") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val executed = Platform.runOnVersion(platform = ApplicationType.Desktop) { true } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute if platform is not equal to the given value") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Desktop)

    // When:
    val executed = Platform.runOnVersion(platform = ApplicationType.Android) { true } ?: false

    // Then:
    executed shouldBe false
  }

  test("should execute if version requirements are met") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android, version = 25)

    // When:
    val executed =
      Platform.runOnVersion(minVersion = 20, maxVersion = 30, platform = ApplicationType.Android) {
        true
      } ?: false

    // Then:
    executed shouldBe true
  }

  test("should not execute if version requirements are not met") {
    // Given:
    Gdx.app = fakeApp(platform = ApplicationType.Android, version = 25)

    // When:
    val executed =
      Platform.runOnVersion(minVersion = 8, maxVersion = 12, platform = ApplicationType.iOS) {
        true
      } ?: false

    // Then:
    executed shouldBe false
  }
})
