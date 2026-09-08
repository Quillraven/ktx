package ktx.tiled

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapLayers
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MapLayerTest {
  private val mapLayer =
    MapLayer().apply {
      properties.apply {
        put("active", true)
        put("customProperty", 123)
      }
    }

  @Test
  fun `should retrieve properties from MapLayer`() {
    assertEquals(true, mapLayer.property<Boolean>("active"))
    assertEquals(123, mapLayer.property<Int>("customProperty"))
  }

  @Test
  fun `should retrieve properties from MapLayer with default value`() {
    assertEquals(true, mapLayer.property("active", false))
    assertEquals(123, mapLayer.property("customProperty", 0))
    assertEquals(-1f, mapLayer.property("x", -1f))
  }

  @Test
  fun `should retrieve properties from MapLayer without default value`() {
    assertNull(mapLayer.propertyOrNull("x"))
    assertEquals(123, mapLayer.propertyOrNull<Int>("customProperty"))
  }

  @Test
  fun `should check if property from MapLayer exists`() {
    assertTrue(mapLayer.containsProperty("active"))
    assertFalse(mapLayer.containsProperty("x"))
  }

  @Test(expected = MissingPropertyException::class)
  fun `should not retrieve non-existing property from MapLayer`() {
    mapLayer.property<String>("non-existing")
  }

  @Test
  fun `should return true when MapLayers is empty`() {
    val actual = MapLayers()

    assertTrue(actual.isEmpty())
  }

  @Test
  fun `should return false when MapLayers is not empty`() {
    val actual = MapLayers()
    actual.add(MapLayer())

    assertFalse(actual.isEmpty())
  }

  @Test
  fun `should return true when MapLayers becomes empty`() {
    val actual = MapLayers()
    actual.add(MapLayer())
    actual.remove(0)

    assertTrue(actual.isEmpty())
  }

  @Test
  fun `should return true when MapLayers is not empty`() {
    val actual = MapLayers()
    actual.add(MapLayer())

    assertTrue(actual.isNotEmpty())
  }

  @Test
  fun `should return false when MapLayers is empty`() {
    val actual = MapLayers()

    assertFalse(actual.isNotEmpty())
  }

  @Test
  fun `should execute action per cell of TiledMapTileLayer`() {
    val tileLayer =
      TiledMapTileLayer(2, 2, 32, 32).apply {
        setCell(0, 0, Cell().setTile(StaticTiledMapTile(TextureRegion())))
        setCell(1, 1, Cell().setTile(StaticTiledMapTile(TextureRegion())))
      }

    val visitedCells = mutableSetOf<Pair<Int, Int>>()

    tileLayer.forEachCell { _, cellX, cellY ->
      visitedCells += cellX to cellY
    }

    assertEquals(setOf(0 to 0, 1 to 1), visitedCells)
  }

  @Test
  fun `should not execute any action for TiledMapTileLayer without cells`() {
    val tileLayer = TiledMapTileLayer(2, 2, 32, 32)

    tileLayer.forEachCell { _, _, _ ->
      fail()
    }
  }
}
