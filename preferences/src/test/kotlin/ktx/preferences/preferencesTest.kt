package ktx.preferences

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.Json
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/**
 * Mock-up [Preferences] implementation wrapping a [MutableMap].
 */
class TestPreferences : Preferences {
  private val map = mutableMapOf<String, Any>()
  var flushed = false

  override fun contains(key: String) = map.containsKey(key)

  override fun get(): MutableMap<String, *> = map

  override fun getBoolean(key: String) = map[key] as Boolean

  override fun getBoolean(
    key: String,
    defValue: Boolean,
  ) = map.getOrDefault(key, defValue) as Boolean

  override fun getInteger(key: String) = map[key] as Int

  override fun getInteger(
    key: String,
    defValue: Int,
  ) = map.getOrDefault(key, defValue) as Int

  override fun getLong(key: String) = map[key] as Long

  override fun getLong(
    key: String,
    defValue: Long,
  ) = map.getOrDefault(key, defValue) as Long

  override fun getFloat(key: String) = map[key] as Float

  override fun getFloat(
    key: String,
    defValue: Float,
  ) = map.getOrDefault(key, defValue) as Float

  override fun getString(key: String) = map[key] as String

  override fun getString(
    key: String,
    defValue: String,
  ) = map.getOrDefault(key, defValue) as String

  override fun put(vals: MutableMap<String, *>): Preferences {
    vals.forEach { map[it.key] = it.value as Any }
    return this
  }

  override fun putLong(
    key: String,
    value: Long,
  ): Preferences {
    map[key] = value
    return this
  }

  override fun putInteger(
    key: String,
    value: Int,
  ): Preferences {
    map[key] = value
    return this
  }

  override fun putBoolean(
    key: String,
    value: Boolean,
  ): Preferences {
    map[key] = value
    return this
  }

  override fun putFloat(
    key: String,
    value: Float,
  ): Preferences {
    map[key] = value
    return this
  }

  override fun putString(
    key: String,
    value: String,
  ): Preferences {
    map[key] = value
    return this
  }

  override fun remove(key: String) {
    map.remove(key)
  }

  override fun clear() = map.clear()

  override fun flush() {
    flushed = true
  }
}

/**
 * For JSON serialization tests.
 */
data class JsonSerializable(
  val data: String = "",
)

