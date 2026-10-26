package ktx.tiled

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer
import com.badlogic.gdx.maps.tiled.renderers.beginInternal
import com.badlogic.gdx.maps.tiled.renderers.endInternal
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class TiledMapRendererTest : FunSpec({
  test("should call beginRender and endRender without setView") {
    val renderer = mock<BatchTiledMapRenderer>()

    renderer.use {
      verify(renderer).beginInternal()
      it shouldBeSameInstanceAs renderer
      verify(renderer, never()).endInternal()
    }
    verify(renderer).endInternal()
    verify(renderer, never()).setView(any())
  }

  test("should call beginRender and endRender with camera setView") {
    val renderer = mock<BatchTiledMapRenderer>()
    val camera = OrthographicCamera()

    renderer.use(camera) {
      verify(renderer).beginInternal()
      verify(renderer).setView(camera)
      it shouldBeSameInstanceAs renderer
      verify(renderer, never()).endInternal()
    }
    verify(renderer).endInternal()
  }

  test("should call beginRender and endRender with map boundary setView") {
    val renderer = mock<BatchTiledMapRenderer>()
    val mat4 = Matrix4()
    val mapBoundary = Rectangle(1f, 2f, 3f, 4f)

    renderer.use(mat4, mapBoundary) {
      verify(renderer).beginInternal()
      verify(renderer).setView(mat4, mapBoundary.x, mapBoundary.y, mapBoundary.width, mapBoundary.height)
      it shouldBeSameInstanceAs renderer
      verify(renderer, never()).endInternal()
    }
    verify(renderer).endInternal()
  }

  test("should call beginRender and endRender with map boundary rectangle setView") {
    val renderer = mock<BatchTiledMapRenderer>()
    val mat4 = Matrix4()
    val mapBoundary = Rectangle(1f, 2f, 3f, 4f)

    renderer.use(mat4, mapBoundary) {
      verify(renderer).beginInternal()
      verify(renderer).setView(mat4, mapBoundary.x, mapBoundary.y, mapBoundary.width, mapBoundary.height)
      it shouldBeSameInstanceAs renderer
      verify(renderer, never()).endInternal()
    }
    verify(renderer).endInternal()
  }
})
