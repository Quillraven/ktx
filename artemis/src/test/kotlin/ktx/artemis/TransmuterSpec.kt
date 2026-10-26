package ktx.artemis

import com.artemis.EntityTransmuterFactory
import com.artemis.World
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TransmuterSpec : FunSpec({
  context("utilities for transmuters") {
    val world by lazy { World() }
    val transmuterFactory by lazy { EntityTransmuterFactory(world) }
    val transformMapper by lazy { world.getMapper(Transform::class.java) }
    val textureMapper by lazy { world.getMapper(Texture::class.java) }
    val entity by lazy {
      world.entity {
        add(Transform())
      }
    }

    context("add component function") {
      test("should add a component to an entity") {
        val transmuter =
          transmuterFactory
            .add<Texture>()
            .build()

        transmuter.transmute(entity)

        textureMapper.has(entity) shouldBe true
      }
    }
    context("remove component function") {
      test("should remove a component from an entity") {
        val transmuter =
          transmuterFactory
            .add<Texture>()
            .remove<Transform>()
            .build()

        transmuter.transmute(entity)

        textureMapper.has(entity) shouldBe true
        !transformMapper.has(entity) shouldBe true
      }
    }
  }
})
