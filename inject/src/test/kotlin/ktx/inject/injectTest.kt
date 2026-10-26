package ktx.inject

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.reflect.ReflectionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import ktx.reflect.Reflection
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.Random

/**
 * Tests the KTX dependency injection module: [Context] implementation.
 */
class DependencyInjectionTest : FunSpec({
  val context = Context()

  test("should register components") {
    context.bind { listOf<Any>() }

    (List::class.java in context) shouldBe true
    (List::class in context) shouldBe true
  }

  test("should inject components") {
    context.bind { mutableListOf<Any>() }

    val list = context.inject<MutableList<String>>()

    (MutableList::class.java in context) shouldBe true
    (MutableList::class in context) shouldBe true
    list.shouldNotBeNull()
    list.add("Test")
    list.size shouldBe 1
    list[0] shouldBe "Test"
  }

  test("should bind singletons") {
    val singleton = Random()

    context.bindSingleton(singleton)

    context.contains<Random>() shouldBe true
    val provided = context.inject<Random>()
    provided shouldBeSameInstanceAs singleton
    context.inject<Random>() shouldBeSameInstanceAs context.inject<Random>()

    val provider = context.provider<Random>()
    provider() shouldBeSameInstanceAs singleton
    provider() shouldBeSameInstanceAs provider()
  }

  test("should bind singletons to custom type") {
    val singleton = java.lang.String("Singleton")

    context.bindSingleton<CharSequence>(singleton)

    context.contains<String>() shouldBe false
    context.contains<CharSequence>() shouldBe true
    val provided = context.inject<CharSequence>()
    provided shouldBeSameInstanceAs singleton
    context.inject<CharSequence>() shouldBeSameInstanceAs context.inject<CharSequence>()

    val provider = context.provider<CharSequence>()
    provider() shouldBeSameInstanceAs singleton
    provider() shouldBeSameInstanceAs provider()
  }

  test("should bind singletons to multiple types") {
    val singleton = java.lang.String("Singleton")

    context.bindSingleton(String::class, CharSequence::class) { singleton }

    context.contains<String>() shouldBe true
    context.contains<CharSequence>() shouldBe true
    context.inject<String>() shouldBeSameInstanceAs singleton
    context.provider<CharSequence>()() shouldBeSameInstanceAs context.inject<String>()
    context.inject<String>() shouldNotBeSameInstanceAs "Singleton"
  }

  test("should bind singletons using provider's result") {
    val singleton = java.lang.String("Singleton")

    context.bindSingleton { singleton }

    context.contains<String>() shouldBe true
    val provided = context.inject<String>()
    provided shouldBeSameInstanceAs singleton
    context.inject<String>() shouldBeSameInstanceAs context.inject<String>()

    val provider = context.provider<String>()
    provider() shouldBeSameInstanceAs singleton
    provider() shouldBeSameInstanceAs provider()
  }

  test("should bind singletons using provider's result to custom type") {
    val singleton = java.lang.String("Singleton")

    context.bindSingleton<CharSequence> { singleton }

    context.contains<String>() shouldBe false
    context.contains<CharSequence>() shouldBe true
    val provided = context.inject<CharSequence>()
    provided shouldBeSameInstanceAs singleton
    context.inject<CharSequence>() shouldBeSameInstanceAs context.inject<CharSequence>()

    val provider = context.provider<CharSequence>()
    provider() shouldBeSameInstanceAs singleton
    provider() shouldBeSameInstanceAs provider()
  }

  test("should bind singleton using provider's result to given class") {
    val singleton = java.lang.String("Singleton")

    context.bindSingleton(CharSequence::class) { singleton }

    context.contains<CharSequence>() shouldBe true
    val provided = context.inject<CharSequence>()
    provided shouldBeSameInstanceAs singleton
    context.inject<CharSequence>() shouldBeSameInstanceAs context.inject<CharSequence>()

    val provider = context.provider<CharSequence>()
    provider() shouldBeSameInstanceAs singleton
    provider() shouldBeSameInstanceAs provider()
  }

  test("should remove singletons") {
    val singleton = Random()
    context.bindSingleton(singleton)

    val singletonProvider = context.remove<Random>()

    context.contains<Random>() shouldBe false
    singletonProvider.shouldNotBeNull()
    singletonProvider?.invoke() shouldBeSameInstanceAs singleton
  }

  test("should remove providers") {
    val provider = { Random() }
    context.bind(provider)

    val removed = context.remove<Random>()

    context.contains<Random>() shouldBe false
    removed.shouldNotBeNull()
    removed shouldBeSameInstanceAs provider
  }

  test("should not throw an exception when trying to remove absent provider") {
    val removed = context.remove<Random>()

    removed.shouldBeNull()
  }

  test("should inject providers") {
    context.bind { Random() }

    val provider = context.provider<Random>()

    context.contains<Random>() shouldBe true
    provider.shouldNotBeNull()
    val random1 = provider()
    val random2 = provider()
    random1.shouldNotBeNull()
    random2.shouldNotBeNull()
    random2 shouldNotBeSameInstanceAs random1
  }

  test("should bind providers to multiple types") {
    context.bind(String::class, CharSequence::class) { java.lang.String("New") }

    context.contains<String>() shouldBe true
    context.contains<CharSequence>() shouldBe true
    context.provider<CharSequence>()() shouldNotBeSameInstanceAs context.inject<String>()
  }

  test("should throw exception when trying to register provider for same type multiple times") {
    shouldThrow<InjectionException> {
      context.bind { "Test." }
      context.bind { "Should throw." }
    }
  }

  test("should throw exception when trying to register singleton of the same type multiple times") {
    shouldThrow<InjectionException> {
      context.bindSingleton("Test.")
      context.bindSingleton("Should throw.")
    }
  }

  test("should throw exception when trying to register provider and singleton of same type") {
    shouldThrow<InjectionException> {
      context.bindSingleton("Test.")
      context.bind { "Should throw." }
    }
  }

  test("should throw exception when trying to register provider for same types") {
    shouldThrow<InjectionException> {
      context.bind(String::class, String::class) { "Should throw." }
    }
  }

  test("should throw exception when trying to register singleton for same types") {
    shouldThrow<InjectionException> {
      context.bindSingleton(String::class, String::class, singleton = "Should throw.")
    }
  }

  test("should throw exception when trying to register singleton via provider for same types") {
    shouldThrow<InjectionException> {
      context.bindSingleton(String::class, String::class) { "Should throw." }
    }
  }

  test("should inject context") {
    val injected = context.inject<Context>()

    injected.shouldNotBeNull()
    (Context::class.java in context) shouldBe true
    context.contains<Context>() shouldBe true
    injected shouldBeSameInstanceAs context
  }

  test("should remove providers on clear except for Context provider") {
    context.bind { "Test" }

    context.clear()

    context.contains<Context>() shouldBe true
    context.contains<String>() shouldBe false
  }

  test("should fill context with builder DSL") {
    context.register {
      bind { Random() }
      bindSingleton("Test")
    }

    context.contains<Random>() shouldBe true
    context.contains<String>() shouldBe true
  }

  test("should configure context exactly once") {
    val variable: Int

    context.register {
      variable = 42
    }

    variable shouldBe 42
  }

  test("should throw exception upon injection of missing type") {
    shouldThrow<InjectionException> {
      context.inject<String>()
    }
  }

  test("should throw exception upon injection of provider of missing type") {
    shouldThrow<InjectionException> {
      context.provider<String>()
    }
  }

  test("should inject instances with invocation syntax") {
    context.bind { Random() }

    val injected: Random = context()

    injected.shouldNotBeNull()
  }

  test("should remove providers on dispose except for Context provider") {
    context.bind { "Test" }

    context.dispose()

    context.contains<Context>() shouldBe true
    context.contains<String>() shouldBe false
  }

  test("should dispose of Disposable components") {
    val nonDisposable = Any()
    context.bindSingleton(nonDisposable)
    val singleton = mock<Disposable>()
    context.bindSingleton(singleton)
    val provider = mock<DisposableProvider<Random>>()
    context.bind(provider)

    context.dispose()

    context.contains<Any>() shouldBe false
    context.contains<Disposable>() shouldBe false
    verify(singleton).dispose()
    context.contains<Random>() shouldBe false
    verify(provider).dispose()
  }

  test("should dispose of Disposable components with error handling") {
    Gdx.app = mock()
    val singleton =
      mock<Disposable> {
        on(it.dispose()) doThrow GdxRuntimeException("Expected.")
      }
    context.bindSingleton(singleton)
    val provider = mock<DisposableProvider<Random>>()
    context.bind(provider)

    context.dispose()

    context.contains<Disposable>() shouldBe false
    verify(singleton).dispose()
    verify(Gdx.app).error(eq("KTX"), any(), argThat { this is GdxRuntimeException }) // Ensures exception was logged.
    context.contains<Random>() shouldBe false
    verify(provider).dispose()
  }

  afterTest {
    context.clear()
  }
})

