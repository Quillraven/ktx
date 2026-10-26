package ktx.artemis

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class WorldSpec : FunSpec({
  context("utilities for world") {
    val worldConfig by lazy {
      WorldConfigurationBuilder().build()
    }

    context("getSystem function") {
      test("should add a system and return it") {
        val system = TestSystem()
        worldConfig.setSystem(system)
        val world = World(worldConfig)

        world.getSystem<TestSystem>() shouldBe system
      }
      test("should add a system and return it with operator function") {
        val system = TestSystem()
        worldConfig.setSystem(system)
        val world = World(worldConfig)

        world[TestSystem::class] shouldBe system
      }
      test("should throw an exception if the system doesn't exist in the world") {
        shouldThrow<MissingBaseSystemException> {
          val world = World(worldConfig)

          world.getSystem<TestSystem>()
        }
      }
      test("should return null if the system doesn't exist in the world") {
        val world = World(worldConfig)

        world[TestSystem::class].shouldBeNull()
      }
    }
  }
})
