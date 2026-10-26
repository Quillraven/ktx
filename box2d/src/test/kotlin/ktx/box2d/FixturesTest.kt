package ktx.box2d

import com.badlogic.gdx.physics.box2d.Filter
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests utility extensions related to body fixtures.
 */
class FixturesTest : Box2DTest() {
  init {
    test("should set filter properties of FixtureDef") {
      val fixtureDefinition = FixtureDef()

      fixtureDefinition.filter {
        categoryBits = 1
        maskBits = 2
        groupIndex = 3
      }

      fixtureDefinition.filter.apply {
        categoryBits shouldBe 1.toShort()
        maskBits shouldBe 2.toShort()
        groupIndex shouldBe 3.toShort()
      }
    }

    test("should configure filter exactly once") {
      val fixtureDefinition = FixtureDef()
      val variable: Int

      fixtureDefinition.filter {
        variable = 42
      }

      variable shouldBe 42
    }

    test("should copy filter properties into filter of FixtureDef") {
      val fixtureDefinition = FixtureDef()
      val filter =
        Filter().apply {
          categoryBits = 1
          maskBits = 2
          groupIndex = 3
        }

      fixtureDefinition.filter(filter)

      fixtureDefinition.filter.apply {
        categoryBits shouldBe 1.toShort()
        maskBits shouldBe 2.toShort()
        groupIndex shouldBe 3.toShort()
      }
    }

    test("should replace creation callback") {
      val fixtureDefinition = FixtureDefinition()
      val callback: (Fixture) -> Unit = {}

      fixtureDefinition.onCreate(callback)

      fixtureDefinition.creationCallback shouldBeSameInstanceAs callback
    }
  }
}
