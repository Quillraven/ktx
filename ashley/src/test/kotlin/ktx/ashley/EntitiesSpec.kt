package ktx.ashley

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

class EntitiesSpec : FunSpec({
  context("utilities for entities") {
    val transform by lazy {
      Transform()
    }
    val entity by lazy {
      Entity().apply {
        add(transform)
      }
    }
    val engine by lazy {
      PooledEngine()
    }
    context("get operator") {
      test("should get component with component mapper for better performance") {
        entity[Transform.mapper] shouldBeSameInstanceAs transform
      }
      test("should return null if there is no component") {
        entity[Texture.mapper].shouldBeNull()
      }
    }

    context("get component function") {
      test("should get component by reified type") {
        entity.get<Transform>() shouldBeSameInstanceAs transform
      }
      test("should return null if there is no component") {
        entity.get<Texture>().shouldBeNull()
      }
    }

    context("remove component function") {
      test("should remove a component by reified type") {
        val component = entity.remove<Transform>()

        entity.getComponent(Transform::class.java).shouldBeNull()
        component shouldBe transform
      }
      test("should fail to remove an absent component by reified type") {
        val component = entity.remove<Texture>()

        entity.getComponent(Texture::class.java).shouldBeNull()
        component.shouldBeNull()
      }
    }

    context("has component function") {
      test("should return true if exists") {
        entity.has(Transform.mapper) shouldBe true
      }
      test("should return false if does not exists") {
        entity.has(Texture.mapper) shouldBe false
      }
    }

    context("has not component function") {
      test("should return false if exists") {
        entity.hasNot(Transform.mapper) shouldBe false
      }
      test("should return true if does not exists") {
        entity.hasNot(Texture.mapper) shouldBe true
      }
    }

    context("contains component function") {
      test("should return true if component exists") {
        entity.contains(Transform.mapper) shouldBe true
      }
      test("should return false if component does not exists") {
        entity.contains(Texture.mapper) shouldBe false
      }
      test("should return true as operator if component exists") {
        (Transform.mapper in entity) shouldBe true
      }
      test("should return false as operator if component does not exists") {
        (Texture.mapper !in entity) shouldBe true
      }
    }

    context("plus assignment operator") {
      test("should add a component to an entity") {
        entity.remove<Transform>()

        entity += Transform(1f, 0f)

        (Transform.mapper in entity) shouldBe true
        entity[Transform.mapper]?.x shouldBe 1f
      }

      test("should replace a component of an entity") {
        entity += Transform(1f, 0f)

        entity[Transform.mapper]?.x shouldBe 1f
        entity[Transform.mapper] shouldNotBeSameInstanceAs transform
      }
    }

    context("add component function") {
      test("should add a component with a configuration and return it") {
        val component =
          entity.addComponent<Transform>(engine) {
            x = 3f
          }
        (Transform.mapper in entity) shouldBe true
        entity[Transform.mapper] shouldBe component
        component.x shouldBe 3f
      }
      test("should configure component exactly once") {
        val variable: Int
        entity.addComponent<Transform>(engine) {
          variable = 42
        }
        variable shouldBe 42
      }
    }
  }
})
