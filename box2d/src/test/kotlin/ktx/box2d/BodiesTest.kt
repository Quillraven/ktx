package ktx.box2d

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.Shape
import com.badlogic.gdx.physics.box2d.World
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

// TODO Kotlin Contracts in 1.3 do not support vararg methods such as polygon, chain or loop.
// Once that compiler bug is fixed, their tests with init blocks should be extended to test the contracts.
// https://youtrack.jetbrains.com/issue/KT-30497

/**
 * Tests Box2D bodies utilities and [BodyDefinition] - KTX extension of Box2D BodyDef with [FixtureDefinition] factory
 * methods.
 */
class BodiesTest : Box2DTest() {
  private fun createBody(): Body = World(Vector2.Zero, true).createBody(BodyDef())

  init {
    // BodyDefinition DSL tests:

    test("should construct FixtureDef with a custom shape") {
      val bodyDefinition = BodyDefinition()
      val shape = mock<Shape>()

      val fixtureDef = bodyDefinition.fixture(shape)

      fixtureDef.shape shouldBeSameInstanceAs shape
      fixtureDef.disposeOfShape shouldBe false
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      verify(shape, never()).dispose()
    }

    test("should construct FixtureDef with a custom shape with init block") {
      val bodyDefinition = BodyDefinition()
      val shape = mock<Shape>()
      val variable: Int

      val fixtureDef =
        bodyDefinition.fixture(shape) {
          density = 0.5f
          it shouldBeSameInstanceAs shape
          variable = 42
        }

      fixtureDef.shape shouldBeSameInstanceAs shape
      fixtureDef.disposeOfShape shouldBe false
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      variable shouldBe 42
    }

    test("should construct FixtureDef with a custom shape and mark it for disposing") {
      val bodyDefinition = BodyDefinition()
      val shape = mock<Shape>()

      val fixtureDef = bodyDefinition.fixture(shape, disposeOfShape = true)

      fixtureDef.shape shouldBeSameInstanceAs shape
      fixtureDef.disposeOfShape shouldBe true
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
    }

    test("should construct FixtureDef with a CircleShape") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.circle(radius = 1f, position = Vector2(2f, 3f))

      (fixtureDef.shape is CircleShape) shouldBe true
      val shape = fixtureDef.shape as CircleShape
      shape.radius shouldBe 1f
      shape.position.x shouldBe 2f
      shape.position.y shouldBe 3f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a CircleShape with init block") {
      val bodyDefinition = BodyDefinition()
      val variable: Int

      val fixtureDef =
        bodyDefinition.circle(radius = 1f, position = Vector2(2f, 3f)) {
          density = 0.5f
          variable = 42
        }

      (fixtureDef.shape is CircleShape) shouldBe true
      val shape = fixtureDef.shape as CircleShape
      shape.radius shouldBe 1f
      shape.position.x shouldBe 2f
      shape.position.y shouldBe 3f
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
      variable shouldBe 42
    }

    test("should construct FixtureDef with a PolygonShape set to a box") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef =
        bodyDefinition.box(
          width = 2f,
          height = 2f,
          position = Vector2(1f, 1f),
          angle = 90f * MathUtils.degreesToRadians,
        )

      (fixtureDef.shape is PolygonShape) shouldBe true
      val shape = fixtureDef.shape as PolygonShape
      assertPolygonEquals(
        arrayOf(
          Vector2(2f, 0f),
          Vector2(2f, 2f),
          Vector2(0f, 2f),
          Vector2(0f, 0f),
        ),
        shape,
      )
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a PolygonShape set to a box with init block") {
      val bodyDefinition = BodyDefinition()
      val variable: Int

      val fixtureDef =
        bodyDefinition.box(
          width = 2f,
          height = 2f,
          position = Vector2(1f, 1f),
          angle = 90f * MathUtils.degreesToRadians,
        ) {
          density = 0.5f
          variable = 42
        }

