package ktx.collections

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.LinkedList

/**
 * Tests utilities for libGDX custom ArrayList equivalent - Array.
 */
class ArraysTest : FunSpec({
  test("should create Array") {
    val array = gdxArrayOf<Any>()

    array.shouldNotBeNull()
    array.size shouldBe 0
  }

  test("should create Array with custom initial capacity") {
    gdxArrayOf<Any>(initialCapacity = 32).items.size shouldBe 32
    gdxArrayOf<Any>(initialCapacity = 128).items.size shouldBe 128
  }

  test("should create Arrays with custom ordered setting") {
    gdxArrayOf<Any>(ordered = false).ordered shouldBe false
    gdxArrayOf<Any>(ordered = true).ordered shouldBe true
  }

  test("should create Array with custom elements") {
    val array = gdxArrayOf("1", "2", "3")

    array.size shouldBe 3
    ("1" in array) shouldBe true
    ("2" in array) shouldBe true
    ("3" in array) shouldBe true
  }

  test("should report size of Array") {
    val array = GdxArray.with("1", "2", "3")

    array.size() shouldBe 3
    array.size() shouldBe array.size
  }

  test("should return 0 as null Array size") {
    val nullArray: GdxArray<Any>? = null

    nullArray.size() shouldBe 0
  }

  test("should report size of IntArray") {
    val array = GdxIntArray.with(1, 2, 3)

    array.size() shouldBe 3
    array.size() shouldBe array.size
  }

  test("should return 0 as null IntArray size") {
    val nullArray: GdxIntArray? = null

    nullArray.size() shouldBe 0
  }

  test("should report size of FloatArray") {
    val array = GdxFloatArray.with(1f, 2f, 3f)

    array.size() shouldBe 3
    array.size() shouldBe array.size
  }

  test("should return 0 as null FloatArray size") {
    val nullArray: GdxFloatArray? = null

    nullArray.size() shouldBe 0
  }

  test("should report size of BooleanArray") {
    val array = GdxBooleanArray.with(true, false, true)

    array.size() shouldBe 3
    array.size() shouldBe array.size
  }

  test("should return 0 as null BooleanArray size") {
    val nullArray: GdxBooleanArray? = null

    nullArray.size() shouldBe 0
  }

  test("should report empty status") {
    GdxArray.with("1", "2", "3").isEmpty() shouldBe false
    GdxArray<Any>().isEmpty() shouldBe true
    (null as GdxArray<Any>?).isEmpty() shouldBe true
  }

  test("should verify empty status contract") {
    fun getArray(): GdxArray<Any>? = GdxArray.with("1")

    val array = getArray()
    if (!array.isEmpty()) {
      (array.size == 1) shouldBe true
    }
  }

  test("should report non empty status") {
    GdxArray.with("1", "2", "3").isNotEmpty() shouldBe true
    GdxArray<Any>().isNotEmpty() shouldBe false
    (null as GdxArray<Any>?).isNotEmpty() shouldBe false
  }

  test("should verify non empty status contract") {
    fun getArray(): GdxArray<Any>? = GdxArray.with("1")

    val array = getArray()
    if (array.isNotEmpty()) {
      (array.size == 1) shouldBe true
    }
  }

  test("should return last valid index of Array") {
    val array = GdxArray.with("1", "2", "3")

    array.lastIndex shouldBe 2
  }

  test("should return last valid index of empty Array") {
    val emptyArray = GdxArray<Any>()

    emptyArray.lastIndex shouldBe -1
  }

  test("should return negative last index for null Array") {
    val nullArray: GdxArray<Any>? = null

    nullArray.lastIndex shouldBe -1
  }

  test("should return last valid index of IntArray") {
    val array = GdxIntArray.with(1, 2, 3)

    array.lastIndex shouldBe 2
  }

  test("should return last valid index of empty IntArray") {
    val emptyArray = GdxIntArray()

    emptyArray.lastIndex shouldBe -1
  }

  test("should return negative last index for null IntArray") {
    val nullArray: GdxIntArray? = null

    nullArray.lastIndex shouldBe -1
  }

  test("should return last valid index of FloatArray") {
    val array = GdxFloatArray.with(1f, 2f, 3f)

    array.lastIndex shouldBe 2
  }

  test("should return last valid index of empty FloatArray") {
    val emptyArray = GdxFloatArray()

    emptyArray.lastIndex shouldBe -1
  }

  test("should return negative last index for null FloatArray") {
    val nullArray: GdxFloatArray? = null

    nullArray.lastIndex shouldBe -1
  }

  test("should return last valid index of BooleanArray") {
    val array = GdxBooleanArray.with(true, false, true)

    array.lastIndex shouldBe 2
  }

  test("should return last valid index of empty BooleanArray") {
    val emptyArray = GdxBooleanArray()

    emptyArray.lastIndex shouldBe -1
  }

  test("should return negative last index for null BooleanArray") {
    val nullArray: GdxBooleanArray? = null

    nullArray.lastIndex shouldBe -1
  }

  test("should return alternative if element is null") {
    val array = GdxArray.with("0", null, "2")

    array.get(0, "3") shouldBe "0"
    array[1, "3"] shouldBe "3" // This method is also available through square bracket operator.
    array.get(2, "3") shouldBe "2"
    array[3, "3"] shouldBe "3"
  }

  test("should add all values from Iterable") {
    val array = GdxArray<String>()

    array.addAll(listOf("1", "2", "3"))

    array.size shouldBe 3
    ("1" in array) shouldBe true
    ("2" in array) shouldBe true
    ("3" in array) shouldBe true
  }

  test("should remove all values from Iterable") {
    val array = GdxArray.with("1", "2", "3")

    array.removeAll(listOf("1", "2", "3"))

    array.size shouldBe 0
    ("1" in array) shouldBe false
    ("2" in array) shouldBe false
    ("3" in array) shouldBe false
  }

  test("should remove all values from native Array") {
    val array = GdxArray.with("1", "2", "3")

    array.removeAll(arrayOf("1", "2", "3"))

    array.size shouldBe 0
    ("1" in array) shouldBe false
    ("2" in array) shouldBe false
    ("3" in array) shouldBe false
  }

  test("should add elements with += operator") {
    val array = GdxArray.with("1")

    array += "2"

    array shouldBe gdxArrayOf("1", "2")
  }

  test("should add Iterable with += operator") {
    val array = GdxArray.with("1")

    array += listOf("2", "3") as Iterable<String>

    array shouldBe gdxArrayOf("1", "2", "3")
  }

  test("should add Collection with += operator") {
    val array = GdxArray.with("1")

    array += listOf("2", "3")

    array shouldBe gdxArrayOf("1", "2", "3")
  }

  test("should add GdxArray with += operator") {
    val array = GdxArray.with("1")

    array += GdxArray.with("2", "3")

    array shouldBe gdxArrayOf("1", "2", "3")
  }

  test("should add arrays and elements with + operator") {
    val array = GdxArray<String>()

    val result = array + "1"

    result shouldBe gdxArrayOf("1")
    array shouldBe gdxArrayOf<String>()

    val chained = result + "2" + "3"

    chained shouldBe gdxArrayOf("1", "2", "3")
    result shouldBe gdxArrayOf("1")
  }

  test("should add arrays and Iterables with + operator") {
    val array = GdxArray.with("0")

    val result = array + listOf("1", "2", "3")

    result shouldBe gdxArrayOf("0", "1", "2", "3")
    array shouldBe gdxArrayOf("0")
  }

  test("should add arrays and native Arrays with + operator") {
    val array = GdxArray.with("0")

    val result = array + arrayOf("1", "2", "3")

    result shouldBe gdxArrayOf("0", "1", "2", "3")
    array shouldBe gdxArrayOf("0")
  }

  test("should remove values with - operator") {
    var array = GdxArray.with("1", "2", "3", "4", "5", "6")

    var result = array - "1"

    result shouldBe gdxArrayOf("2", "3", "4", "5", "6")
    array shouldBe gdxArrayOf("1", "2", "3", "4", "5", "6")

    array = result
    result = array - listOf("2", "3")

    result shouldBe gdxArrayOf("4", "5", "6")
    array shouldBe gdxArrayOf("2", "3", "4", "5", "6")

    array = result
    result = array - arrayOf("4", "5")

    result shouldBe gdxArrayOf("6")
    array shouldBe gdxArrayOf("4", "5", "6")
  }

  test("should remove values with -= operator") {
    val array = GdxArray.with("1", "2", "3", "4", "5", "6")

    array -= "1"

    array shouldBe gdxArrayOf("2", "3", "4", "5", "6")

    array -= listOf("2", "3")

    array shouldBe gdxArrayOf("4", "5", "6")

    array -= arrayOf("4", "5")

    array shouldBe gdxArrayOf("6")
  }

  test("should chain operators") {
    val array = GdxArray.with("1", "2", "3", "4")

    val result = array + "5" - "2" + GdxArray.with("7") - GdxArray.with("4", "6")

    result shouldBe GdxArray.with("1", "3", "5", "7")
  }

  test("should find elements with in operator") {
    val array = GdxArray.with("1")
    val identityCheck = false // Will compare with equals(Object).

    array.contains("1", identityCheck) shouldBe true // Standard libGDX API.
    ("1" in array) shouldBe true
    array.contains("1") shouldBe true // Operator method alias.

    array.removeValue("1", identityCheck)

    array.contains("1", identityCheck) shouldBe false // Standard libGDX API.
    ("1" in array) shouldBe false
    array.contains("1") shouldBe false
  }

  test("should allow to iterate Array with iterator reference") {
    val array = GdxArray.with("1", "2", "3")

    array.iterate { value, iterator -> if (value == "2") iterator.remove() }

    array.size shouldBe 2
    ("2" in array) shouldBe false
  }

  test("should sort elements in descending natural order") {
    val array = GdxArray.with(1, 2, 3)

    array.sortDescending()

    array shouldBe GdxArray.with(3, 2, 1)
  }

  test("should sort elements by property") {
    val array = GdxArray.with("Twenty-one", "Eleven", "One")

    array.sortBy { it.length }

    array shouldBe GdxArray.with("One", "Eleven", "Twenty-one")
  }

  test("should sort elements by property in descending order") {
    val array = GdxArray.with("One", "Eleven", "Twenty-one")

    array.sortByDescending { it.length }

    array shouldBe GdxArray.with("Twenty-one", "Eleven", "One")
  }

  test("should remove elements from existing GdxArray") {
    val array = GdxArray.with(1, 2, 3, 4, 5)
    val noneRemovedResult = array.removeAll { it > 10 }
    assert(!noneRemovedResult)
    array shouldBe GdxArray.with(1, 2, 3, 4, 5)

    val evensRemovedResult = array.removeAll { it % 2 == 0 }
    assert(evensRemovedResult)
    array shouldBe GdxArray.with(1, 3, 5)

    val allRemovedResult = array.removeAll { it is Number }
    assert(allRemovedResult)
    array shouldBe GdxArray<Int>()

    val emptyRemoveResult = array.removeAll { it > 0 }
    assert(!emptyRemoveResult)
    array shouldBe GdxArray<Int>()
  }

  test("should free removed elements") {
    val array = GdxArray.with(Vector2(), Vector2(1f, 1f), Vector2(2f, 2f))
    val pool =
      object : Pool<Vector2>() {
        override fun newObject() = Vector2()
      }
    array.removeAll(pool) { it.len() > 0.5f }
    2 shouldBe pool.peak
  }

  test("should transfer elements matching predicate") {
    val array = GdxArray.with(0, 1, 2, 3, 4)
    val target = GdxArray.with<Int>()

    array.transfer(toArray = target) {
      it % 2 == 0
    }

    array shouldBe GdxArray.with(1, 3)
    target shouldBe GdxArray.with(0, 2, 4)
  }

  test("should transfer all elements") {
    val array = GdxArray.with(0, 1, 2, 3, 4)
    val target = GdxArray.with<Int>()

    array.transfer(toArray = target) {
      true
    }

    array shouldBe GdxArray.with<Int>()
    target shouldBe GdxArray.with(0, 1, 2, 3, 4)
  }

  test("should transfer no elements") {
    val array = GdxArray.with(0, 1, 2, 3, 4)
    val target = GdxArray.with<Int>()

    array.transfer(toArray = target) {
      false
    }

    array shouldBe GdxArray.with(0, 1, 2, 3, 4)
    target shouldBe GdxArray.with<Int>()
  }

  test("should not transfer any elements from empty array") {
    val array = GdxArray.with<Int>()
    val target = GdxArray.with<Int>()

    array.transfer(toArray = target) {
      it % 2 == 0
    }

    array shouldBe GdxArray.with<Int>()
    target shouldBe GdxArray.with<Int>()
  }

  test("should transfer to wider typed array") {
    val array = GdxArray.with("ABC", "AB", "ABC")
    val target = GdxArray<CharSequence>()

    array.transfer(target) {
      it.length < 3
    }

    array shouldBe GdxArray.with("ABC", "ABC")
    target shouldBe GdxArray.with("AB" as CharSequence)
  }

  test("should retain elements from existing GdxArray") {
    val array = GdxArray.with(1, 2, 3, 4, 5)
    val allRetainedResult = array.retainAll { it < 6 }
    assert(!allRetainedResult)
    array shouldBe GdxArray.with(1, 2, 3, 4, 5)

    val oddsRetainedResult = array.retainAll { it % 2 == 1 }
    assert(oddsRetainedResult)
    array shouldBe GdxArray.with(1, 3, 5)

    val noneRetainedResult = array.retainAll { it < 0 }
    assert(noneRetainedResult)
    array shouldBe GdxArray<Int>()

    val emptyRetainResult = array.retainAll { it > 0 }
    assert(!emptyRetainResult)
    array shouldBe GdxArray<Int>()
  }

  test("should free unretained elements") {
    val array = GdxArray.with(Vector2(), Vector2(1f, 1f), Vector2(2f, 2f))
    val pool =
      object : Pool<Vector2>() {
        override fun newObject() = Vector2()
      }
    array.retainAll(pool) { it.len() < 0.5f }
    2 shouldBe pool.peak
  }

  test("should map elements into a new GdxArray") {
    val array = GdxArray.with(1, 2, 3)

    val result = array.map { it * 2 }

    result shouldBe GdxArray.with(2, 4, 6)
  }

  test("should filter elements into a new GdxArray") {
    val array = GdxArray.with(1, 2, 3, 4, 5)

    val result = array.filter { it % 2 == 1 }

    result shouldBe GdxArray.with(1, 3, 5)
  }

  test("should flatten elements into a new GdxArray") {
    val array = GdxArray.with(GdxArray.with(1), listOf<Int>(), LinkedList(arrayListOf(2, 3)))

    val result = array.flatten()

    result shouldBe GdxArray.with(1, 2, 3)
  }

  test("should map elements to lists and flatten them into a new GdxArray") {
    val array = GdxArray.with(1, 2, 3)

    val result = array.flatMap { counter -> List(counter) { counter } }

    result shouldBe GdxArray.with(1, 2, 2, 3, 3, 3)
  }

  test("should convert Array to ObjectSet") {
    val array = GdxArray.with("1", "2", "3")

    val set = array.toGdxSet()

    set shouldBe gdxSetOf("1", "2", "3")
  }

  test("should convert Iterables to Arrays") {
    val listAsArray = listOf("1", "2", "3").toGdxArray()

    listAsArray shouldBe gdxArrayOf("1", "2", "3")
  }

  test("should convert native Arrays to GdxArrays") {
    val array = arrayOf("1", "2", "3").toGdxArray()

    array shouldBe gdxArrayOf("1", "2", "3")
  }

  test("should convert native IntArrays to GdxIntArrays") {
    val intArray = intArrayOf(1, 2, 3).toGdxArray()

    intArray shouldBe GdxIntArray.with(1, 2, 3)
  }

  test("should convert native FloatArrays to GdxFloatArrays") {
    val floatArray = floatArrayOf(1f, 2f, 3f).toGdxArray()

    floatArray.size shouldBe 3
    (1f in floatArray) shouldBe true
    (2f in floatArray) shouldBe true
    (3f in floatArray) shouldBe true
    floatArray[0] shouldBe (1f plusOrMinus 0.001f)
    floatArray[1] shouldBe (2f plusOrMinus 0.001f)
    floatArray[2] shouldBe (3f plusOrMinus 0.001f)
  }

  test("should convert native BooleanArrays to GdxBooleanArrays") {
    val booleanArray = booleanArrayOf(true, false, true).toGdxArray()

    booleanArray.size shouldBe 3
    booleanArray[0] shouldBe true
    booleanArray[1] shouldBe false
    booleanArray[2] shouldBe true
  }

  test("should provide aliases for collections with conflicting names") {
    (GdxArray<Any>() is com.badlogic.gdx.utils.Array<Any>) shouldBe true
    (GdxIntArray() is com.badlogic.gdx.utils.IntArray) shouldBe true
    (GdxFloatArray() is com.badlogic.gdx.utils.FloatArray) shouldBe true
    (GdxBooleanArray() is com.badlogic.gdx.utils.BooleanArray) shouldBe true
    (GdxCharArray() is com.badlogic.gdx.utils.CharArray) shouldBe true
    (GdxLongArray() is com.badlogic.gdx.utils.LongArray) shouldBe true
    (GdxShortArray() is com.badlogic.gdx.utils.ShortArray) shouldBe true
  }

  test("should create gdx arrays from vararg elements") {
    gdxBooleanArrayOf(true, false) shouldBe GdxBooleanArray.with(true, false)
    gdxByteArrayOf(0, 1) shouldBe GdxByteArray.with(0, 1)
    gdxCharArrayOf('0', '1') shouldBe GdxCharArray.with('0', '1')
    gdxShortArrayOf(0, 1) shouldBe GdxShortArray.with(0, 1)
    gdxIntArrayOf(0, 1) shouldBe GdxIntArray.with(0, 1)
    gdxLongArrayOf(0L, 1L) shouldBe GdxLongArray.with(0L, 1L)
    gdxFloatArrayOf(0.1F, 0.2F) shouldBe GdxFloatArray.with(0.1F, 0.2F)
  }
})
