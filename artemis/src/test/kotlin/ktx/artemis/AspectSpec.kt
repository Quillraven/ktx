package ktx.artemis

import com.artemis.World
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AspectSpec : FunSpec({
  context("utilities for aspects") {
    val world by lazy { World() }
    val textureEntity by lazy {
      world.createEntity().apply {
        edit().add(Texture())
      }
    }
    val rigidBodyEntity by lazy {
      world.createEntity().apply {
        edit().add(RigidBody())
      }
    }

    val textureAndTransformEntity by lazy {
      world.createEntity().apply {
        edit()
          .add(Texture())
          .add(Transform())
      }
    }

    val threeComponentsEntity by lazy {
      world.createEntity().apply {
        edit()
          .add(Texture())
          .add(Transform())
          .add(RigidBody())
      }
    }

    test("should create an aspect that matches one of the components") {
      val aspect = oneOf(Texture::class, Transform::class).build(world)

      aspect.isInterested(textureEntity) shouldBe true
      aspect.isInterested(textureAndTransformEntity) shouldBe true
      aspect.isInterested(rigidBodyEntity) shouldBe false
    }
    test("should create an aspect that matches all the components") {
      val aspect = allOf(Texture::class, Transform::class).build(world)

      aspect.isInterested(textureEntity) shouldBe false
      aspect.isInterested(textureAndTransformEntity) shouldBe true
    }
    test("should create an aspect that excludes components") {
      exclude(Transform::class).build(world).isInterested(textureEntity) shouldBe true
      exclude(Texture::class).build(world).isInterested(textureEntity) shouldBe false
    }

    test("should create an aspect that matches all, one of and excludes components") {
      val aspect =
        exclude(Remove::class)
          .oneOf(Position::class, RigidBody::class)
          .allOf(Texture::class, Transform::class)
          .exclude(Dead::class)
          .build(world)

      aspect.isInterested(rigidBodyEntity) shouldBe false
      aspect.isInterested(threeComponentsEntity) shouldBe true

      threeComponentsEntity.edit().add(Dead())

      aspect.isInterested(threeComponentsEntity) shouldBe false
    }
  }
})
