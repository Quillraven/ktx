package ktx.artemis

import com.artemis.ArchetypeBuilder
import com.artemis.World
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EntitySpec : FunSpec({
  context("utilities for entities") {
    val transform by lazy { Transform() }
    val world by lazy { World() }
    val entity by lazy {
      world.entity {
        add(transform)
      }
    }
    val entityEdit by lazy { world.edit(entity) }
    val transformMapper by lazy { world.getMapper(Transform::class.java) }
    val textureMapper by lazy { world.getMapper(Texture::class.java) }
    val archetype by lazy {
      ArchetypeBuilder()
        .add(Transform::class.java)
        .build(world)
    }

    context("create entity function") {
      test("should add an entity to the World") {
        val entityId =
          world.entity {
            with<Transform> {
              x = 2f
            }
          }

        (transformMapper.get(entityId).x == 2f) shouldBe true
      }
      test("should add an entity with an archetype to the world") {
        val entityId = world.entity(archetype)

        (transformMapper.get(entityId).x == 0f) shouldBe true
      }
    }

    context("edit entity function") {
      test("should add a component to an entity") {
        textureMapper.remove(entity)

        world.edit(entity) {
          with<Texture>()
        }

        textureMapper.has(entity) shouldBe true
      }
      test("should replace a component of an entity") {
        world.entity()

        world.edit(entity) {
          with<Transform> {
            x = 2f
          }
        }

        transformMapper.has(entity) shouldBe true
        transformMapper.get(entity).x shouldBe 2f
      }
      test("should configure component exactly once") {
        val variable: Int
        entityEdit.with<Transform> {
          variable = 42
        }
        variable shouldBe 42
      }
    }

    context("plus assignment operator") {
      test("should add a component to an entity") {
        val entityId = entityEdit.entityId
        transformMapper.remove(entityId)

        entityEdit += Transform(1f, 0f)

        transformMapper.has(entityId) shouldBe true
        transformMapper.get(entityId).x shouldBe 1f
      }

      test("should replace a component of an entity") {
        val entityId = entityEdit.entityId

        transformMapper.remove(entityId)

        entityEdit += Transform(1f, 0f)

        transformMapper.has(entityId) shouldBe true
        transformMapper.get(entityId).x shouldBe 1f
      }
    }

    context("remove component function") {
      test("should remove component") {
        transformMapper.has(entity) shouldBe true

        world.edit(entity) {
          remove<Transform>()
        }
        !transformMapper.has(entity) shouldBe true
      }
    }
  }
})
