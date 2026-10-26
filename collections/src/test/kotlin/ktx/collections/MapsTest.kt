package ktx.collections

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IdentityMap
import com.badlogic.gdx.utils.IntFloatMap
import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.LongMap
import com.badlogic.gdx.utils.ObjectIntMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.lang.IllegalArgumentException
import java.util.LinkedList

/**
 * Tests utilities for libGDX custom HashMap equivalent - [ObjectMap].
 */
class MapsTest : FunSpec({
  test("should create GdxMap") {
    val map = gdxMapOf<Any, Any>()

    map.shouldNotBeNull()
    map.size shouldBe 0
  }

  test("should create GdxMap with custom initial capacity") {
    val map = gdxMapOf<Any, Any>(initialCapacity = 32)

    map.shouldNotBeNull()
    map.size shouldBe 0
  }

  test("should create GdxMap with custom load factor") {
    val map = gdxMapOf<Any, Any>(loadFactor = 0.4f)

    map.shouldNotBeNull()
    map.size shouldBe 0
  }

  test("should create GdxMap from key to value pairs") {
    val map = gdxMapOf("key" to "value", "other" to "another")

    map.size shouldBe 2
    ("key" in map) shouldBe true
    ("other" in map) shouldBe true
    map.containsValue("value", false) shouldBe true
    map.containsValue("another", false) shouldBe true
    map["key"] shouldBe "value"
    map["other"] shouldBe "another"
  }

  test("should report size") {
    val map = gdxMapOf(1 to "1", 2 to "2", 3 to "3")

    map.size() shouldBe 3
    map.size() shouldBe map.size
  }

  test("should report size on null Map") {
    val nullMap: ObjectMap<Any, Any>? = null

    nullMap.size() shouldBe 0
  }

  test("should report empty status") {
    gdxMapOf(1 to "1", 2 to "2", 3 to "3").isEmpty() shouldBe false
    (ObjectMap<Any, Any>().isEmpty()) shouldBe true
    (null as ObjectMap<Any, Any>?).isEmpty() shouldBe true
  }

  test("should verify empty status contract") {
    fun getMap(): GdxMap<Any, Any>? = gdxMapOf(1 to "1")

    val map = getMap()
    if (!map.isEmpty()) {
      (map.size == 1) shouldBe true
    }
  }

  test("should report non empty status") {
    gdxMapOf(1 to "1", 2 to "2", 3 to "3").isNotEmpty() shouldBe true
    (ObjectMap<Any, Any>().isNotEmpty()) shouldBe false
    (null as ObjectMap<Any, Any>?).isNotEmpty() shouldBe false
  }

  test("should verify non empty status contract") {
    fun getMap(): GdxMap<Any, Any>? = gdxMapOf(1 to "1")

    val map = getMap()
    if (map.isNotEmpty()) {
      (map.size == 1) shouldBe true
    }
  }

  test("should put values in Map with square brackets") {
    val map = ObjectMap<String, String>()

    map["key"] = "value"

    ("key" in map) shouldBe true
    map.containsValue("value", false) shouldBe true
  }

  test("should allow to iterate with iterator reference") {
    val map = gdxMapOf(1 to "1", 2 to "2", 3 to "3")

    map.iterate { _, value, iterator -> if (value == "1") iterator.remove() }

    map.size shouldBe 2
    (1 in map) shouldBe false
    map.containsValue("1", false) shouldBe false
  }

  test("should check if Map contains key with in operator") {
    val map = ObjectMap<String, String>()

    map.put("key", "value")

    ("key" in map) shouldBe true
    ("absent" !in map) shouldBe true
  }

  test("should convert GdxMap keys to Set") {
    val set = gdxMapOf(1 to "1", 2 to "2", 3 to "3").toGdxSet() // .keys().toGdxSet() and .values().toGdxSet() also work.

    set.shouldNotBeNull()
    set.size shouldBe 3
    (1 in set) shouldBe true
    (2 in set) shouldBe true
    (3 in set) shouldBe true
  }

  test("should convert Set to GdxMap") {
    val setAsMap = ObjectSet.with("1", "2", "3").toGdxMap { it.toInt() }

    setAsMap shouldBe gdxMapOf(1 to "1", 2 to "2", 3 to "3")
  }

  test("should convert GdxArray to GdxMap") {
    val arrayAsMap = Array.with("1", "2", "3").toGdxMap { it.toInt() }

    arrayAsMap shouldBe gdxMapOf(1 to "1", 2 to "2", 3 to "3")
  }

  test("should convert Array to GdxMap") {
    val arrayAsMap = arrayOf("1", "2", "3").toGdxMap { it.toInt() }

    arrayAsMap shouldBe gdxMapOf(1 to "1", 2 to "2", 3 to "3")
  }

  test("should convert Iterables to GdxMap") {
    val map =
      listOf("1", "2", "3").toGdxMap(
        keyProvider = { it.toInt() },
        valueProvider = { it + it },
      )

    map shouldBe gdxMapOf(1 to "11", 2 to "22", 3 to "33")
  }

  test("should convert customized Arrays to GdxMap") {
    val map =
      arrayOf("1", "2", "3").toGdxMap(
        keyProvider = { it.toInt() },
        valueProvider = { it + it },
      )

    map shouldBe gdxMapOf(1 to "11", 2 to "22", 3 to "33")
  }

  test("should create IdentityMaps") {
    val map = gdxIdentityMapOf<Any, Any>()

    map.shouldNotBeNull()
    map.size shouldBe 0
  }

  test("should create IdentityMaps from key to value pairs") {
    val key = "key"
    val otherKey = "other"
    val map = gdxIdentityMapOf(key to "value", otherKey to "another")

    map.size shouldBe 2
    (key in map) shouldBe true
    (otherKey in map) shouldBe true
    map.containsValue("value", false) shouldBe true
    map.containsValue("another", false) shouldBe true
    map[key] shouldBe "value"
    map[otherKey] shouldBe "another"
  }

  test("should check if IdentityMap contains key with in operator") {
    val map = IdentityMap<String, String>()
    val key = "key"

    map.put(key, "value")

    (key in map) shouldBe true
    ("absent" !in map) shouldBe true
  }

  test("should put values in IdentityMap with square brackets") {
    val map = IdentityMap<String, String>()
    val key = "key"

    map[key] = "value"

    (key in map) shouldBe true
    map.containsValue("value", false) shouldBe true
  }

  test("should allow to iterate IdentityMap with iterator reference") {
    val keyToRemove = 1
    val map = gdxIdentityMapOf(keyToRemove to "1", 2 to "2", 3 to "3")

    map.iterate { _, value, iterator -> if (value == "1") iterator.remove() }

    map.size shouldBe 2
    (keyToRemove in map) shouldBe false
    map.containsValue("1", false) shouldBe false
  }

  test("should support optimized primitive IntIntMap") {
    val map = gdxIntIntMap()

    map[1] = 2 // Tests array bracket operator.

    map[1] shouldBe 2
    map[1, -1] shouldBe 2 // Second parameter is the default value returned if key is missing: -1.
    map.size shouldBe 1
    (1 in map) shouldBe true // Tests contains extension (in operator).
    (2 !in map) shouldBe true // This operator (in) tests if keys are in the map and should return false even if the parameter
    // matches a valid value stored in the map.
  }

  test("should support optimized primitive IntFloatMap") {
    val map = gdxIntFloatMap()

    map[1] = 2f // Tests array bracket operator.

    map[1] shouldBe (2f plusOrMinus 0.00001f)
    map[1, -1f] shouldBe (2f plusOrMinus 0.00001f) // Second parameter is the default value returned if key is missing: -1f.
    map.size shouldBe 1
    (1 in map) shouldBe true // Tests contains extension (in operator).
    (2 !in map) shouldBe true
  }

  test("should support optimized IntMap with primitive int keys") {
    val map = gdxIntMap<String>()

    map[1] = "2" // Tests array bracket operator.

    map[1] shouldBe "2"
    map.size shouldBe 1
    (1 in map) shouldBe true // Tests contains extension (in operator).
    (2 !in map) shouldBe true
  }

  test("should provide alias for compatibility with other libGDX collections") {
    (GdxMap<Any, Any>() is ObjectMap<Any, Any>) shouldBe true
  }

  test("should destruct ObjectMap#Entry into key and value") {
    val entry = ObjectMap.Entry<String, String>()
    entry.key = "Key"
    entry.value = "Value"

    val (key, value) = entry

    key shouldBe "Key"
    value shouldBe "Value"
  }

  test("should destruct IntMap#Entry into key and value") {
    val entry = IntMap.Entry<String>()
    entry.key = 10
    entry.value = "Value"

    val (key, value) = entry

    key shouldBe 10
    value shouldBe "Value"
  }

  test("should destruct LongMap#Entry into key and value") {
    val entry = LongMap.Entry<String>()
    entry.key = 10L
    entry.value = "Value"

    val (key, value) = entry

    key shouldBe 10L
    value shouldBe "Value"
  }

  test("should destruct IntIntMap#Entry into key and value") {
    val entry = IntIntMap.Entry()
    entry.key = 10
    entry.value = 20

    val (key, value) = entry

    key shouldBe 10
    value shouldBe 20
  }

  test("should destruct IntFloatMap#Entry into key and value") {
    val entry = IntFloatMap.Entry()
    entry.key = 10
    entry.value = 20f

    val (key, value) = entry

    key shouldBe 10
    value shouldBe 20f
  }

  test("should destruct ObjectIntMap#Entry into key and value") {
    val entry = ObjectIntMap.Entry<String>()
    entry.key = "Key"
    entry.value = 10

    val (key, value) = entry

    key shouldBe "Key"
    value shouldBe 10
  }

  test("should map elements into a new GdxMap") {
    val map = gdxMapOf("One" to 1, "Two" to 2, "Three" to 3)

    val result = map.map { it.value * 2 }

    result shouldBe gdxMapOf("One" to 2, "Two" to 4, "Three" to 6)
  }

  test("should filter elements into a new GdxMap") {
    val map = gdxMapOf("One" to 1, "Two" to 2, "Three" to 3, "Four" to 4, "Five" to 5)

    val result = map.filter { it.value % 2 == 1 }

    result shouldBe gdxMapOf("One" to 1, "Three" to 3, "Five" to 5)
  }

  test("should flatten elements into a new GdxArray") {
    val map = gdxMapOf(1 to GdxArray.with(1), 2 to listOf<Int>(), 3 to LinkedList(arrayListOf(2, 3)))

    val result = map.flatten()

    result.size shouldBe 3
    result shouldBe GdxArray.with(1, 2, 3)
  }

  test("should map elements to lists and flatten them into a new GdxArray") {
    val map = gdxMapOf("One" to 1, "Two" to 2, "Three" to 3)

    val result = map.flatMap { (_, value) -> List(value!!) { value } }
    result.sort()

    result shouldBe GdxArray.with(1, 2, 2, 3, 3, 3)
  }

  test("should add new entry to the GdxArrayMap using square brackets assignment") {
    val gdxArrayMap = GdxArrayMap<Int, String>()
    gdxArrayMap[1] = "One"
    gdxArrayMap[2] = "Two"
    gdxArrayMap.put(3, "Three")
    gdxArrayMap.put(4, "Four")
    gdxArrayMap[1] shouldBe "One"
    gdxArrayMap[2] shouldBe "Two"
    gdxArrayMap[3] shouldBe "Three"
    gdxArrayMap[4] shouldBe "Four"
  }

  test("should return existing value for GdxMap when key exists") {
    val map = gdxMapOf("42" to 42)

    val actual = map.getOrPut("42") { 43 }

    actual shouldBe 42
    map["42"] shouldBe 42
  }

  test("should return and put default value to GdxMap when key does not exist") {
    val map = gdxMapOf<String, Int>()

    val actual = map.getOrPut("42") { 43 }

    actual shouldBe 43
    ("42" in map) shouldBe true
    map["42"] shouldBe 43
  }

  test("should return null for GdxMap when null is stored for given key") {
    val map = gdxMapOf<String, Int?>("42" to null)

    val actual = map.getOrPut("42") { 43 }

    actual.shouldBeNull()
    map["42"] shouldBe null
  }

  test("should throw an IllegalArgumentException when getOrPut is called with null key for GdxMap") {
    val map = gdxMapOf<String?, String?>()

    shouldThrow<IllegalArgumentException> {
      map.getOrPut(null) { "42" }
    }
  }

  test("should return existing value for GdxIdentityMap when key exists") {
    val map = gdxIdentityMapOf("42" to 42)

    val actual = map.getOrPut("42") { 43 }

    actual shouldBe 42
    map["42"] shouldBe 42
  }

  test("should return and put default value to GdxIdentityMap when key does not exist") {
    val map = gdxIdentityMapOf<String, Int>()

    val actual = map.getOrPut("42") { 43 }

    actual shouldBe 43
    ("42" in map) shouldBe true
    map["42"] shouldBe 43
  }

  test("should return null for GdxIdentityMap when null is stored for given key") {
    val map = gdxIdentityMapOf<String, Int?>("42" to null)

    val actual = map.getOrPut("42") { 43 }

    actual.shouldBeNull()
    map["42"] shouldBe null
  }

  test("should throw an IllegalArgumentException when getOrPut is called with null key for GdxIdentityMap") {
    val map = gdxIdentityMapOf<String?, String?>()

    shouldThrow<IllegalArgumentException> {
      map.getOrPut(null) { "42" }
    }
  }

  test("should return existing value for GdxArrayMap when key exists") {
    val map = GdxArrayMap<String, Int>()
    map["42"] = 42

    val actual = map.getOrPut("42") { 43 }

    actual shouldBe 42
    map["42"] shouldBe 42
  }

  test("should return and put default value to GdxArrayMap when key does not exist") {
    val map = GdxArrayMap<String, Int>()

    val actual = map.getOrPut("42") { 43 }

    actual shouldBe 43
    map.containsKey("42") shouldBe true
    map["42"] shouldBe 43
  }

  test("should return null for GdxArrayMap when null is stored for given key") {
    val map = GdxArrayMap<String, Int?>()
    map["42"] = null

    val actual = map.getOrPut("42") { 43 }

    actual.shouldBeNull()
    map["42"] shouldBe null
  }

  test("should return and put default value to GdxArrayMap when key is null") {
    val map = GdxArrayMap<String?, String?>()

    val actual = map.getOrPut(null) { "42" }

    actual shouldBe "42"
    map[null] shouldBe "42"
  }

  test("should return existing value for IntMap when key exists") {
    val map = IntMap<String>()
    map[42] = "42"

    val actual = map.getOrPut(42) { "43" }

    actual shouldBe "42"
    map[42] shouldBe "42"
  }

  test("should return and put default value to IntMap when key does not exist") {
    val map = IntMap<String>()

    val actual = map.getOrPut(42) { "43" }

    actual shouldBe "43"
    (42 in map) shouldBe true
    map[42] shouldBe "43"
  }

  test("should return null for IntMap when null is stored for given key") {
    val map = IntMap<String>()
    map.put(42, null)

    val actual = map.getOrPut(42) { "43" }

    actual.shouldBeNull()
    map[42] shouldBe null
  }
})
