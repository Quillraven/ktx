package ktx.tiled

import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.TiledMapTileSet

/**
 * Extension method to directly access the [MapProperties] of a [TiledMap]. If the property
 * is not defined then this method throws a [MissingPropertyException].
 * @param key property name.
 * @return value of the property.
 * @throws MissingPropertyException If the property is not defined.
 */
inline fun <reified T> TiledMap.property(key: String): T =
  properties[key, T::class.java]
    ?: throw MissingPropertyException("Property $key does not exist.")

/**
 * Extension method to directly access the [MapProperties] of a [TiledMap]. The type is automatically
 * derived from the type of the given default value. If the property is not defined the [defaultValue]
 * will be returned.
 * @param key property name.
 * @param defaultValue default value in case the property is missing.
 * @return value of the property or defaultValue if property is missing.
 */
inline fun <reified T> TiledMap.property(
  key: String,
  defaultValue: T,
): T = properties[key, defaultValue, T::class.java]

/**
 * Extension method to directly access the [MapProperties] of a [TiledMap]. If the property
 * is not defined then this method returns null.
 * @param key property name.
 * @return value of the property or null if the property is missing.
 */
inline fun <reified T> TiledMap.propertyOrNull(key: String): T? = properties[key, T::class.java]

/**
 * Extension method to directly access the [MapProperties] of a [TiledMap] and its
 * [containsKey][MapProperties.containsKey] method.
 * @param key property name.
 * @return true if the property exists. Otherwise, false.
 */
fun TiledMap.containsProperty(key: String) = properties.containsKey(key)

/**
 * Extension property to retrieve the width of the [TiledMap].
 * @throws MissingPropertyException if property width does not exist.
 */
val TiledMap.width: Int
  get() = property("width")

/**
 * Extension property to retrieve the height of the [TiledMap].
 * @throws MissingPropertyException if property height does not exist.
 */
val TiledMap.height: Int
  get() = property("height")

/**
 * Extension property to retrieve the tile width of each tile of the [TiledMap].
 * @throws MissingPropertyException if property tilewidth does not exist.
 */
val TiledMap.tileWidth: Int
  get() = property("tilewidth")

/**
 * Extension property to retrieve the tile height of each tile of the [TiledMap].
 * @throws MissingPropertyException if property tileheight does not exist.
 */
val TiledMap.tileHeight: Int
  get() = property("tileheight")

/**
 * Extension property to retrieve the background color of the [TiledMap]. Null if property was not set.
 */
val TiledMap.backgroundColor: String?
  get() = propertyOrNull("backgroundcolor")

/**
 * Extension property to retrieve the orientation of the [TiledMap].
 * @throws MissingPropertyException if property orientation does not exist.
 */
val TiledMap.orientation: String
  get() = property("orientation")

/**
 * Extension property to retrieve the hex side length of a hexagonal [TiledMap].
 * @throws MissingPropertyException if property hexsidelength does not exist.
 */
val TiledMap.hexSideLength: Int
  get() = property("hexsidelength")

/**
 * Extension property to retrieve the stagger axis of the [TiledMap].
 * @throws MissingPropertyException if property staggeraxis does not exist.
 */
val TiledMap.staggerAxis: String
  get() = property("staggeraxis")

/**
 * Extension property to retrieve the stagger index of the [TiledMap].
 * @throws MissingPropertyException if property staggerindex does not exist.
 */
val TiledMap.staggerIndex: String
  get() = property("staggerindex")

/**
 * Extension method to retrieve the total width of the [TiledMap]. It is the result of the
 * width multiplied by the tile width of the map.
 *
 * @see [TiledMap.width]
 * @see [TiledMap.tileWidth]
 *
 * @return total width in pixels.
 */
fun TiledMap.totalWidth() = width * tileWidth

/**
 * Extension method to retrieve the total height of the [TiledMap]. It is the result of the
 * height multiplied by the tile height of the map.
 *
 * @see [TiledMap.height]
 * @see [TiledMap.tileHeight]
 *
 * @return total height in pixels.
 */
fun TiledMap.totalHeight() = height * tileHeight

