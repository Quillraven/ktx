package ktx.ashley

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.systems.SortedIteratingSystem
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Comparator

class ListenersSpec : FunSpec({
  context("utilities for entity listeners") {
    val engine = Engine()

    test("should invoke an addition entity listener") {
      var invoked = false
      val listener =
        object : EntityAdditionListener {
          override fun entityAdded(entity: Entity) {
            invoked = true
          }
        }
      engine.addEntityListener(listener)
      val entity = engine.entity()
      engine.removeEntity(entity)
      invoked shouldBe true
      engine.removeEntityListener(listener)
    }

    test("should invoke a removal entity listener") {
      var invoked = false
      val listener =
        object : EntityRemovalListener {
          override fun entityRemoved(entity: Entity) {
            invoked = true
          }
        }
      val entity = engine.entity()
      engine.addEntityListener(listener)
      engine.removeEntity(entity)
      invoked shouldBe true
      engine.removeEntityListener(listener)
    }

    test("should invoke an addition entity listener made with a lambda") {
      var invoked = false
      val listener =
        engine.onEntityAdded {
          invoked = true
        }
      val entity = engine.entity()
      invoked shouldBe true
      engine.removeEntity(entity)
      engine.removeEntityListener(listener)
    }

    test("should invoke a removal entity listener made with a lambda") {
      var invoked = false
      val entity = engine.entity()
      val listener =
        engine.onEntityRemoved {
          invoked = true
        }
      engine.removeEntity(entity)
      invoked shouldBe true
      engine.removeEntityListener(listener)
    }

    test("should run listeners bound to an iterating system") {
      var addAssertion = false
      var removeAssertion = false

      val system =
        object : IteratingSystem(allOf(TestComponent::class).get()) {
          private lateinit var additionListener: EntityAdditionListener
          private lateinit var removalListener: EntityRemovalListener

          override fun processEntity(
            entity: Entity,
            deltaTime: Float,
          ) = Unit

          override fun addedToEngine(engine: Engine) {
            super.addedToEngine(engine)

            additionListener =
              onEntityAdded { entity ->
                if (entity[TestComponent.mapper] != null) {
                  addAssertion = true
                }
              }

            removalListener =
              onEntityRemoved { entity ->
                if (entity[TestComponent.mapper] != null) {
                  removeAssertion = true
                }
              }
          }

          override fun removedFromEngine(engine: Engine) {
            super.removedFromEngine(engine)

            engine.removeEntityListener(additionListener)
            engine.removeEntityListener(removalListener)
          }
        }

      engine.addSystem(system)

      engine.entity().apply(engine::removeEntity)
      addAssertion shouldBe false
      removeAssertion shouldBe false

      val entity =
        engine.entity {
          with<TestComponent>()
        }
      engine.removeEntity(entity)
      engine.removeSystem(system)

      addAssertion shouldBe true
      removeAssertion shouldBe true
    }

    test("should run listeners bound to an interval iterating system") {
      var addAssertion = false
      var removeAssertion = false

      val system =
        object : IntervalIteratingSystem(allOf(TestComponent::class).get(), 0f) {
          private lateinit var additionListener: EntityAdditionListener
          private lateinit var removalListener: EntityRemovalListener

          override fun processEntity(entity: Entity) = Unit

          override fun addedToEngine(engine: Engine) {
            super.addedToEngine(engine)

            additionListener =
              onEntityAdded { entity ->
                if (entity[TestComponent.mapper] != null) {
                  addAssertion = true
                }
              }

            removalListener =
              onEntityRemoved { entity ->
                if (entity[TestComponent.mapper] != null) {
                  removeAssertion = true
                }
              }
          }

          override fun removedFromEngine(engine: Engine) {
            super.removedFromEngine(engine)

            engine.removeEntityListener(additionListener)
            engine.removeEntityListener(removalListener)
          }
        }

      engine.addSystem(system)

      engine.entity().apply(engine::removeEntity)
      addAssertion shouldBe false
      removeAssertion shouldBe false

      val entity =
        engine.entity {
          with<TestComponent>()
        }
      engine.removeEntity(entity)
      engine.removeSystem(system)

      addAssertion shouldBe true
      removeAssertion shouldBe true
    }

    test("should run listeners bound to a sorted iterating system") {
      var addAssertion = false
      var removeAssertion = false

      val system =
        object : SortedIteratingSystem(allOf(TestComponent::class).get(), Comparator.comparing(Any::hashCode)) {
          private lateinit var additionListener: EntityAdditionListener
          private lateinit var removalListener: EntityRemovalListener

          override fun processEntity(
            entity: Entity,
            deltaTime: Float,
          ) = Unit

          override fun addedToEngine(engine: Engine) {
            super.addedToEngine(engine)

            additionListener =
              onEntityAdded { entity ->
                if (entity[TestComponent.mapper] != null) {
                  addAssertion = true
                }
              }

            removalListener =
              onEntityRemoved { entity ->
                if (entity[TestComponent.mapper] != null) {
                  removeAssertion = true
                }
              }
          }

          override fun removedFromEngine(engine: Engine) {
            super.removedFromEngine(engine)

            engine.removeEntityListener(additionListener)
            engine.removeEntityListener(removalListener)
          }
        }

      engine.addSystem(system)

      engine.entity().apply(engine::removeEntity)
      addAssertion shouldBe false
      removeAssertion shouldBe false

      val entity =
        engine.entity {
          with<TestComponent>()
        }
      engine.removeEntity(entity)
      engine.removeSystem(system)

      addAssertion shouldBe true
      removeAssertion shouldBe true
    }
  }
})

internal class TestComponent : Component {
  companion object : Mapper<TestComponent>()
}
