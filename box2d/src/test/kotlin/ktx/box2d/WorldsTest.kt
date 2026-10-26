package ktx.box2d

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.KinematicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

/**
 * Tests [World] utilities and extension methods.
 */
class WorldsTest : Box2DTest() {
  init {
    test("should roughly match Earth's gravity") {
      earthGravity.x shouldBe 0f
      earthGravity.y shouldBe -9.8f
    }

    test("should create World with default gravity") {
      val world = createWorld()

      world.gravity shouldBe Vector2(0f, 0f)
      world.dispose()
    }

    test("should create World with custom gravity") {
      val world = createWorld(gravity = Vector2(1f, 1f), allowSleep = false)

      world.gravity shouldBe Vector2(1f, 1f)
      world.dispose()
    }

    test("should construct a Body") {
      val world = createWorld()

      val body = world.body()

      body.shouldNotBeNull()
      body.world shouldBeSameInstanceAs world
      body.type shouldBe StaticBody
      world.dispose()
    }

    test("should construct a Body of chosen BodyType") {
      val world = createWorld()

      val body = world.body(type = KinematicBody)

      body.shouldNotBeNull()
      body.world shouldBeSameInstanceAs world
      body.type shouldBe KinematicBody
      world.dispose()
    }

    test("should construct a customized Body with a Fixture") {
      val world = createWorld()

      val body =
        world.body {
          type = DynamicBody
          circle(radius = 2f) {
            density = 0.5f
          }
        }

      body.world shouldBeSameInstanceAs world
      body.type shouldBe DynamicBody
      body.fixtureList.size shouldBe 1
      val fixture = body.fixtureList[0]
      fixture.density shouldBe 0.5f
      (fixture.shape is CircleShape) shouldBe true
      fixture.shape.radius shouldBe 2f
      world.dispose()
    }

    test("should construct a customized Body with multiple Fixture instances") {
      val world = createWorld()

      val body =
        world.body {
          type = DynamicBody
          circle(radius = 2f) {
            density = 0.5f
          }
          edge(from = Vector2(1f, 1f), to = Vector2(2f, 2f)) {
            density = 0.75f
          }
        }

      body.world shouldBeSameInstanceAs world
      body.type shouldBe DynamicBody
      body.fixtureList.size shouldBe 2
      val circle = body.fixtureList[0]
      circle.density shouldBe 0.5f
      (circle.shape is CircleShape) shouldBe true
      circle.shape.radius shouldBe 2f
      val edge = body.fixtureList[1]
      edge.density shouldBe 0.75f
      (edge.shape is EdgeShape) shouldBe true
      assertEdgeEquals(Vector2(1f, 1f), Vector2(2f, 2f), edge.shape as EdgeShape)
      world.dispose()
    }

    test("should construct Body exactly once") {
      val world = createWorld()
      val fixture: FixtureDefinition
      val variable: Int

      world.body {
        fixture = circle()
        variable = 42
      }

      fixture.shouldNotBeNull()
      variable shouldBe 42
    }

    test("should dispose of Shape instances during Body construction") {
      val world = createWorld()
      val reusable = spy(CircleShape())
      val disposable = spy(CircleShape())

      world.body {
        fixture(reusable, disposeOfShape = false)
        fixture(disposable, disposeOfShape = true)
      }

      verify(reusable, never()).dispose()
      verify(disposable).dispose()
    }

    test("should set custom user data of bodies") {
      val world = createWorld()
      val data = Any()

      val body =
        world.body {
          userData = data
        }

      body.userData shouldBeSameInstanceAs data
      world.dispose()
    }

    test("should set custom user data of fixtures") {
      val world = createWorld()
      val circleData = Any()
      val boxData = Any()

      val body =
        world.body {
          circle {
            userData = circleData
          }
          box {
            userData = boxData
          }
        }

      val circle = body.fixtureList[0]
      circle.userData shouldBeSameInstanceAs circleData
      val box = body.fixtureList[1]
      box.userData shouldBeSameInstanceAs boxData
      world.dispose()
    }

    test("should invoke body creation callback") {
      val world = createWorld()
      var callbackParameter: Body? = null

      val body =
        world.body {
          onCreate {
            callbackParameter = it
          }
        }

      callbackParameter shouldBeSameInstanceAs body
      world.dispose()
    }

    test("should invoke fixture creation callback") {
      val world = createWorld()
      var callbackParameter: Fixture? = null

      val body =
        world.body {
          circle {
            onCreate {
              callbackParameter = it
            }
          }
        }

      callbackParameter shouldBeSameInstanceAs body.fixtureList[0]
      world.dispose()
    }

    test("should ray-cast between two vectors") {
      val world = createWorld()
      val expectedEdge = world.body().edge(from = Vector2.Zero, to = Vector2(0f, 2f))
      var called = false

      world.rayCast(Vector2(-1f, 1f), Vector2(1f, 1f)) { fixture, point, normal, fraction ->
        called = true
        fixture shouldBeSameInstanceAs expectedEdge
        point shouldBe Vector2(0f, 1f)
        normal shouldBe Vector2(-1f, 0f)
        fraction shouldBe 0.5f
        RayCast.TERMINATE
      }

      called shouldBe true
      world.dispose()
    }

    test("should ray-cast between two coordinates") {
      val world = createWorld()
      val expectedEdge = world.body().edge(from = Vector2.Zero, to = Vector2(0f, 2f))
      var called = false

      world.rayCast(startX = -1f, startY = 1f, endX = 1f, endY = 1f) { fixture, point, normal, fraction ->
        called = true
        fixture shouldBeSameInstanceAs expectedEdge
        point shouldBe Vector2(0f, 1f)
        normal shouldBe Vector2(-1f, 0f)
        fraction shouldBe 0.5f
        RayCast.TERMINATE
      }

      called shouldBe true
      world.dispose()
    }

    test("should provide constants for ray-cast behavior") {
      val tolerance = 0f

      RayCast.CONTINUE shouldBe (1f plusOrMinus tolerance)
      RayCast.TERMINATE shouldBe (0f plusOrMinus tolerance)
      RayCast.IGNORE shouldBe (-1f plusOrMinus tolerance)
    }

    test("should query for overlapping fixtures with AABB and stop") {
      val world = createWorld()
      val matchingEdge1 = world.body().edge(from = Vector2.Zero, to = Vector2(0f, 2f))
      val matchingEdge2 = world.body().edge(from = Vector2(1f, 0f), to = Vector2(1f, 2f))
      val matchedFixtures = mutableSetOf<Fixture>()

      world.query(lowerX = -1f, lowerY = 1f, upperX = 1f, upperY = 1f) { fixture ->
        matchedFixtures += fixture
        Query.STOP
      }

      matchedFixtures.size shouldBe 1
      (matchedFixtures.contains(matchingEdge1) || matchedFixtures.contains(matchingEdge2)) shouldBe true
      world.dispose()
    }

    test("should query for overlapping fixtures with AABB and continue") {
      val world = createWorld()
      val matchingEdge1 = world.body().edge(from = Vector2.Zero, to = Vector2(0f, 2f))
      val matchingEdge2 = world.body().edge(from = Vector2(1f, 0f), to = Vector2(1f, 2f))
      val matchedFixtures = mutableSetOf<Fixture>()

      world.query(lowerX = -1f, lowerY = 1f, upperX = 1f, upperY = 1f) { fixture ->
        matchedFixtures += fixture
        Query.CONTINUE
      }

      matchedFixtures shouldBe setOf(matchingEdge1, matchingEdge2)
      world.dispose()
    }

    test("should query world and not callback if there are no fixtures overlapping the AABB") {
      val world = createWorld()
      world.body().edge(from = Vector2.Zero, to = Vector2(0f, 2f))
      world.body().edge(from = Vector2(1f, 0f), to = Vector2(1f, 2f))
      val matchedFixtures = mutableSetOf<Fixture>()

      world.query(lowerX = -2f, lowerY = 1f, upperX = -1f, upperY = 1f) { fixture ->
        matchedFixtures += fixture
        Query.CONTINUE
      }

      matchedFixtures.isEmpty() shouldBe true
      world.dispose()
    }
  }
}