/**
 * Extension operator to check if a certain [MapLayer] is part of the [TiledMap]
 * @param layerName name of [MapLayer].
 * @return true if and only if the layer does exist.
 */
operator fun TiledMap.contains(layerName: String) = layers[layerName] != null

/**
 * Extension method to retrieve a [MapLayer] of a [TiledMap]. If the layer does
 * not exist then this method is throwing a [MissingLayerException]
 * @param layerName name of [MapLayer]
 * @throws MissingLayerException If the layer does not exist
 */
fun TiledMap.layer(layerName: String) =
  layers[layerName]
    ?: throw MissingLayerException("Layer $layerName does not exist for map.")

/**
 * Extension method to easily execute an action per [MapObject] of a given [MapLayer].
 * If the layer does not exist then nothing is happening.
 *
 * @param layerName name of [MapLayer]
 * @param action action to execute per [MapObject] of the [MapLayer]
 */
inline fun TiledMap.forEachMapObject(
  layerName: String,
  action: (MapObject) -> Unit,
) {
  layers[layerName]?.objects?.forEach {
    action(it)
  }
}

/**
 * Extension method to run an [action] on a specific type of [layers][MapLayer] of the [TiledMap]. The lambda
 * takes the matching [MapLayer] as a parameter.
 *
 * The class matching is an exact match meaning that a given subclass like [TiledMapTileLayer] will not match
 * for the argument [MapLayer].
 */
inline fun <reified T : MapLayer> TiledMap.forEachLayer(action: (T) -> Unit) {
  this.layers.forEach {
    if (it::class == T::class) {
      action(it as T)
    }
  }
}

/**
 * Extension method to retrieve a [TiledMapTileSet] of the [TiledMap] by name. If the tileset does not
 * exist then this method throws a [MissingTileSetException].
 *
 * @param tilesetName name of the [TiledMapTileSet].
 * @return [TiledMapTileSet] matching the given name.
 * @throws MissingTileSetException if the tileset with the given [tilesetName] does not exist.
 */
fun TiledMap.tileSet(tilesetName: String): TiledMapTileSet =
  this.tileSets.getTileSet(tilesetName)
    ?: throw MissingTileSetException("TileSet $tilesetName does not exist for map.")

/**
 * Extension method to easily execute an action per [Cell] of every [TiledMapTileLayer] of the [TiledMap].
 * The action takes the [TiledMapTileLayer] the [Cell] belongs to, the [Cell] as well as its cell
 * coordinates as parameters. Cells without a tile (i.e. `null` cells) are skipped.
 *
 * @param action action to execute per non-empty [Cell] of every [TiledMapTileLayer]. The layer the [Cell]
 * belongs to is passed as the layer parameter, cell coordinates as the cellX and cellY parameters.
 */
inline fun TiledMap.forEachCell(action: (layer: TiledMapTileLayer, cell: Cell, cellX: Int, cellY: Int) -> Unit) {
  layers.filterIsInstance<TiledMapTileLayer>().forEach { layer ->
    layer.forEachCell { cell, cellX, cellY ->
      action(layer, cell, cellX, cellY)
    }
  }
}

/**
 * Extension method to retrieve a [TiledMapTile] from the [TiledMap] by the tileset name and the
 * local tile ID within that tileset. The [id] is the local tile ID as defined in the Tiled editor,
 * and is automatically converted to the global tile ID using the tileset's `firstgid` property.
 *
 * @param tilesetName name of the [TiledMapTileSet].
 * @param id local tile ID within the tileset.
 * @return [TiledMapTile] matching the given tileset and local tile ID.
 * @throws MissingTileSetException if the tileset with the given [tilesetName] does not exist.
 * @throws MissingTileException if the tile with the computed global ID does not exist in the tileset.
 */
fun TiledMap.tileById(
  tilesetName: String,
  id: Int,
): TiledMapTile {
  val tileSet = this.tileSet(tilesetName)
  val firstGid = tileSet.property<Int>("firstgid")
  val tiledId = firstGid + id

  return tileSet.getTile(tiledId)
    ?: throw MissingTileException("Tile of id $tiledId does not exist for TileSet $tilesetName. Firstgid=$firstGid.")
}
