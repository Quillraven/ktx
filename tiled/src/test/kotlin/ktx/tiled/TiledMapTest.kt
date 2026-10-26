package ktx.tiled

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.TiledMapTileSet
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TiledMapTest : FunSpec({
  val tiledMap =
    TiledMap().apply {
      properties.apply {
        put("width", 16)
        put("height", 8)
        put("tilewidth", 32)
        put("tileheight", 32)
        put("backgroundcolor", "#ffffff")
        put("orientation", "orthogonal")
        put("hexsidelength", 0)
        put("staggeraxis", "Y")
        put("staggerindex", "Odd")
      }
      layers.add(
        MapLayer().apply {
          name = "layer-1"
          objects.apply {
            add(MapObject())
            add(MapObject())
            add(MapObject())
          }
        },
      )
      layers.add(
        MapLayer().apply {
          name = "layer-2"
        },
      )
      tileSets.addTileSet(
        TiledMapTileSet().apply {
          name = "tileset-1"
          properties.put("firstgid", 1)
          putTile(1, StaticTiledMapTile(TextureRegion()))
          putTile(2, StaticTiledMapTile(TextureRegion()))
        },
      )
    }
  val tiledMapWithTileLayers =
    TiledMap().apply {
      layers.add(
        TiledMapTileLayer(2, 2, 32, 32).apply {
          name = "tile-layer-1"
          setCell(0, 0, Cell().setTile(StaticTiledMapTile(TextureRegion())))
          setCell(1, 1, Cell().setTile(StaticTiledMapTile(TextureRegion())))
        },
      )
      layers.add(
        TiledMapTileLayer(2, 2, 32, 32).apply {
          name = "tile-layer-2"
          setCell(0, 1, Cell().setTile(StaticTiledMapTile(TextureRegion())))
        },
      )
    }

  test("should retrieve properties from TiledMap") {
    tiledMap.property<Int>("width") shouldBe 16
  }

  test("should retrieve properties from TiledMap with default value") {
    tiledMap.property("width", 0) shouldBe 16
    tiledMap.property("x", -1) shouldBe -1
  }

  test("should retrieve properties from TiledMap without default value") {
    tiledMap.propertyOrNull<Any>("x").shouldBeNull()
    tiledMap.propertyOrNull<Int>("width") shouldBe 16
  }

  test("should check if property from TiledMap exists") {
    tiledMap.containsProperty("width") shouldBe true
    tiledMap.containsProperty("x") shouldBe false
  }

  test("should retrieve standard properties of TiledMap") {
    tiledMap.width shouldBe 16
    tiledMap.height shouldBe 8
    tiledMap.tileWidth shouldBe 32
    tiledMap.tileHeight shouldBe 32
    tiledMap.totalWidth() shouldBe (16 * 32)
    tiledMap.totalHeight() shouldBe (8 * 32)
    tiledMap.backgroundColor shouldBe "#ffffff"
    tiledMap.orientation shouldBe "orthogonal"
    tiledMap.hexSideLength shouldBe 0
    tiledMap.staggerAxis shouldBe "Y"
    tiledMap.staggerIndex shouldBe "Odd"
  }

  test("should not retrieve non-existing property from TiledMap") {
    shouldThrow<MissingPropertyException> {
      tiledMap.property<String>("non-existing")
    }
  }

  test("should retrieve existing layer from TiledMap") {
    tiledMap.layer("layer-1").name shouldBe "layer-1"
  }

  test("should not retrieve non-existing layer from TiledMap") {
    shouldThrow<MissingLayerException> {
      tiledMap.layer("non-existing")
    }
  }

  test("should retrieve existing layer in TiledMap as nullable") {
    tiledMap.layerOrNull("layer-1")?.name shouldBe "layer-1"
  }

  test("should retrieve non-existing layer in TiledMap as null") {
    tiledMap.layerOrNull("non-existing").shouldBeNull()
  }

  test("should check if layer exists in TiledMap") {
    tiledMap.contains("layer-1") shouldBe true
    tiledMap.contains("non-existing") shouldBe false

    ("layer-1" in tiledMap) shouldBe true
    ("non-existing" in tiledMap) shouldBe false
  }

  test("should execute action per object of a layer") {
    var counter = 0

    tiledMap.forEachMapObject("layer-1") {
      it.isVisible = false
      counter++
    }

    counter shouldBe 3
    (tiledMap.layers["layer-1"].objects.all { !it.isVisible }) shouldBe true
  }

  test("should not execute any action for empty layer") {
    tiledMap.forEachMapObject("layer-2") {
      fail("Test failed.")
    }
  }

  test("should not execute any action for non-existing layer") {
    tiledMap.forEachMapObject("non-existing") {
      fail("Test failed.")
    }
  }

  test("should not execute lambda when there is no layer for the given type") {
    tiledMap.forEachLayer<TiledMapTileLayer> {
      fail("Test failed.")
    }
  }

  test("should execute lambda when there is a layer for the given type") {
    var counter = 0

    tiledMap.forEachLayer<MapLayer> {
      it.isVisible = false
      ++counter
    }

    counter shouldBe 2
    (tiledMap.layers.all { !it.isVisible }) shouldBe true
  }

  test("should retrieve tile by local id from TiledMap") {
    tiledMap.tileById("tileset-1", 0).shouldNotBeNull()
    tiledMap.tileById("tileset-1", 1).shouldNotBeNull()
  }

  test("should not retrieve tile from non-existing tileset of TiledMap") {
    shouldThrow<MissingTileSetException> {
      tiledMap.tileById("non-existing", 0)
    }
  }

  test("should not retrieve tile with non-existing local id from TiledMap") {
    shouldThrow<MissingTileException> {
      tiledMap.tileById("tileset-1", 2)
    }
  }

  test("should retrieve existing tileset from TiledMap") {
    tiledMap.tileSet("tileset-1").name shouldBe "tileset-1"
  }

  test("should not retrieve non-existing tileset from TiledMap") {
    shouldThrow<MissingTileSetException> {
      tiledMap.tileSet("non-existing")
    }
  }

  test("should execute action per cell of every tile layer of TiledMap") {
    val visitedCells = mutableMapOf<Pair<Int, Int>, String>()

    tiledMapWithTileLayers.forEachCell { layer, _, cellX, cellY ->
      visitedCells[cellX to cellY] = layer.name
    }

    visitedCells.size shouldBe 3
    visitedCells[0 to 0] shouldBe "tile-layer-1"
    visitedCells[1 to 1] shouldBe "tile-layer-1"
    visitedCells[0 to 1] shouldBe "tile-layer-2"
  }

  test("should not execute any action for TiledMap without tile layers") {
    tiledMap.forEachCell { _, _, _, _ ->
      fail("Test failed.")
    }
  }
})
