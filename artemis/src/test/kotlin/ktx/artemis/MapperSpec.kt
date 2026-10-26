package ktx.artemis

import com.artemis.ComponentMapper
import com.artemis.World
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapperSpec : FunSpec({
  context("utilities for mappers") {
    val world by lazy { World() }
    val transform by lazy { Transform() }
    val entity by lazy {
      world.entity {
        add(transform)
      }
    }
    val transformMapper by lazy { world.getMapper(Transform::class.java) }
    val textureMapper by lazy { world.getMapper(Texture::class.java) }

    context("contains component function") {
      test("should return true if component exists") {
        transformMapper.contains(entity) shouldBe true
      }
      test("should return false if component does not exists") {
        textureMapper.contains(entity) shouldBe false
      }
      test("should return true with operator if component exists") {
        (entity in transformMapper) shouldBe true
      }
      test("should return false with operator if component does not exists") {
        (entity !in textureMapper) shouldBe true
      }
    }
    context("getMapper function") {
      test("should return a mapper") {
        val positionMapper = world.mapperFor<Position>()
        positionMapper.create(entity)

        (positionMapper.javaClass == ComponentMapper::class.java) shouldBe true
        positionMapper.has(entity) shouldBe true
      }
    }
  }
})
