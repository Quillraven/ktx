package ktx.log

import com.badlogic.gdx.Application
import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Application.ApplicationType.HeadlessDesktop
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Application.LOG_NONE
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.ApplicationLogger
import com.badlogic.gdx.Audio
import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.Input
import com.badlogic.gdx.LifecycleListener
import com.badlogic.gdx.Net
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Clipboard
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Tests libGDX logging wrappers.
 */
class LogTest : FunSpec({
  val application = MockApplication()

  beforeTest {
    application.logLevel = LOG_DEBUG
    application.logs.clear()
  }

  // MockApplication logs messages to MockApplication.logs list. When given an exception, it logs user given message as
  // first log and exception message as second log. That's why tests check application.logs list.

  test("should log debug logs") {
    debug { "Test." }

    application.logs[0] shouldBe "[DEBUG] Test."
  }

  test("should log debug logs with custom tag") {
    debug("TRACE") { "Test." }

    application.logs[0] shouldBe "[TRACE] Test."
  }

  test("should log debug logs with exception") {
    debug(RuntimeException("Error.")) { "Test." }

    application.logs[0] shouldBe "[DEBUG] Test."
    application.logs[1] shouldBe "Error."
  }

  test("should log debug logs with custom tag and exception") {
    debug(RuntimeException("Error."), "TRACE") { "Test." }

    application.logs[0] shouldBe "[TRACE] Test."
    application.logs[1] shouldBe "Error."
  }

  test("should ignore debug logs if level is off") {
    application.logLevel = LOG_NONE

    debug {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should ignore debug logs with exceptions if level is off") {
    application.logLevel = LOG_NONE

    debug(RuntimeException("Error.")) {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should log info logs") {
    info { "Test." }

    application.logs[0] shouldBe "[INFO] Test."
  }

  test("should log info logs with custom tag") {
    info("WARN") { "Test." }

    application.logs[0] shouldBe "[WARN] Test."
  }

  test("should log info logs with exception") {
    info(RuntimeException("Error.")) { "Test." }

    application.logs[0] shouldBe "[INFO] Test."
    application.logs[1] shouldBe "Error."
  }

  test("should log info logs with custom tag and exception") {
    info(RuntimeException("Error."), "WARN") { "Test." }

    application.logs[0] shouldBe "[WARN] Test."
    application.logs[1] shouldBe "Error."
  }

  test("should ignore info logs if level is off") {
    application.logLevel = LOG_NONE

    info {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should ignore info logs with exceptions level is off") {
    application.logLevel = LOG_NONE

    info(RuntimeException("Error.")) {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should log error logs") {
    error { "Test." }

    application.logs[0] shouldBe "[ERROR] Test."
  }

  test("should log error logs with custom tag") {
    ktx.log.error("FATAL") { "Test." }

    application.logs[0] shouldBe "[FATAL] Test."
  }

  test("should log error logs with exception") {
    ktx.log.error(RuntimeException("Error.")) { "Test." }

    application.logs[0] shouldBe "[ERROR] Test."
    application.logs[1] shouldBe "Error."
  }

  test("should log error logs with custom tag and exception") {
    ktx.log.error(RuntimeException("Error."), "FATAL") { "Test." }

    application.logs[0] shouldBe "[FATAL] Test."
    application.logs[1] shouldBe "Error."
  }

  test("should ignore error logs if level is off") {
    application.logLevel = LOG_NONE

    error {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should ignore error logs with exceptions if level is off") {
    application.logLevel = LOG_NONE

    ktx.log.error(RuntimeException("Error.")) {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should create logger for class") {
    val logger = logger<MockClass>()

    logger.name shouldBe MockClass::class.java.name
    logger.debugTag shouldBe "DEBUG"
    logger.infoTag shouldBe "INFO"
    logger.errorTag shouldBe "ERROR"
  }

  test("should log debug logs via logger") {
    val logger = logger<MockClass>()

    logger.debug { "Test." }

    application.logs[0] shouldBe "[DEBUG] ${MockClass::class.java.name} - Test."
  }

  test("should log debug logs with exception via logger") {
    val logger = logger<MockClass>()

    logger.debug(RuntimeException("Error.")) { "Test." }

    application.logs[0] shouldBe "[DEBUG] ${MockClass::class.java.name} - Test."
    application.logs[1] shouldBe "Error."
  }

  test("should ignore debug logs via logger if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger.debug {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should ignore debug logs with exceptions via logger if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger.debug(RuntimeException("Error.")) {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should log info logs via logger") {
    val logger = logger<MockClass>()

    logger.info { "Test." }

    application.logs[0] shouldBe "[INFO] ${MockClass::class.java.name} - Test."
  }

  test("should log info logs with exception via logger") {
    val logger = logger<MockClass>()

    logger.info(RuntimeException("Error.")) { "Test." }

    application.logs[0] shouldBe "[INFO] ${MockClass::class.java.name} - Test."
    application.logs[1] shouldBe "Error."
  }

  test("should ignore info logs via logger if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger.info {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should ignore info logs with exceptions via logger if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger.info(RuntimeException("Error.")) {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should log error logs via logger") {
    val logger = logger<MockClass>()

    logger.error { "Test." }

    application.logs[0] shouldBe "[ERROR] ${MockClass::class.java.name} - Test."
  }

  test("should log error logs with exception via logger") {
    val logger = logger<MockClass>()

    logger.error(RuntimeException("Error.")) { "Test." }

    application.logs[0] shouldBe "[ERROR] ${MockClass::class.java.name} - Test."
    application.logs[1] shouldBe "Error."
  }

  test("should ignore error logs via logger if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger.error {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should ignore error logs with  exceptions via logger if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger.error(RuntimeException("Error.")) {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should log info logs via logger operator") {
    val logger = logger<MockClass>()

    logger { "Test." }

    application.logs[0] shouldBe "[INFO] ${MockClass::class.java.name} - Test."
  }

  test("should log info logs with exception via logger operator") {
    val logger = logger<MockClass>()

    logger(RuntimeException("Error.")) { "Test." }

    application.logs[0] shouldBe "[INFO] ${MockClass::class.java.name} - Test."
    application.logs[1] shouldBe "Error."
  }

  test("should ignore Info logs via logger operator if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should ignore info logs with exceptions via logger operator if level is off") {
    val logger = logger<MockClass>()
    application.logLevel = LOG_NONE

    logger(RuntimeException("Error.")) {
      fail("Should not build message.")
      "Test."
    }

    application.logs.isEmpty() shouldBe true
  }

  test("should create custom logger with modified message format") {
    class CustomLogger(
      name: String,
    ) : Logger(name, debugTag = "D", infoTag = "I", errorTag = "E") {
      override fun buildMessage(message: String): String = "$name ||| $message"
    }

    val logger = CustomLogger("LoggerName")

    logger.debug { "Test debug log." }
    logger.info { "Test info log." }
    logger.error { "Test error log." }

    application.logs[0] shouldBe "[D] LoggerName ||| Test debug log."
    application.logs[1] shouldBe "[I] LoggerName ||| Test info log."
    application.logs[2] shouldBe "[E] LoggerName ||| Test error log."
  }
})

/**
 * Used by the [Logger] tests.
 */
class MockClass

/**
 * Mocks a desktop application. Stores logs in [logs] list.
 */
class MockApplication : Application {
  val logs = mutableListOf<String>()
  private var level: Int = LOG_DEBUG

  init {
    Gdx.app = this
  }

  override fun getLogLevel(): Int = level

  override fun setLogLevel(logLevel: Int) {
    level = logLevel
  }

  override fun debug(
    tag: String?,
    message: String?,
  ) {
    logs.add("[$tag] $message")
  }

  override fun debug(
    tag: String?,
    message: String?,
    exception: Throwable,
  ) {
    logs.add("[$tag] $message")
    logs.add(exception.message!!)
  }

  override fun log(
    tag: String?,
    message: String?,
  ) {
    logs.add("[$tag] $message")
  }

  override fun log(
    tag: String?,
    message: String?,
    exception: Throwable,
  ) {
    logs.add("[$tag] $message")
    logs.add(exception.message!!)
  }

  override fun error(
    tag: String?,
    message: String?,
  ) {
    logs.add("[$tag] $message")
  }

  override fun error(
    tag: String?,
    message: String?,
    exception: Throwable,
  ) {
    logs.add("[$tag] $message")
    logs.add(exception.message!!)
  }

  override fun getClipboard(): Clipboard? = null

  override fun getFiles(): Files? = null

  override fun getApplicationListener(): ApplicationListener? = null

  override fun removeLifecycleListener(listener: LifecycleListener?) = Unit

  override fun getPreferences(name: String?): Preferences? = null

  override fun addLifecycleListener(listener: LifecycleListener?) = Unit

  override fun getVersion(): Int = 0

  override fun postRunnable(runnable: Runnable?) = Unit

  override fun getGraphics(): Graphics? = null

  override fun getAudio(): Audio? = null

  override fun exit() = Unit

  override fun getType(): ApplicationType = HeadlessDesktop

  override fun getInput(): Input? = null

  override fun getNativeHeap(): Long = 0L

  override fun getNet(): Net? = null

  override fun getJavaHeap(): Long = 0L

  override fun setApplicationLogger(applicationLogger: ApplicationLogger?) = Unit

  override fun getApplicationLogger(): ApplicationLogger? = null
}