class PreferencesTest : FunSpec({
  val preferences: Preferences = TestPreferences()

  test("should set string value") {
    preferences["Key"] = "Value"

    ("Key" in preferences) shouldBe true
    preferences.getString("Key") shouldBe "Value"
  }

  test("should set boolean value") {
    preferences["Key"] = true

    ("Key" in preferences) shouldBe true
    preferences.getBoolean("Key") shouldBe true
  }

  test("should set int value") {
    preferences["Key"] = 1

    ("Key" in preferences) shouldBe true
    preferences.getInteger("Key") shouldBe 1
  }

  test("should set float value") {
    preferences["Key"] = 1f

    ("Key" in preferences) shouldBe true
    preferences.getFloat("Key") shouldBe 1f
  }

  test("should set double value and cast to float") {
    preferences["Key"] = 2.0

    ("Key" in preferences) shouldBe true
    preferences.getFloat("Key") shouldBe 2.0f
  }

  test("should throw an exception when setting double value outside of float positive range") {
    shouldThrow<GdxRuntimeException> {
      preferences["Key"] = Double.MAX_VALUE
    }
  }

  test("should throw an exception when setting double value outside of float negative range") {
    shouldThrow<GdxRuntimeException> {
      preferences["Key"] = Double.MIN_VALUE
    }
  }

  test("should set long value") {
    preferences["Key"] = 1L

    ("Key" in preferences) shouldBe true
    preferences.getLong("Key") shouldBe 1L
  }

  test("should set values as pairs") {
    preferences.set("Key1" to "Value")
    preferences.set("Key2" to 1)
    preferences.set("Key3" to 2L)
    preferences.set("Key4" to 3.0f)
    preferences.set("Key5" to 4.0)
    preferences.set("Key6" to true)
    preferences.set("Key7" to JsonSerializable("test"))

    preferences.getString("Key1") shouldBe "Value"
    preferences.getInteger("Key2") shouldBe 1
    preferences.getLong("Key3") shouldBe 2L
    preferences.getFloat("Key4") shouldBe 3.0f
    preferences.getFloat("Key5") shouldBe 4.0f
    preferences.getBoolean("Key6") shouldBe true
    Json().fromJson(JsonSerializable::class.java, preferences.getString("Key7")) shouldBe JsonSerializable("test")
  }

  test("should throw an exception when setting double value outside of float range as pair") {
    shouldThrow<GdxRuntimeException> {
      preferences.set("Key" to Double.MAX_VALUE)
    }
  }

  test("should set any value") {
    preferences["Key"] = JsonSerializable("test")

    ("Key" in preferences) shouldBe true
    Json().fromJson(JsonSerializable::class.java, preferences.getString("Key")) shouldBe JsonSerializable("test")
  }

  test("should get string value") {
    preferences["Key"] = "Value"

    val result: String? = preferences["Key"]

    result shouldBe "Value"
  }

  test("should get boolean value") {
    preferences["Key"] = true

    val result: Boolean? = preferences["Key"]

    result shouldBe true
  }

  test("should get int value") {
    preferences["Key"] = 1

    val result: Int? = preferences["Key"]

    result shouldBe 1
  }

  test("should get float value") {
    preferences["Key"] = 1f

    val result: Float? = preferences["Key"]

    result shouldBe 1f
  }

  test("should get long value") {
    preferences["Key"] = 1L

    val result: Long? = preferences["Key"]

    result shouldBe 1L
  }

  test("should get any value") {
    preferences["Key"] = JsonSerializable("test")

    val result: JsonSerializable? = preferences["Key"]

    result shouldBe JsonSerializable("test")
  }

  test("should get missing string value") {
    val result: String? = preferences["Key"]

    result.shouldBeNull()
  }

  test("should get missing boolean value") {
    val result: Boolean? = preferences["Key"]

    result.shouldBeNull()
  }

  test("should get missing int value") {
    val result: Int? = preferences["Key"]

    result.shouldBeNull()
  }

  test("should get missing float value") {
    val result: Float? = preferences["Key"]

    result.shouldBeNull()
  }

  test("should get missing long value") {
    val result: Long? = preferences["Key"]

    result.shouldBeNull()
  }

  test("should get missing object value") {
    val result: JsonSerializable? = preferences["Key"]

    result.shouldBeNull()
  }

  test("should get string with default value") {
    preferences["Key"] = "Value"

    val result: String = preferences["Key", "default"]

    result shouldBe "Value"
  }

  test("should get boolean with default value") {
    preferences["Key"] = true

    val result: Boolean = preferences["Key", false]

    result shouldBe true
  }

  test("should get int with default value") {
    preferences["Key"] = 1

    val result: Int = preferences["Key", 0]

    result shouldBe 1
  }

  test("should get float with default value") {
    preferences["Key"] = 1f

    val result: Float = preferences["Key", 0f]

    result shouldBe 1f
  }

  test("should get long with default value") {
    preferences["Key"] = 1L

    val result: Long = preferences["Key", 0L]

    result shouldBe 1L
  }

  test("should get any object with default value") {
    preferences["Key"] = JsonSerializable("test")

    val result: JsonSerializable = preferences["Key", JsonSerializable("")]

    result shouldBe JsonSerializable("test")
  }

  test("should get missing string with default value") {
    val result: String = preferences["Key", "default"]

    result shouldBe "default"
  }

  test("should get missing boolean with default value") {
    val result: Boolean = preferences["Key", true]

    result shouldBe true
  }

  test("should get missing int with default value") {
    val result: Int = preferences["Key", 42]

    result shouldBe 42
  }

  test("should get missing float with default value") {
    val result: Float = preferences["Key", 4.2f]

    result shouldBe 4.2f
  }

  test("should get missing long with default value") {
    val result: Long = preferences["Key", 42L]

    result shouldBe 42L
  }

  test("should get missing object with default value") {
    val result: JsonSerializable = preferences["Key", JsonSerializable("Test")]

    result shouldBe JsonSerializable("Test")
  }

  test("should flush changes after executing passed operations") {
    var wasExecuted: Boolean
    var wasFlushedDuringExecution: Boolean
    preferences.flush {
      wasExecuted = true
      wasFlushedDuringExecution = (preferences as TestPreferences).flushed
    }

    (preferences as TestPreferences).flushed shouldBe true
    wasExecuted shouldBe true
    wasFlushedDuringExecution shouldBe false
  }

  test("should perform operations on flushing exactly once") {
    val variable: Int

    preferences.flush {
      variable = 42
    }

    variable shouldBe 42
  }
})
