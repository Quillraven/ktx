package ktx.reflect

import com.badlogic.gdx.utils.reflect.ReflectionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

interface Super

open class Tested : Super

annotation class Annotation(
  val value: String,
)

@Annotation("Test")
data class Extension(
  val a: String,
  val b: String,
) : Tested() {
  @Suppress("unused")
  fun test(c: String): String = a + b + c
}

@Suppress("unused")
class Container(
  @JvmField val field: String,
)

enum class Enum { RED, BLUE }

@OptIn(Reflection::class)
class ReflectTest : FunSpec({
  test("should wrap selected class") {
    // When:
    val reflectedClass = reflect<Tested>()

    // Then:
    reflectedClass.javaClass shouldBeSameInstanceAs Tested::class.java
  }

  test("should wrap selected Kotlin class") {
    // When:
    val reflectedClass = reflect(Tested::class)

    // Then:
    reflectedClass.javaClass shouldBeSameInstanceAs Tested::class.java
  }

  test("should wrap selected Java class") {
    // When:
    val reflectedClass = reflect(Tested::class.java)

    // Then:
    reflectedClass.javaClass shouldBeSameInstanceAs Tested::class.java
  }

  test("should wrap selected class by name") {
    // When:
    val reflectedClass = reflect("ktx.reflect.Tested")

    // Then:
    reflectedClass.javaClass shouldBeSameInstanceAs Tested::class.java
  }

  test("should wrap selected Kotlin class via constructor") {
    // When:
    val reflectedClass = ReflectedClass(Tested::class)

    // Then:
    reflectedClass.javaClass shouldBeSameInstanceAs Tested::class.java
  }

  test("should return simple name") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val name = reflectedClass.simpleName

    // Then:
    name shouldBe "Tested"
  }

  test("should test if an object is an instance of a class") {
    // Given:
    val reflectedClass = reflect<Tested>()
    val instance = Tested()

    // When:
    val isInstance = reflectedClass.isInstance(instance)

    // Then:
    isInstance shouldBe true
  }

  test("should test if an object is not an instance of a class") {
    // Given:
    val reflectedClass = reflect<Tested>()
    val instance = "Tested"

    // When:
    val isInstance = reflectedClass.isInstance(instance)

    // Then:
    isInstance shouldBe false
  }

  test("should test if null is an instance of a class") {
    // Given:
    val reflectedClass = reflect<Tested>()
    val instance = null

    // When:
    val isInstance = reflectedClass.isInstance(instance)

    // Then:
    isInstance shouldBe false
  }

  test("should test if is assignable from the same class") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isAssignable = reflectedClass.isAssignableFrom<Tested>()

    // Then:
    isAssignable shouldBe true
  }

  test("should test if is assignable from a class extension") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isAssignable = reflectedClass.isAssignableFrom<Extension>()

    // Then:
    isAssignable shouldBe true
  }

  test("should test if is assignable from a different class") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isAssignable = reflectedClass.isAssignableFrom<String>()

    // Then:
    isAssignable shouldBe false
  }

  test("should check if is a member class") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isMemberClass = reflectedClass.isMemberClass

    // Then:
    isMemberClass shouldBe false
  }

  test("should check if is a static class") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isStatic = reflectedClass.isStatic

    // Then:
    isStatic shouldBe false
  }

  test("should check if is an array class") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isArray = reflectedClass.isArray

    // Then:
    isArray shouldBe false
  }

  test("should check if is an array class given an array") {
    // Given:
    val reflectedClass = reflect<Array<Tested>>()

    // When:
    val isArray = reflectedClass.isArray

    // Then:
    isArray shouldBe true
  }

  test("should check if is a primitive class") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isPrimitive = reflectedClass.isPrimitive

    // Then:
    isPrimitive shouldBe false
  }

  test("should check if is an enum") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isEnum = reflectedClass.isEnum

    // Then:
    isEnum shouldBe false
  }

  test("should check if is an enum given an enum") {
    // Given:
    val reflectedClass = reflect<Enum>()

    // When:
    val isEnum = reflectedClass.isEnum

    // Then:
    isEnum shouldBe true
  }

  test("should check if is an annotation") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isAnnotation = reflectedClass.isAnnotation

    // Then:
    isAnnotation shouldBe false
  }

  test("should check if is an annotation class given an annotation") {
    // Given:
    val reflectedClass = reflect<Override>()

    // When:
    val isAnnotation = reflectedClass.isAnnotation

    // Then:
    isAnnotation shouldBe true
  }

  test("should check if is an interface") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isInterface = reflectedClass.isInterface

    // Then:
    isInterface shouldBe false
  }

  test("should check if is an interface class given an interface") {
    // Given:
    val reflectedClass = reflect<Super>()

    // When:
    val isInterface = reflectedClass.isInterface

    // Then:
    isInterface shouldBe true
  }

  test("should check if is an abstract class") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isAbstract = reflectedClass.isAbstract

    // Then:
    isAbstract shouldBe false
  }

  test("should check if is an abstract class given an interface") {
    // Given:
    val reflectedClass = reflect<Super>()

    // When:
    val isAbstract = reflectedClass.isAbstract

    // Then:
    isAbstract shouldBe true
  }

  test("should return no component type if the class is not an array") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val componentType = reflectedClass.componentType

    // Then:
    componentType.shouldBeNull()
  }

  test("should return no type if the class is an array") {
    // Given:
    val reflectedClass = reflect<Array<Tested>>()

    // When:
    val componentType = reflectedClass.componentType

    // Then:
    componentType shouldBeSameInstanceAs Tested::class.java
  }

  test("should find constructors") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val constructors = reflectedClass.constructors

    // Then:
    constructors.size shouldBe 1
    constructors.first().parameterTypes.size shouldBe 0
  }

  test("should find the only constructor") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val constructor = reflectedClass.constructor

    // Then:
    constructor.parameterTypes.size shouldBe 0
    (constructor.newInstance() is Tested) shouldBe true
  }

  test("should fail to find a single constructor if a class has multiple constructors") {
    // Given:
    val reflectedClass = reflect<String>()

    // Expect:
    shouldThrow<ReflectionException> {
      reflectedClass.constructor
    }
  }

  test("should find methods") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val methods = reflectedClass.methods

    // Then:
    methods.size shouldBe Extension::class.java.methods.size
  }

  test("should find declared methods") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val methods = reflectedClass.declaredMethods

    // Then:
    methods.size shouldBe Extension::class.java.declaredMethods.size
  }

  test("should find fields") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val fields = reflectedClass.fields

    // Then: should not find private backing fields:
    val fieldNames = fields.map { it.name }.toSet()
    ("a" in fieldNames) shouldBe false
    ("b" in fieldNames) shouldBe false
  }

  test("should find declared fields") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val fields = reflectedClass.declaredFields

    // Then: should return custom fields plus internal field.
    val fieldNames = fields.map { it.name }.toSet()
    ("a" in fieldNames) shouldBe true
    ("b" in fieldNames) shouldBe true
  }

  test("should find annotations") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val annotations = reflectedClass.annotations.map { it.annotationType }.toSet()

    // Then:
    annotations shouldBe setOf(Annotation::class.java, Metadata::class.java)
  }

  test("should find declared annotations") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val annotations = reflectedClass.declaredAnnotations.map { it.annotationType }.toSet()

    // Then:
    annotations shouldBe setOf(Annotation::class.java, Metadata::class.java)
  }

  test("should find interfaces") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val interfaces = reflectedClass.interfaces

    // Then:
    interfaces shouldBe arrayOf(Super::class.java)
  }

  test("should find enum constants") {
    // Given:
    val reflectedClass = reflect<Enum>()

    // When:
    val constants = reflectedClass.enumConstants

    // Then:
    constants shouldBe arrayOf(Enum.RED, Enum.BLUE)
  }

  test("should not find enum constants if the class is not an enum") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val constants = reflectedClass.enumConstants

    // Then:
    constants.shouldBeNull()
  }

  test("should create a new instance") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val instance = reflectedClass.newInstance()

    // Then:
    instance.shouldNotBeNull()
    reflectedClass.newInstance() shouldNotBeSameInstanceAs instance
  }

  test("should create an array instance") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val array = reflectedClass.newArrayInstance(3)

    // Then:
    array shouldBe arrayOfNulls<Tested>(3)
  }

  test("should create an array instance with 0 elements") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val array = reflectedClass.newArrayInstance(0)

    // Then:
    array shouldBe arrayOfNulls<Tested>(0)
  }

  test("should not create an array instance with negative size ") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    shouldThrow<ReflectionException> {
      reflectedClass.newArrayInstance(-1)
    }
  }

  test("should fail to create a new instance if a class does not have a public no-arg constructor") {
    // Given:
    val reflectedClass = reflect<Int>()

    // Expect:
    shouldThrow<ReflectionException> {
      reflectedClass.newInstance()
    }
  }

  test("should find constructor given parameter types") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val constructor = reflectedClass.getConstructor(String::class, String::class)

    // Then:
    constructor.parameterTypes shouldBe arrayOf(String::class.java, String::class.java)
    constructor.newInstance("a", "b") shouldBe Extension("a", "b")
  }

  test("should find declared constructor given parameter types") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val constructor = reflectedClass.getDeclaredConstructor(String::class, String::class)

    // Then:
    constructor.parameterTypes shouldBe arrayOf(String::class.java, String::class.java)
    constructor.newInstance("a", "b") shouldBe Extension("a", "b")
  }

  test("should find a method given name and parameter types") {
    // Given:
    val reflectedClass = reflect<Extension>()
    val instance = Extension("a", "b")

    // When:
    val method = reflectedClass.getMethod("test", String::class)

    // Then:
    method.name shouldBe "test"
    method.parameterTypes shouldBe arrayOf(String::class.java)
    method.invoke(instance, "c") shouldBe "abc"
  }

  test("should find a declared method given name and parameter types") {
    // Given:
    val reflectedClass = reflect<Extension>()
    val instance = Extension("a", "b")

    // When:
    val method = reflectedClass.getDeclaredMethod("test", String::class)

    // Then:
    method.name shouldBe "test"
    method.parameterTypes shouldBe arrayOf(String::class.java)
    method.invoke(instance, "c") shouldBe "abc"
  }

  test("should find a field by name") {
    // Given:
    val reflectedClass = reflect<Container>()
    val instance = Container("test")

    // When:
    val field = reflectedClass.getField("field")

    // Then:
    field.name shouldBe "field"
    field.type shouldBeSameInstanceAs String::class.java
    field.get(instance) shouldBe "test"
  }

  test("should find a declared field by name") {
    // Given:
    val reflectedClass = reflect<Container>()
    val instance = Container("test")

    // When:
    val field = reflectedClass.getDeclaredField("field")

    // Then:
    field.name shouldBe "field"
    field.type shouldBeSameInstanceAs String::class.java
    field.get(instance) shouldBe "test"
  }

  test("should check if annotation is present") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val isPresent = reflectedClass.isAnnotationPresent<Annotation>()

    // Then:
    isPresent shouldBe true
  }

  test("should check if annotation is not present") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val isPresent = reflectedClass.isAnnotationPresent<Annotation>()

    // Then:
    isPresent shouldBe false
  }

  test("should find an annotation by type") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val annotation = reflectedClass.getAnnotation<Annotation>()

    // Then:
    annotation.shouldNotBeNull()
    annotation?.get<Annotation>()?.value shouldBe "Test"
  }

  test("should not find an annotation by type") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val annotation = reflectedClass.getAnnotation<Annotation>()

    // Then:
    annotation.shouldBeNull()
  }

  test("should find a declared annotation by type") {
    // Given:
    val reflectedClass = reflect<Extension>()

    // When:
    val annotation = reflectedClass.getDeclaredAnnotation<Annotation>()

    // Then:
    annotation.shouldNotBeNull()
    annotation?.get<Annotation>()?.value shouldBe "Test"
  }

  test("should not find a declared annotation by type") {
    // Given:
    val reflectedClass = reflect<Tested>()

    // When:
    val annotation = reflectedClass.getDeclaredAnnotation<Annotation>()

    // Then:
    annotation.shouldBeNull()
  }
})
