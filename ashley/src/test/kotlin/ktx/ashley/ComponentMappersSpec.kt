package ktx.ashley

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.GdxRuntimeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

/** For [Mapper] testing. Must be a top-level class due to the companion object usage. */
class CustomComponent : Component {
  companion object : Mapper<CustomComponent>()
}

/** For [Mapper] testing. Must not be enclosed by another class for test purposes. */
class TopLevelMapper : Mapper<CustomComponent>()

class ComponentMappersSpec : FunSpec({
  context("utilities for component mappers") {
    val entity =
      Entity().apply {
        add(Texture())
      }
    test("should return a component mapper for the provided a reified type") {
      val mapper = mapperFor<Texture>()
      mapper.has(entity) shouldBe true
    }
  }

  context("Mapper abstract class") {
    val entity =
      Entity().apply {
        add(CustomComponent())
      }
    test("should create a component mapper of the enclosing component class") {
      CustomComponent.mapper.has(entity) shouldBe true
    }
    test("should fail to create a component mapper if there is no enclosing class") {
      shouldThrow<GdxRuntimeException> { TopLevelMapper() }.message shouldEndWith
        "defined outside of the corresponding com.badlogic.ashley.core.Component."
    }
    test("should fail to create a component mapper if the enclosing class is not a component") {
      class BrokenMapper : Mapper<Component>()
      shouldThrow<GdxRuntimeException> { BrokenMapper() }.message shouldEndWith "does not implement com.badlogic.ashley.core.Component."
    }
  }
})
