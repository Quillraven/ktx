package ktx.tiled

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import com.badlogic.gdx.utils.Array
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

abstract class TiledMapTileTest<T : TiledMapTile> : FunSpec() {
  abstract val tile: T

  init {
    test("should retrieve properties from tile") {
      tile.property<Boolean>("prop1") shouldBe true
      tile.property<String>("prop2") shouldBe "text"
    }

    test("should retrieve properties from tile with default value") {
      tile.property("prop1", false) shouldBe true
      tile.property("prop2", "") shouldBe "text"
      tile.property("non-existing", -1f) shouldBe -1f
    }

    test("should retrieve properties from tile without default value") {
      tile.propertyOrNull<Any>("non-existing").shouldBeNull()
      tile.propertyOrNull<String>("prop2") shouldBe "text"
    }

    test("should check if property from tile exists") {
      tile.containsProperty("prop1") shouldBe true
      tile.containsProperty("prop2") shouldBe true
      tile.containsProperty("non-existing") shouldBe false
    }

    test("retrieve non-existing property from Tile using exception") {
      shouldThrow<MissingPropertyException> {
        tile.property<String>("non-existing")
      }
    }
  }
}

class StaticTiledMapTileTest : TiledMapTileTest<StaticTiledMapTile>() {
  override val tile =
    StaticTiledMapTile(TextureRegion()).apply {
      properties.apply {
        put("prop1", true)
        put("prop2", "text")
      }
    }
}

class AnimatedTiledMapTileTest : TiledMapTileTest<AnimatedTiledMapTile>() {
  override val tile =
    AnimatedTiledMapTile(1f, Array<StaticTiledMapTile>()).apply {
      properties.apply {
        put("prop1", true)
        put("prop2", "text")
      }
    }
}
