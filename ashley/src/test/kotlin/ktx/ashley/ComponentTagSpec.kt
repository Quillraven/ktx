package ktx.ashley

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FlagComponent : Component

class NoDefaultArgConstructorComponent(
  val text: String,
) : Component

var Entity.singletonTagCreatedByReflection by tagFor<FlagComponent>()
var Entity.providerTagCreatedByReflection by tagFor<FlagComponent>(singleton = false)
var Entity.singletonTagCreatedWithProvider by tagFor<FlagComponent> { FlagComponent() }
var Entity.providerTagCreatedWithProvider by tagFor(singleton = false) { FlagComponent() }
var Entity.singletonTagCreatedWithInstance by tagFor(FlagComponent())
val Entity.readOnlyTag by tagFor<NoDefaultArgConstructorComponent>()

class ComponentTagSpec : FunSpec({
  context("utilities tag component delegates") {
    val entity = Entity()
    val mapper = mapperFor<FlagComponent>()

    test("should detect if component is not defined with singleton tag created by reflection") {
      entity.singletonTagCreatedByReflection shouldBe false
    }
    test("should detect if component is defined with singleton tag created by reflection") {
      entity.add(FlagComponent())
      entity.singletonTagCreatedByReflection shouldBe true
    }
    test("should add component with singleton tag created by reflection") {
      entity.singletonTagCreatedByReflection = true
      entity.has(mapper) shouldBe true
    }
    test("should remove component with singleton tag created by reflection") {
      entity.add(FlagComponent())
      entity.singletonTagCreatedByReflection = false
      entity.has(mapper) shouldBe false
    }

    test("should detect if component is not defined with provider tag created by reflection") {
      entity.providerTagCreatedByReflection shouldBe false
    }
    test("should detect if component is defined with provider tag created by reflection") {
      entity.add(FlagComponent())
      entity.providerTagCreatedByReflection shouldBe true
    }
    test("should add component with provider tag created by reflection") {
      entity.providerTagCreatedByReflection = true
      entity.has(mapper) shouldBe true
    }
    test("should remove component with provider tag created by reflection") {
      entity.add(FlagComponent())
      entity.providerTagCreatedByReflection = false
      entity.has(mapper) shouldBe false
    }

    test("should detect if component is not defined with singleton tag created with provider") {
      entity.singletonTagCreatedWithProvider shouldBe false
    }
    test("should detect if component is defined with singleton tag created with provider") {
      entity.add(FlagComponent())
      entity.singletonTagCreatedWithProvider shouldBe true
    }
    test("should add component with singleton tag created with provider") {
      entity.singletonTagCreatedWithProvider = true
      entity.has(mapper) shouldBe true
    }
    test("should remove component with singleton tag created with provider") {
      entity.add(FlagComponent())
      entity.singletonTagCreatedWithProvider = false
      entity.has(mapper) shouldBe false
    }

    test("should detect if component is not defined with provider tag created with provider") {
      entity.providerTagCreatedWithProvider shouldBe false
    }
    test("should detect if component is defined with provider tag created with provider") {
      entity.add(FlagComponent())
      entity.providerTagCreatedWithProvider shouldBe true
    }
    test("should add component with provider tag created with provider") {
      entity.providerTagCreatedWithProvider = true
      entity.has(mapper) shouldBe true
    }
    test("should remove component with provider tag created with provider") {
      entity.add(FlagComponent())
      entity.providerTagCreatedWithProvider = false
      entity.has(mapper) shouldBe false
    }

    test("should detect if component is not defined with singleton tag created with instance") {
      entity.singletonTagCreatedWithInstance shouldBe false
    }
    test("should detect if component is defined with singleton tag created with instance") {
      entity.add(FlagComponent())
      entity.singletonTagCreatedWithInstance shouldBe true
    }
    test("should add component with singleton tag created with instance") {
      entity.singletonTagCreatedWithInstance = true
      entity.has(mapper) shouldBe true
    }
    test("should remove component with singleton tag created with instance") {
      entity.add(FlagComponent())
      entity.singletonTagCreatedWithInstance = false
      entity.has(mapper) shouldBe false
    }

    test("should support read-only tags with no default argument constructors") {
      entity.readOnlyTag shouldBe false
      entity.add(NoDefaultArgConstructorComponent("test"))
      entity.readOnlyTag shouldBe true
    }
  }
})
