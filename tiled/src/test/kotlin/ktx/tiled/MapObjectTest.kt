package ktx.tiled

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.objects.CircleMapObject
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PointMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.objects.TextMapObject
import com.badlogic.gdx.maps.objects.TextureMapObject
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Ellipse
import com.badlogic.gdx.math.Rectangle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class MapObjectTest : FunSpec({
  val mapObject =
    MapObject().apply {
      properties.apply {
        put("id", 13)
        put("x", 1)
        put("y", 0f)
        put("rotation", -2.33f)
        put("type", "SomeType")
        put("width", 20f)
        put("height", 30f)
        put("name", "Property")
        put("active", true)
      }
    }

  test("should retrieve properties from MapObject") {
    mapObject.property<Int>("x") shouldBe 1
    mapObject.property<Float>("width") shouldBe 20f
    mapObject.property<String>("name") shouldBe "Property"
    mapObject.property<Boolean>("active") shouldBe true
  }

  test("should retrieve properties from MapObject with default value") {
    mapObject.property("x", 0) shouldBe 1
    mapObject.property("non-existing", 0) shouldBe 0
    mapObject.property("width", 0f) shouldBe 20f
    mapObject.property("name", "") shouldBe "Property"
    mapObject.property("active", false) shouldBe true
  }

  test("should retrieve properties from MapObject without default value") {
    mapObject.propertyOrNull<Any>("non-existing").shouldBeNull()
    mapObject.propertyOrNull<Int>("x") shouldBe 1
  }

  test("should check if property from MapObject exists") {
    mapObject.containsProperty("x") shouldBe true
    mapObject.containsProperty("non-existing") shouldBe false
  }

  test("should retrieve standard properties of MapObject") {
    mapObject.id shouldBe 13
    mapObject.x shouldBe 1f
    mapObject.y shouldBe 0f
    mapObject.width shouldBe 20f
    mapObject.height shouldBe 30f
    mapObject.rotation shouldBe -2.33f
    mapObject.type shouldBe "SomeType"
  }

  test("should not retrieve non-existing property from MapObject") {
    shouldThrow<MissingPropertyException> {
      mapObject.property<String>("non-existing")
    }
  }

  test("should retrieve shape from MapObject with Circle type") {
    val circleObject = CircleMapObject()

    circleObject.shape shouldBe Circle(0f, 0f, 1f)
  }

  test("should retrieve shape from MapObject with Ellipse type") {
    val ellipseObject = EllipseMapObject()

    ellipseObject.shape shouldBe Ellipse(0f, 0f, 1f, 1f)
  }

  test("should retrieve shape from MapObject with Polyline type") {
    val polylineVertices = floatArrayOf(0f, 0f, 1f, 1f)
    val polylineObject = PolylineMapObject(polylineVertices)

    polylineObject.shape shouldBe polylineObject.polyline
  }

  test("should retrieve shape from MapObject with Polygon type") {
    val polygonVertices = floatArrayOf(0f, 0f, 1f, 1f, 2f, 0f)
    val polygonObject = PolygonMapObject(polygonVertices)

    polygonObject.shape shouldBe polygonObject.polygon
  }

  test("should retrieve shape from MapObject with Rectangle type") {
    val rectObject = RectangleMapObject()

    rectObject.shape shouldBe Rectangle(0f, 0f, 1f, 1f)
  }

  test("should retrieve shape from MapObject with Point type") {
    val pointObject = PointMapObject()

    pointObject.shape shouldBe Rectangle(0f, 0f, 0f, 0f)
  }

  test("should retrieve shape from MapObject with Text type") {
    val textObject = TextMapObject()

    textObject.shape shouldBe Rectangle(0f, 0f, 1f, 1f)
  }

  test("retrieve shape from unsupported MapObject") {
    shouldThrow<MissingShapeException> {
      val textureObject = TextureMapObject()

      textureObject.shape
    }
  }

  test("should return true when MapObjects is empty") {
    val actual = MapObjects()

    actual.isEmpty() shouldBe true
  }

  test("should return false when MapObjects is not empty") {
    val actual = MapObjects()
    actual.add(MapObject())

    actual.isEmpty() shouldBe false
  }

  test("should return true when MapObjects is not empty") {
    val actual = MapObjects()
    actual.add(MapObject())

    actual.isNotEmpty() shouldBe true
  }

  test("should return false when MapObjects is empty") {
    val actual = MapObjects()

    actual.isNotEmpty() shouldBe false
  }
})
