package ktx.ashley

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs

class EnginesSpec : FunSpec({
  context("utilities for engines") {
    val engine by lazy {
      PooledEngine()
    }
    context("creating a component") {
      val component = engine.create<Transform>()
      test("should create a pooled component with reified type") {
        component.x shouldBe 0f
        component.y shouldBe 0f
      }
    }
    context("creating a component without a no-arg constructor") {
      @Suppress("UNUSED_PARAMETER")
      class MissingNoArgConstructorComponent(
        body: String,
      ) : Component

      test("should throw an exception if the non-pooled engine was unable to create the component") {
        val nonPooledEngine = Engine()
        shouldThrow<CreateComponentException> {
          nonPooledEngine.create<MissingNoArgConstructorComponent>()
        }
      }

      test("should throw an exception if the pooled engine was unable to create the component") {
        shouldThrow<CreateComponentException> {
          engine.create<MissingNoArgConstructorComponent>()
        }
      }
    }
    context("creating a corrupted component that throws an exception") {
      class CorruptedComponent : Component {
        init {
          throw IllegalStateException()
        }
      }
      test("should throw an exception if the non-pooled engine was unable to create the corrupted component") {
        val nonPooledEngine = Engine()
        shouldThrow<CreateComponentException> {
          nonPooledEngine.create<CorruptedComponent>()
        }.let { ex ->
          generateSequence(ex.cause) { it.cause }.last().shouldBeInstanceOf<IllegalStateException>()
        }
      }

      test("should throw an exception if the pooled engine was unable to create the corrupted component") {
        shouldThrow<CreateComponentException> {
          engine.create<CorruptedComponent>()
        }.let { ex ->
          generateSequence(ex.cause) { it.cause }.last().shouldBeInstanceOf<IllegalStateException>()
        }
      }
    }
    context("creating a component with configuration") {
      val component =
        engine.create<Transform> {
          x = 1f
          y = 2f
        }
      test("should create a pooled component with reified type") {
        component.x shouldBe 1f
        component.y shouldBe 2f
      }
    }
    context("multiple entity creation DSL") {
      test("should add an entity") {
        engine.add {
          entity {}
        }
        engine.entities.size() shouldBe 1
      }

      test("should add multiple entities") {
        engine.add {
          entity {}
          entity {}
        }
        engine.entities.size() shouldBe 2
      }
    }

    context("single entity creation DSL") {
      test("should add an entity and return it") {
        val entity = engine.entity()
        engine.entities.size() shouldBe 1
        engine.entities[0] shouldBe entity
      }
      test("should add an entity with a component with default configuration") {
        engine.entity {
          with<Transform>()
        }
        val transformEntities = engine.getEntitiesFor(oneOf(Transform::class).get())
        val component = transformEntities.single().getComponent(Transform::class.java)
        component.x shouldBe 0f
        component.y shouldBe 0f
      }
      test("should add an entity with a component with configuration") {
        engine.entity {
          with<Transform> {
            x = 1f
            y = 2f
          }
        }
        val transformEntities = engine.getEntitiesFor(oneOf(Transform::class).get())
        val component = transformEntities.single().getComponent(Transform::class.java)
        component.x shouldBe 1f
        component.y shouldBe 2f
      }
      test("should add multiple different components") {
        engine.entity {
          with<Transform>()
          with<Texture>()
        }
        val transformEntities = engine.getEntitiesFor(allOf(Transform::class, Texture::class).get())
        transformEntities.size() shouldBe 1

        val entity = transformEntities.single()
        entity.getComponent(Transform::class.java).shouldNotBeNull()
        entity.getComponent(Texture::class.java).shouldNotBeNull()
      }
    }

    context("contracts API") {
      test("should create entity exactly once") {
        val variable: Int
        engine.entity {
          with<Transform> {
            variable = 42
          }
        }
        variable shouldBe 42
      }
      test("should create component exactly once") {
        val variable: Int
        engine.create<Transform> {
          variable = 42
        }
        variable shouldBe 42
      }
      test("should add entities exactly once") {
        val variable: Int
        engine.add {
          variable = 42
        }
        variable shouldBe 42
      }
      test("should configure entities exactly once") {
        val variable: Int
        engine.configureEntity(Entity()) {
          variable = 42
        }
        variable shouldBe 42
      }
    }

    context("entity configuration with configureEntity") {
      test("should capture entity and engine") {
        val assignedEngine: Engine
        val assignedEntity: Entity
        val entity = Entity()

        engine.configureEntity(entity) {
          assignedEngine = this.engine
          assignedEntity = this.entity
        }

        assignedEngine shouldBeSameInstanceAs engine
        assignedEntity shouldBeSameInstanceAs entity
      }
      test("should add a component with configuration") {
        val entity = Entity()

        engine.configureEntity(entity) {
          with<Transform> {
            x = 1f
            y = 2f
          }
        }

        val component = entity.getComponent(Transform::class.java)
        component.x shouldBe 1f
        component.y shouldBe 2f
      }
    }

    context("getSystem function") {
      test("should add a system and return it") {
        val system = TestSystem()
        engine.addSystem(system)
        engine.getSystem<TestSystem>() shouldBe system
      }
      test("should add a system and return it as operator") {
        val system = TestSystem()
        engine.addSystem(system)
        engine[TestSystem::class] shouldBe system
      }
      test("should throw an exception if the system is missing in the engine") {
        shouldThrow<MissingEntitySystemException> {
          engine.getSystem<TestSystem>()
        }
      }
      test("should return null if the system is missing in the engine") {
        engine[TestSystem::class].shouldBeNull()
      }
    }
  }
})