/** Implements both [Disposable] and provider interfaces. */
interface DisposableProvider<out Type> :
  Disposable,
  () -> Type

/**
 * Tests specialized [SingletonProvider] that allows to dispose of singletons registered in a [Context].
 */
class SingletonProviderTest : FunSpec({
  test("should not throw exception when trying to dispose singleton that does not implement Disposable") {
    val singletonProvider = SingletonProvider(Random())

    singletonProvider.dispose()
  }

  test("should dispose of Disposable singleton") {
    val singleton = mock<Disposable>()
    val singletonProvider = SingletonProvider(singleton)

    singletonProvider.dispose()

    verify(singleton).dispose()
  }

  test("should rethrow Disposable exceptions") {
    shouldThrow<GdxRuntimeException> {
      val singleton =
        mock<Disposable> {
          on(it.dispose()) doThrow GdxRuntimeException("Expected.")
        }
      val singletonProvider = SingletonProvider(singleton)

      singletonProvider.dispose()
    }
  }
})

@OptIn(Reflection::class)
class ReflectionInjectionTest : FunSpec({
  test("should create instance via reflection") {
    // Given:
    val context = Context()

    // When:
    val instance = context.newInstanceOf<Dependency>()

    // Then:
    instance.shouldNotBeNull()
  }

  test("should create instance with constructor parameters via reflection") {
    // Given:
    val context = Context()
    val dependency = Dependency()
    context.bindSingleton(dependency)

    // When:
    val instance = context.newInstanceOf<Dependant>()

    // Then:
    instance.shouldNotBeNull()
    instance.dependency shouldBeSameInstanceAs dependency
  }

  test("should fail to create instance with missing dependencies via reflection") {
    // Given:
    val context = Context()

    // Expect:
    shouldThrow<InjectionException> {
      context.newInstanceOf<Dependant>()
    }
  }

  test("should fail to create instance of class with no public constructors via reflection") {
    // Given:
    val context = Context()

    // Expect:
    shouldThrow<ReflectionException> {
      context.newInstanceOf<InvalidClass>()
    }
  }

  test("should fail to create instance of class with multiple public constructors via reflection") {
    // Given:
    val context = Context()

    // Expect:
    shouldThrow<ReflectionException> {
      context.newInstanceOf<MultipleConstructors>()
    }
  }

  test("should bind a provider") {
    // Given:
    val context = Context()
    val dependency = Dependency()
    context.bindSingleton(dependency)

    // When:
    context.bind<Dependant>()

    // Then:
    context.inject<Dependant>().shouldNotBeNull()
    context.inject<Dependant>().dependency shouldBeSameInstanceAs dependency
  }

  test("should create a new instance via reflection on every provider call") {
    // Given:
    val context = Context()

    // When:
    context.bind<Dependency>()

    // Then:
    context.inject<Dependency>() shouldNotBeSameInstanceAs context.inject<Dependency>()
  }

  test("should bind a singleton") {
    // Given:
    val context = Context()
    context.bind<Dependency>()

    // When:
    val instance = context.bindSingleton<Dependant>()

    // Then:
    instance.shouldNotBeNull()
    instance.dependency.shouldNotBeNull()
    context.inject<Dependant>() shouldBeSameInstanceAs instance
    context.inject<Dependant>() shouldBeSameInstanceAs context.inject<Dependant>()
  }
})

class Dependency

class Dependant(
  val dependency: Dependency,
)

class InvalidClass private constructor()

@Suppress("unused", "UNUSED_PARAMETER")
class MultipleConstructors() {
  constructor(a: String) : this()
}
