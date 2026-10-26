package ktx.scene2d

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

class Scene2DSkinTest : FunSpec({
  test("should throw exception if defaultSkin is not initialized") {
    // Resetting Scene2DSkin with reflection to ensure that `skin` is null:
    Scene2DSkin::class
      .declaredMemberProperties
      .filter { it.name == "skin" }
      .map { it.javaField!! }
      .onEach { it.isAccessible = true }
      .forEach { it.set(Scene2DSkin, null) }

    shouldThrow<IllegalStateException> {
      Scene2DSkin.defaultSkin
    }
  }

  test("should invoke skin reload listeners") {
    var invoked = false

    Scene2DSkin.addListener { invoked = true }

    Scene2DSkin.defaultSkin = Skin() // Invokes setter, should trigger listeners.
    invoked shouldBe true
  }

  test("should remove listeners") {
    var amount = 0
    val listener: (Skin) -> Unit = { amount++ }
    Scene2DSkin.addListener(listener)

    Scene2DSkin.defaultSkin = Skin() // Invokes setter, should trigger listeners.
    amount shouldBe 1

    Scene2DSkin.removeListener(listener)
    Scene2DSkin.defaultSkin = Skin() // Invokes setter, should trigger listeners.
    amount shouldBe 1
  }

  test("should clear listeners") {
    var amount = 0
    Scene2DSkin.addListener { amount++ }
    Scene2DSkin.addListener { amount++ }

    Scene2DSkin.defaultSkin = Skin() // Invokes setter, should trigger listeners.
    amount shouldBe 2

    Scene2DSkin.clearListeners()
    Scene2DSkin.defaultSkin = Skin() // Invokes setter, should trigger listeners.
    amount shouldBe 2
  }
})
