package ktx.artemis

import com.artemis.ArchetypeBuilder
import com.artemis.World
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ArchetypeSpec : FunSpec({
  context("utilities for archetypes") {
    val world by lazy { World() }
    val archetypeBuilder by lazy { ArchetypeBuilder() }
    val transformMapper by lazy { world.getMapper(Transform::class.java) }
    val textureMapper by lazy { world.getMapper(Texture::class.java) }
    val positionMapper by lazy { world.getMapper(Position::class.java) }

    context("add component function") {
      test("should add a component") {
        val archetype =
          archetypeBuilder
            .add<Transform>()
            .build(world)

        val entity = world.entity(archetype)

        transformMapper.has(entity) shouldBe true
      }
      test("should add multiple components") {
        val archetype =
          archetypeBuilder
            .add(
              Transform::class,
              Texture::class,
            ).build(world)

        val entity = world.entity(archetype)

        transformMapper.has(entity) shouldBe true
        textureMapper.has(entity) shouldBe true
      }
    }

    context("remove component function") {
      test("should remove a component") {
        val archetype =
          archetypeBuilder
            .add(
              Transform::class,
              Texture::class,
            ).remove<Texture>()
            .build(world)

        val entity = world.entity(archetype)

        transformMapper.has(entity) shouldBe true
        !textureMapper.has(entity) shouldBe true
      }
      test("should remove multiple components") {
        val archetype =
          archetypeBuilder
            .add(
              Position::class,
              Transform::class,
              Texture::class,
            ).remove(
              Transform::class,
              Texture::class,
            ).build(world)

        val entity = world.entity(archetype)
        positionMapper.has(entity) shouldBe true
        !transformMapper.has(entity) shouldBe true
        !textureMapper.has(entity) shouldBe true
      }
    }
  }
})
