package ktx.tiled

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapLayers
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class MapLayerTest : FunSpec({
  val mapLayer =
    MapLayer().apply {
      properties.apply {
        put("active", true)
        put("customProperty", 123)
      }
    }

  test("should retrieve properties from MapLayer") {
    mapLayer.property<Boolean>("active") shouldBe true
    mapLayer.property<Int>("customProperty") shouldBe 123
  }

  test("should retrieve properties from MapLayer with default value") {
    mapLayer.property("active", false) shouldBe true
    mapLayer.property("customProperty", 0) shouldBe 123
    mapLayer.property("x", -1f) shouldBe -1f
  }

  test("should retrieve properties from MapLayer without default value") {
    mapLayer.propertyOrNull<Any>("x").shouldBeNull()
    mapLayer.propertyOrNull<Int>("customProperty") shouldBe 123
  }

  test("should check if property from MapLayer exists") {
    mapLayer.containsProperty("active") shouldBe true
    mapLayer.containsProperty("x") shouldBe false
  }

  test("should not retrieve non-existing property from MapLayer") {
    shouldThrow<MissingPropertyException> {
      mapLayer.property<String>("non-existing")
    }
  }

  test("should return true when MapLayers is empty") {
    val actual = MapLayers()

    actual.isEmpty() shouldBe true
  }

  test("should return false when MapLayers is not empty") {
    val actual = MapLayers()
    actual.add(MapLayer())

    actual.isEmpty() shouldBe false
  }

  test("should return true when MapLayers becomes empty") {
    val actual = MapLayers()
    actual.add(MapLayer())
    actual.remove(0)

    actual.isEmpty() shouldBe true
  }

  test("should return true when MapLayers is not empty") {
    val actual = MapLayers()
    actual.add(MapLayer())

    actual.isNotEmpty() shouldBe true
  }

  test("should return false when MapLayers is empty") {
    val actual = MapLayers()

    actual.isNotEmpty() shouldBe false
  }

  test("should execute action per cell of TiledMapTileLayer") {
    val tileLayer =
      TiledMapTileLayer(2, 2, 32, 32).apply {
        setCell(0, 0, Cell().setTile(StaticTiledMapTile(TextureRegion())))
        setCell(1, 1, Cell().setTile(StaticTiledMapTile(TextureRegion())))
      }

    val visitedCells = mutableSetOf<Pair<Int, Int>>()

    tileLayer.forEachCell { _, cellX, cellY ->
      visitedCells += cellX to cellY
    }

    visitedCells shouldBe setOf(0 to 0, 1 to 1)
  }

  test("should not execute any action for TiledMapTileLayer without cells") {
    val tileLayer = TiledMapTileLayer(2, 2, 32, 32)

    tileLayer.forEachCell { _, _, _ ->
      fail("Test failed.")
    }
  }
})
