package ktx.ashley

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

class ComponentWithString(
  var string: String = "",
) : Component

class ComponentWithInt(
  var integer: Int = 0,
) : Component

var Entity.mandatoryComponent by propertyFor<ComponentWithString>()
var Entity.optionalComponent by optionalPropertyFor<ComponentWithInt>()

class ComponentPropertiesSpec : FunSpec({
  context("utilities for component properties") {
    val mandatory = ComponentWithString()
    val optional = ComponentWithInt()
    val entity =
      Entity().apply {
        add(mandatory)
        add(optional)
      }
    test("should return a mandatory component value via property") {
      entity.mandatoryComponent.shouldNotBeNull()
    }
    test("should return an optional component value via property") {
      entity.optionalComponent.shouldNotBeNull()
    }
    test("should modify mandatory component variables via property") {
      entity.mandatoryComponent.string = "test"
      mandatory.string shouldBe "test"
    }
    test("should modify optional component variables via property") {
      entity.optionalComponent?.integer = 42
      optional.integer shouldBe 42
    }
    test("should replace mandatory component instance via property") {
      entity.mandatoryComponent = ComponentWithString("new")
      entity.mandatoryComponent.string shouldBe "new"
      mandatory.string shouldNotBe entity.mandatoryComponent.string
      entity.getComponent(ComponentWithString::class.java) shouldNotBeSameInstanceAs mandatory
    }
    test("should replace optional component instance via property") {
      entity.optionalComponent = ComponentWithInt(11)
      entity.optionalComponent?.integer shouldBe 11
      optional.integer shouldNotBe entity.optionalComponent!!.integer
      entity.getComponent(ComponentWithInt::class.java) shouldNotBeSameInstanceAs optional
    }
    test("should remove optional component via property") {
      entity.optionalComponent = null
      entity.optionalComponent.shouldBeNull()
      entity.getComponent(ComponentWithInt::class.java).shouldBeNull()
    }
  }
})
