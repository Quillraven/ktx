package ktx.assets

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pool.Poolable
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests [Pool] extensions.
 */
class PoolsTest : FunSpec({
  test("should invoke pool as no parameter function to provide instances") {
    // Given:
    val pool = MockPool()

    // When: Pool is called as a function.
    val instance = pool()

    // Then: Should work as "obtain":
    instance.shouldNotBeNull()
    pool.free(instance)
    pool() shouldBeSameInstanceAs instance // Since the object was freed, pool should return the same instance.
  }

  test("should invoke pool as one parameter function to return instances") {
    // Given:
    val pool = MockPool()
    val instance = pool.obtain()

    // When: Pool is called as a function with object parameter.
    pool(instance)

    // Then: Should work as "free".
    pool.free shouldBe 1
    pool.obtain() shouldBeSameInstanceAs instance // Since the object was freed, pool should return the same instance.
  }

  test("should create new pools with custom providers") {
    // Given: A pool that always returns the same instance:
    val provided = "10"
    val pool = pool { provided }

    // When:
    val obtained = pool()

    // Then:
    obtained shouldBeSameInstanceAs provided
  }

  test("should honor max setting") {
    // Given:
    val pool = pool(max = 5) { "Mock." }

    // When:
    for (index in 1..10) pool.free("Value.")

    // Then:
    pool.free shouldBe 5
  }

  test("should create new pools with a custom discard function") {
    // Given: A pool that adds discarded objects to a list:
    val discarded = mutableListOf<String>()
    val pool = pool(max = 5, discard = { discarded.add(it) }) { "Mock." }

    // When:
    for (index in 1..10) pool.free("Value$index")

    // Then:
    discarded shouldBe listOf("Value6", "Value7", "Value8", "Value9", "Value10")
  }

  test("should create new pools that reset discarded objects by default") {
    // Given:
    val pool = pool(max = 1) { SamplePoolable() }
    val freed = pool()
    val discarded = pool()
    pool.free(freed)

    // When:
    pool.free(discarded)

    // Then:
    discarded.isReset shouldBe true
  }
})

/**
 * A simple data object implementing the [Poolable] interface.
 */
private class SamplePoolable(
  var isReset: Boolean = false,
) : Poolable {
  override fun reset() {
    this.isReset = true
  }
}

/**
 * Provides new [Any] instances.
 */
private class MockPool : Pool<Any>() {
  override fun newObject(): Any = Any()
}