      (fixtureDef.shape is PolygonShape) shouldBe true
      val shape = fixtureDef.shape as PolygonShape
      assertPolygonEquals(
        arrayOf(
          Vector2(2f, 0f),
          Vector2(2f, 2f),
          Vector2(0f, 2f),
          Vector2(0f, 0f),
        ),
        shape,
      )
      fixtureDef.density shouldBe 0.5f
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
      variable shouldBe 42
    }

    test("should construct FixtureDef with a PolygonShape") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.polygon(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f))

      (fixtureDef.shape is PolygonShape) shouldBe true
      val shape = fixtureDef.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a PolygonShape with init block") {
      val bodyDefinition = BodyDefinition()
      val variable: Int

      val fixtureDef =
        bodyDefinition.polygon(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f)) {
          density = 0.5f
          variable = 42
        }

      (fixtureDef.shape is PolygonShape) shouldBe true
      val shape = fixtureDef.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
      variable shouldBe 42
    }

    test("should construct FixtureDef with a PolygonShape with Vector2 points") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.polygon(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f))

      (fixtureDef.shape is PolygonShape) shouldBe true
      val shape = fixtureDef.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a PolygonShape with Vector2 points and init block") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef =
        bodyDefinition.polygon(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)) {
          density = 0.5f
        }

      (fixtureDef.shape is PolygonShape) shouldBe true
      val shape = fixtureDef.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a ChainShape") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.chain(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f))

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a ChainShape with init block") {
      val bodyDefinition = BodyDefinition()
      val variable: Int

      val fixtureDef =
        bodyDefinition.chain(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f)) {
          density = 0.5f
          variable = 42
        }

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
      variable shouldBe 42
    }

    test("should construct FixtureDef with a ChainShape with Vector2 points") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.chain(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f))

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a ChainShape with Vector2 points and init block") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef =
        bodyDefinition.chain(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)) {
          density = 0.5f
        }

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a looped ChainShape") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.loop(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f))

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe true
      // Loop adds another vertex closing the chain:
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a looped ChainShape with init block") {
      val bodyDefinition = BodyDefinition()
      val variable: Int

      val fixtureDef =
        bodyDefinition.loop(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f)) {
          density = 0.5f
          variable = 42
        }

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe true
      // Loop adds another vertex closing the chain:
      assertChainEquals(
        arrayOf(
          Vector2(1f, 1f),
          Vector2(2f, 2f),
          Vector2(1f, 2f),
          Vector2(1f, 1f),
        ),
        shape,
      )
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
      variable shouldBe 42
    }

    test("should construct FixtureDef with a looped ChainShape with Vector2 points") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.loop(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f))

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe true
      // Loop adds another vertex closing the chain:
      assertChainEquals(
        arrayOf(
          Vector2(1f, 1f),
          Vector2(2f, 2f),
          Vector2(1f, 2f),
          Vector2(1f, 1f),
        ),
        shape,
      )
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with a looped ChainShape with Vector2 points and init block") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef =
        bodyDefinition.loop(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)) {
          density = 0.5f
        }

      (fixtureDef.shape is ChainShape) shouldBe true
      val shape = fixtureDef.shape as ChainShape
      shape.isLooped shouldBe true
      // Loop adds another vertex closing the chain:
      assertChainEquals(
        arrayOf(
          Vector2(1f, 1f),
          Vector2(2f, 2f),
          Vector2(1f, 2f),
          Vector2(1f, 1f),
        ),
        shape,
      )
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with an EdgeShape") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.edge(from = Vector2(1f, 1f), to = Vector2(2f, 2f))

      (fixtureDef.shape is EdgeShape) shouldBe true
      val shape = fixtureDef.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 1f), Vector2(2f, 2f), shape)
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with an EdgeShape with init block") {
      val bodyDefinition = BodyDefinition()
      val variable: Int

      val fixtureDef =
        bodyDefinition.edge(from = Vector2(1f, 1f), to = Vector2(2f, 2f)) {
          density = 0.5f
          variable = 42
        }

      (fixtureDef.shape is EdgeShape) shouldBe true
      val shape = fixtureDef.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 1f), Vector2(2f, 2f), shape)
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
      variable shouldBe 42
    }

    test("should construct FixtureDef with an EdgeShape with float points") {
      val bodyDefinition = BodyDefinition()

      val fixtureDef = bodyDefinition.edge(fromX = 1f, fromY = 2f, toX = 3f, toY = 4f)

      (fixtureDef.shape is EdgeShape) shouldBe true
      val shape = fixtureDef.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 2f), Vector2(3f, 4f), shape)
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
    }

    test("should construct FixtureDef with an EdgeShape with float points and init block") {
      val bodyDefinition = BodyDefinition()
      val variable: Int

      val fixtureDef =
        bodyDefinition.edge(fromX = 1f, fromY = 2f, toX = 3f, toY = 4f) {
          density = 0.5f
          variable = 42
        }

      (fixtureDef.shape is EdgeShape) shouldBe true
      val shape = fixtureDef.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 2f), Vector2(3f, 4f), shape)
      fixtureDef.density shouldBe 0.5f
      (fixtureDef in bodyDefinition.fixtureDefinitions) shouldBe true
      fixtureDef.disposeOfShape shouldBe true
      variable shouldBe 42
    }

    test("should replace creation callback") {
      val bodyDefinition = BodyDefinition()
      val callback: (Body) -> Unit = {}

      bodyDefinition.onCreate(callback)

      bodyDefinition.creationCallback shouldBeSameInstanceAs callback
    }

    // Body extension tests:

    test("should construct Fixture with a custom shape") {
      val body = createBody()
      val shape = spy(CircleShape())

      val fixture = body.fixture(shape)

      (fixture.shape is CircleShape) shouldBe true
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      verify(shape, never()).dispose()
    }

    test("should construct Fixture with a custom shape with init block") {
      val body = createBody()
      val shape = spy(CircleShape())
      val variable: Int

      val fixture =
        body.fixture(shape) {
          density = 0.5f
          it shouldBeSameInstanceAs shape
          variable = 42
        }

      (fixture.shape is CircleShape) shouldBe true
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      verify(shape, never()).dispose()
      variable shouldBe 42
    }

    test("should construct Fixture with a custom shape and dispose of it") {
      val body = createBody()
      val shape = spy(CircleShape())

      val fixture = body.fixture(shape, disposeOfShape = true)

      (fixture.shape is CircleShape) shouldBe true
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      verify(shape).dispose()
    }

    test("should construct Fixture with a CircleShape") {
      val body = createBody()

      val fixture = body.circle(radius = 1f, position = Vector2(2f, 3f))

      (fixture.shape is CircleShape) shouldBe true
      val shape = fixture.shape as CircleShape
      shape.radius shouldBe 1f
      shape.position.x shouldBe 2f
      shape.position.y shouldBe 3f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a CircleShape with init block") {
      val body = createBody()
      val variable: Int

      val fixture =
        body.circle(radius = 1f, position = Vector2(2f, 3f)) {
          density = 0.5f
          variable = 42
        }

      (fixture.shape is CircleShape) shouldBe true
      val shape = fixture.shape as CircleShape
      shape.radius shouldBe 1f
      shape.position.x shouldBe 2f
      shape.position.y shouldBe 3f
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      variable shouldBe 42
    }

    test("should construct Fixture with a PolygonShape set to a box") {
      val body = createBody()

      val fixture =
        body.box(
          width = 2f,
          height = 2f,
          position = Vector2(1f, 1f),
          angle = 90f * MathUtils.degreesToRadians,
        )

      (fixture.shape is PolygonShape) shouldBe true
      val shape = fixture.shape as PolygonShape
      assertPolygonEquals(
        arrayOf(
          Vector2(2f, 0f),
          Vector2(2f, 2f),
          Vector2(0f, 2f),
          Vector2(0f, 0f),
        ),
        shape,
      )
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a PolygonShape set to a box with init block") {
      val body = createBody()
      val variable: Int

      val fixture =
        body.box(
          width = 2f,
          height = 2f,
          position = Vector2(1f, 1f),
          angle = 90f * MathUtils.degreesToRadians,
        ) {
          density = 0.5f
          variable = 42
        }

      (fixture.shape is PolygonShape) shouldBe true
      val shape = fixture.shape as PolygonShape
      assertPolygonEquals(
        arrayOf(
          Vector2(2f, 0f),
          Vector2(2f, 2f),
          Vector2(0f, 2f),
          Vector2(0f, 0f),
        ),
        shape,
      )
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      variable shouldBe 42
    }

    test("should construct Fixture with a PolygonShape") {
      val body = createBody()

      val fixture = body.polygon(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f))

      (fixture.shape is PolygonShape) shouldBe true
      val shape = fixture.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a PolygonShape with init block") {
      val body = createBody()
      val variable: Int

      val fixture =
        body.polygon(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f)) {
          density = 0.5f
          variable = 42
        }

      (fixture.shape is PolygonShape) shouldBe true
      val shape = fixture.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      variable shouldBe 42
    }

    test("should construct Fixture with a PolygonShape with Vector2 Points") {
      val body = createBody()

      val fixture = body.polygon(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f))

      (fixture.shape is PolygonShape) shouldBe true
      val shape = fixture.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a PolygonShape with Vector2 points and init block") {
      val body = createBody()

      val fixture =
        body.polygon(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)) {
          density = 0.5f
        }

      (fixture.shape is PolygonShape) shouldBe true
      val shape = fixture.shape as PolygonShape
      // Box2D seems to change vertices order:
      assertPolygonEquals(arrayOf(Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a ChainShape") {
      val body = createBody()

      val fixture = body.chain(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f))

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a ChainShape with init block") {
      val body = createBody()
      val variable: Int

      val fixture =
        body.chain(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f)) {
          density = 0.5f
          variable = 42
        }

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      variable shouldBe 42
    }

    test("should construct Fixture with a ChainShape with Vector2 Points") {
      val body = createBody()

      val fixture = body.chain(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f))

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a ChainShape with Vector2 points and init block") {
      val body = createBody()

      val fixture =
        body.chain(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)) {
          density = 0.5f
        }

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      shape.isLooped shouldBe false
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a looped ChainShape") {
      val body = createBody()

      val fixture = body.loop(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f))

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      // Loop adds another vertex closing the chain:
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a looped ChainShape with init block") {
      val body = createBody()

      val fixture =
        body.loop(vertices = floatArrayOf(1f, 1f, 2f, 2f, 1f, 2f)) {
          density = 0.5f
        }

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      // Loop adds another vertex closing the chain:
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a looped ChainShape with Vector2 points") {
      val body = createBody()

      val fixture = body.loop(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f))

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      // Loop adds another vertex closing the chain:
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with a looped ChainShape with Vector2 points and init block") {
      val body = createBody()

      val fixture =
        body.loop(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f)) {
          density = 0.5f
        }

      (fixture.shape is ChainShape) shouldBe true
      val shape = fixture.shape as ChainShape
      // Loop adds another vertex closing the chain:
      assertChainEquals(arrayOf(Vector2(1f, 1f), Vector2(2f, 2f), Vector2(1f, 2f), Vector2(1f, 1f)), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with an EdgeShape") {
      val body = createBody()

      val fixture = body.edge(from = Vector2(1f, 1f), to = Vector2(2f, 2f))

      (fixture.shape is EdgeShape) shouldBe true
      val shape = fixture.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 1f), Vector2(2f, 2f), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with an EdgeShape with init block") {
      val body = createBody()
      val variable: Int

      val fixture =
        body.edge(from = Vector2(1f, 1f), to = Vector2(2f, 2f)) {
          density = 0.5f
          variable = 42
        }

      (fixture.shape is EdgeShape) shouldBe true
      val shape = fixture.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 1f), Vector2(2f, 2f), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      variable shouldBe 42
    }

    test("should construct Fixture with an EdgeShape with float points") {
      val body = createBody()

      val fixture = body.edge(fromX = 1f, fromY = 2f, toX = 3f, toY = 4f)

      (fixture.shape is EdgeShape) shouldBe true
      val shape = fixture.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 2f), Vector2(3f, 4f), shape)
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
    }

    test("should construct Fixture with an EdgeShape with float points and init block") {
      val body = createBody()
      val variable: Int

      val fixture =
        body.edge(fromX = 1f, fromY = 2f, toX = 3f, toY = 4f) {
          density = 0.5f
          variable = 42
        }

      (fixture.shape is EdgeShape) shouldBe true
      val shape = fixture.shape as EdgeShape
      assertEdgeEquals(Vector2(1f, 2f), Vector2(3f, 4f), shape)
      fixture.density shouldBe 0.5f
      fixture.body shouldBeSameInstanceAs body
      (fixture in body.fixtureList) shouldBe true
      variable shouldBe 42
    }

    test("should invoke fixture creation callback") {
      val body = createBody()
      var callbackParameter: Fixture? = null

      val fixture =
        body.circle {
          onCreate {
            callbackParameter = it
          }
        }

      callbackParameter shouldBeSameInstanceAs fixture
    }
  }
}
