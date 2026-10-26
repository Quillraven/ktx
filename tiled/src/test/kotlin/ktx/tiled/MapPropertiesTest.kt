package ktx.tiled

import com.badlogic.gdx.maps.MapProperties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapPropertiesTest : FunSpec({
  test("should check if a property exists") {
    val mapProperties = MapProperties()
    mapProperties.put("key", "value")

    val result = "key" in mapProperties

    result shouldBe true
  }

  test("should check if a property exists given missing key") {
    val mapProperties = MapProperties()

    val result = "key" in mapProperties

    result shouldBe false
  }

  test("should add property") {
    val mapProperties = MapProperties()

    mapProperties["key"] = "value"

    mapProperties["key"] shouldBe "value"
  }

  test("should override property") {
    val mapProperties = MapProperties()
    mapProperties.put("key", "old")

    mapProperties["key"] = "new"

    mapProperties["key"] shouldBe "new"
  }
})
