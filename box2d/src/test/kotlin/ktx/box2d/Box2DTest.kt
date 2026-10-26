package ktx.box2d

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.utils.GdxNativesLoader
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * Initiates Box2D native library. Provides comparison methods for common Box2D data objects that cannot be easily
 * compared with standard `equals`.
 */
abstract class Box2DTest : FunSpec() {
  protected val floatTolerance = 0.0001f

  protected fun assertChainEquals(
    vertices: Array<Vector2>,
    shape: ChainShape,
  ) {
    withClue("${vertices.size} vertices expected, ${shape.vertexCount} found instead.") { shape.vertexCount shouldBe vertices.size }
    val vertex = Vector2()
    vertices.forEachIndexed { index, expected ->
      shape.getVertex(index, vertex)
      val errorMessage = "Vertex at $index should equal $expected, $vertex found instead."
      withClue(errorMessage) { vertex.x shouldBe (expected.x plusOrMinus floatTolerance) }
      withClue(errorMessage) { vertex.y shouldBe (expected.y plusOrMinus floatTolerance) }
    }
  }

  protected fun assertPolygonEquals(
    vertices: Array<Vector2>,
    shape: PolygonShape,
  ) {
    withClue("${vertices.size} vertices expected, ${shape.vertexCount} found instead.") { shape.vertexCount shouldBe vertices.size }
    val vertex = Vector2()
    vertices.forEachIndexed { index, expected ->
      shape.getVertex(index, vertex)
      val errorMessage = "Vertex at $index should equal $expected, $vertex found instead."
      withClue(errorMessage) { vertex.x shouldBe (expected.x plusOrMinus floatTolerance) }
      withClue(errorMessage) { vertex.y shouldBe (expected.y plusOrMinus floatTolerance) }
    }
  }

  protected fun assertEdgeEquals(
    from: Vector2,
    to: Vector2,
    edgeShape: EdgeShape,
  ) {
    val vertex = Vector2()
    edgeShape.getVertex1(vertex)
    vertex shouldBe from
    edgeShape.getVertex2(vertex)
    vertex shouldBe to
  }

  init {
    beforeSpec {
      GdxNativesLoader.load()
      Box2D.init()
    }
  }
}
