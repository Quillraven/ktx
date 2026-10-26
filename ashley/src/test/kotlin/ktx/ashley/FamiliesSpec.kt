package ktx.ashley

import com.badlogic.ashley.core.Entity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FamiliesSpec : FunSpec({
  context("utilities for component families") {
    val textureEntity =
      Entity().apply {
        add(Texture())
      }

    val rigidBodyEntity =
      Entity().apply {
        add(RigidBody())
      }

    val textureAndTransformEntity =
      Entity().apply {
        add(Texture())
        add(Transform())
      }

    val allComponentsEntity =
      Entity().apply {
        add(Texture())
        add(Transform())
        add(RigidBody())
      }

    test("should create a family that matches one of component") {
      val family = oneOf(Texture::class, Transform::class).get()
      family.matches(textureEntity) shouldBe true
      family.matches(rigidBodyEntity) shouldBe false
    }

    test("should create a family that matches all components") {
      val family = allOf(Texture::class, Transform::class).get()
      family.matches(textureEntity) shouldBe false
      family.matches(textureAndTransformEntity) shouldBe true
    }

    test("should create a family that matches any excluded components") {
      exclude(Transform::class).get().matches(textureEntity) shouldBe true
      exclude(Texture::class).get().matches(textureEntity) shouldBe false
    }

    context("composite families") {
      test("should build a family chained with matching any of one component") {
        val family = exclude(Transform::class).oneOf(Texture::class, RigidBody::class)
        family.get().matches(textureEntity) shouldBe true
        family.get().matches(textureAndTransformEntity) shouldBe false
      }

      test("should build a family chained with matching all components") {
        val family = exclude(RigidBody::class).allOf(Texture::class, Transform::class)
        family.get().matches(allComponentsEntity) shouldBe false
        family.get().matches(textureAndTransformEntity) shouldBe true
      }

      test("should build a family chained with excluding components") {
        val family = oneOf(RigidBody::class).exclude(Texture::class, Transform::class)
        family.get().matches(allComponentsEntity) shouldBe false
        family.get().matches(rigidBodyEntity) shouldBe true
      }
    }
  }
})
