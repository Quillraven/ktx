package ktx.collections

import com.badlogic.gdx.utils.ObjectSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.LinkedList

/**
 * Tests utilities for libGDX custom HashSet equivalent - [ObjectSet].
 */
class SetsTest : FunSpec({
  test("should create GdxSet") {
    val set = gdxSetOf<Any>()

    set.shouldNotBeNull()
    set.size shouldBe 0
  }

  test("should create GdxSet with custom initial capacity") {
    val set = gdxSetOf<Any>(initialCapacity = 32)

    set.shouldNotBeNull()
    set.size shouldBe 0
  }

  test("should create GdxSet with custom load factor") {
    val set = gdxSetOf<Any>(loadFactor = 0.4f)

    set.shouldNotBeNull()
    set.size shouldBe 0
  }

  test("should create GdxSet with custom elements") {
    val set = gdxSetOf("1", "2", "3")

    set.size shouldBe 3
    ("1" in set) shouldBe true
    ("2" in set) shouldBe true
    ("3" in set) shouldBe true
  }

  test("should report size") {
    val set: ObjectSet<String>? = ObjectSet.with("1", "2", "3")

    set.size() shouldBe 3
    set.size() shouldBe set!!.size
    val nullSet: ObjectSet<Any>? = null
    nullSet.size() shouldBe 0
  }

  test("should report empty status") {
    ObjectSet.with("1", "2", "3").isEmpty() shouldBe false
    ObjectSet<Any>().isEmpty() shouldBe true
    (null as ObjectSet<Any>?).isEmpty() shouldBe true
  }

  test("should verify empty status contract") {
    fun getSet(): GdxSet<Any>? = ObjectSet.with("1")

    val set = getSet()
    if (!set.isEmpty()) {
      (set.size == 1) shouldBe true
    }
  }

  test("should report non empty status") {
    ObjectSet.with("1", "2", "3").isNotEmpty() shouldBe true
    ObjectSet<Any>().isNotEmpty() shouldBe false
    (null as ObjectSet<Any>?).isNotEmpty() shouldBe false
  }

  test("should verify non empty status contract") {
    fun getSet(): GdxSet<Any>? = ObjectSet.with("1")

    val set = getSet()
    if (set.isNotEmpty()) {
      (set.size == 1) shouldBe true
    }
  }

  test("should add all values from custom iterable") {
    val set = ObjectSet<String>()

    set.addAll(listOf("1", "2", "3"))

    set shouldBe gdxSetOf("1", "2", "3")
  }

  test("should remove all values from custom iterable") {
    val set = ObjectSet.with("1", "2", "3")

    set.removeAll(listOf("1", "2", "3"))

    set shouldBe gdxSetOf<String>()
  }

  test("should remove all values from Array") {
    val set = ObjectSet.with("1", "2", "3")

    set.removeAll(arrayOf("1", "2", "3"))

    set shouldBe gdxSetOf<String>()
  }

  test("should add values with += operator") {
    val set = ObjectSet.with("1", "2")

    set += "3"

    set shouldBe gdxSetOf("1", "2", "3")
  }

  test("should add values with + operator") {
    val set = ObjectSet<String>()

    val result = set + "1"

    result shouldBe gdxSetOf("1")
    set shouldBe gdxSetOf<String>()

    val chained = result + "2" + "3"

    chained shouldBe gdxSetOf("1", "2", "3")
    result shouldBe gdxSetOf("1")
  }

  test("should remove values with -= operator") {
    val set = ObjectSet.with("1", "2", "3")

    set -= "1"

    set shouldBe gdxSetOf("2", "3")

    set -= "4"

    set shouldBe gdxSetOf("2", "3")
  }

  test("should remove values with - operator") {
    val set = ObjectSet.with("1", "2", "3")

    val result = set - "1"

    result shouldBe gdxSetOf("2", "3")
    set shouldBe gdxSetOf("1", "2", "3")

    val chained = result - "2" - "3"

    chained shouldBe gdxSetOf<String>()
    result shouldBe gdxSetOf("2", "3")
  }

  test("should chain operators") {
    val set = ObjectSet.with("1", "2", "3")

    val result = set + "5" - "2" + "7"

    result shouldBe gdxSetOf("1", "3", "5", "7")
    set shouldBe gdxSetOf("1", "2", "3")
  }

  test("should add elements from iterables with += operator") {
    val set = ObjectSet.with("1", "2")

    set += ObjectSet.with("2", "3")

    set shouldBe ObjectSet.with("1", "2", "3")
  }

  test("should add elements from iterables with + operator") {
    val set = ObjectSet.with("1", "2")

    val result = set + ObjectSet.with("2", "3")

    result shouldBe ObjectSet.with("1", "2", "3")
    set shouldBe ObjectSet.with("1", "2")
  }

  test("should add elements from Arrays with += operator") {
    val set = ObjectSet.with("1", "2")

    set += arrayOf("2", "3")

    set shouldBe ObjectSet.with("1", "2", "3")
  }

  test("should add elements from Arrays with + operator") {
    val set = ObjectSet.with("1", "2")

    val result = set + arrayOf("2", "3")

    result shouldBe ObjectSet.with("1", "2", "3")
    set shouldBe ObjectSet.with("1", "2")
  }

  test("should remove elements from iterables with -= operator") {
    val set = ObjectSet.with("1", "2", "3")

    set -= ObjectSet.with("2", "3", "4")

    set shouldBe gdxSetOf("1")
  }

  test("should remove elements from Arrays with -= operator") {
    val set = ObjectSet.with("1", "2", "3")

    set -= arrayOf("2", "3", "4")

    set shouldBe gdxSetOf("1")
  }

  test("should remove elements from iterables with - operator") {
    val set = ObjectSet.with("1", "2", "3")

    val result = set - ObjectSet.with("2", "3", "4")

    result shouldBe gdxSetOf("1")
    set shouldBe gdxSetOf("1", "2", "3")
  }

  test("should remove elements from Arrays with - operator") {
    val set = ObjectSet.with("1", "2", "3")

    val result = set - arrayOf("2", "3", "4")

    result shouldBe gdxSetOf("1")
    set shouldBe gdxSetOf("1", "2", "3")
  }

  test("should chain collection operators") {
    val set =
      ObjectSet.with("1", "2", "3", "4") +
        ObjectSet.with("3", "5") -
        arrayOf("2", "4", "6") +
        arrayOf("5", "7")

    set shouldBe ObjectSet.with("1", "3", "5", "7")
  }

  test("should allow to iterate with iterator reference") {
    val set = ObjectSet.with("1", "2", "3")

    set.iterate { value, iterator -> if (value == "2") iterator.remove() }

    set.size shouldBe 2
    ("2" in set) shouldBe false
  }

  test("should map elements into a new GdxSet") {
    val set = GdxSet.with(1, 2, 3)

    val result = set.map { it * 2 }

    result shouldBe gdxSetOf(4, 2, 6)
  }

  test("should filter elements into a new GdxSet") {
    val set = GdxSet.with(1, 2, 3, 4, 5)

    val result = set.filter { it % 2 == 1 }

    result shouldBe gdxSetOf(3, 1, 5)
  }

  test("should flatten elements into a new GdxSet") {
    val set = GdxSet.with(GdxArray.with(1, 2), listOf<Int>(), LinkedList(arrayListOf(2, 3)))

    val result = set.flatten()

    result shouldBe gdxSetOf(1, 2, 3)
  }

  test("should map elements to lists and flatten them into a new GdxSet") {
    val set = GdxSet.with(1, 2, 3)

    val result = set.flatMap { count -> List(count) { it } }

    result shouldBe gdxSetOf(0, 1, 2)
  }

  test("should convert Set to Array") {
    val setAsArray = ObjectSet.with("1", "2", "3").toGdxArray()

    setAsArray.size shouldBe 3
    ("1" in setAsArray) shouldBe true
    ("2" in setAsArray) shouldBe true
    ("3" in setAsArray) shouldBe true
  }

  test("should convert iterables to Sets") {
    val listAsSet = listOf("1", "2", "3").toGdxSet()

    listAsSet.size shouldBe 3
    ("1" in listAsSet) shouldBe true
    ("2" in listAsSet) shouldBe true
    ("3" in listAsSet) shouldBe true
  }

  test("should convert Arrays to Sets") {
    val arrayAsSet = arrayOf("1", "2", "3").toGdxSet()

    arrayAsSet.size shouldBe 3
    ("1" in arrayAsSet) shouldBe true
    ("2" in arrayAsSet) shouldBe true
    ("3" in arrayAsSet) shouldBe true
  }

  test("should convert IntArrays to IntSets") {
    val arrayAsSet = intArrayOf(1, 2, 3).toGdxSet()

    arrayAsSet.size shouldBe 3
    (1 in arrayAsSet) shouldBe true
    (2 in arrayAsSet) shouldBe true
    (3 in arrayAsSet) shouldBe true
  }

  test("should provide alias for compatibility with other libGDX collections") {
    (GdxSet<Any>() is ObjectSet<Any>) shouldBe true
  }
})
