package ktx.tiled

import com.badlogic.gdx.maps.tiled.TiledMapTileSet
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class TiledMapTileSetTest : FunSpec({
  val tileset =
    TiledMapTileSet().apply {
      properties.apply {
        put("tilesetProp1", true)
        put("tilesetProp2", 123)
      }
    }

  test("should retrieve properties from TileSet") {
    tileset.property<Boolean>("tilesetProp1") shouldBe true
    tileset.property<Int>("tilesetProp2") shouldBe 123
  }

  test("should retrieve properties from TileSet with default value") {
    tileset.property("tilesetProp1", false) shouldBe true
    tileset.property("tilesetProp2", 0) shouldBe 123
    tileset.property("non-existing", -1f) shouldBe -1f
  }

  test("should retrieve properties from TileSet without default value") {
    tileset.propertyOrNull<Any>("non-existing").shouldBeNull()
    tileset.propertyOrNull<Int>("tilesetProp2") shouldBe 123
  }

  test("should check if property from TileSet exists") {
    tileset.containsProperty("tilesetProp1") shouldBe true
    tileset.containsProperty("non-existing") shouldBe false
  }

  test("should not retrieve non-existing property from TileSet") {
    shouldThrow<MissingPropertyException> {
      tileset.property<String>("non-existing")
    }
  }
})
