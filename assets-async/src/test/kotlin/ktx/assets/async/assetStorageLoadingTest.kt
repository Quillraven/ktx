package ktx.assets.async

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Logger
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.coroutines.runBlocking
import ktx.assets.TextAssetLoader
import ktx.async.AsyncTest
import org.mockito.kotlin.mock
import java.util.IdentityHashMap
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect as ParticleEffect3D

/**
 * [AssetStorage] has 3 main variants of asset loading: [AssetStorage.load], [AssetStorage.loadAsync]
 * and [AssetStorage.loadSync]. To test each one, a common abstract test suite is provided.
 *
 * This test suite ensures that each method supports loading of every default asset type
 * and performs basic asset loading logic tests.
 *
 * Note that variants consuming [String] path and reified asset types could not be easily tested,
 * as they cannot be invoked in abstract methods. However, since all of them are just aliases and
 * contain no logic other than [AssetDescriptor] or [Identifier] initiation, the associated loading
 * methods are still tested.
 *
 * See also: [AssetStorageTest].
 */
abstract class AbstractAssetStorageLoadingTest : AsyncTest() {
  var testName = ""

  /**
   * Must be overridden with the tested loading method variant.
   * Blocks the current thread until the selected asset is loaded.
   */
  protected abstract fun <T> AssetStorage.testLoad(
    path: String,
    type: Class<T>,
    parameters: AssetLoaderParameters<T>?,
  ): T

  private inline fun <reified T> AssetStorage.testLoad(
    path: String,
    parameters: AssetLoaderParameters<T>? = null,
  ): T = testLoad(path, T::class.java, parameters)

  /**
   * Allows to validate state of [LoadingProgress] without failing the test case.
   * Pass [warn] not to fail the test on progress mismatch.
   *
   * Progress is eventually consistent. It does not have to be up to date with the [AssetStorage] state.
   * Usually it will be and all tests would pass just fine, but there are these rare situations where
   * the asserts are evaluated before the progress is updated. That's why if such case is possible,
   * only a warning will be printed instead of failing the test.
   *
   * If the warnings are common, it might point to a bug within the progress updating.
   */
  private fun checkProgress(
    storage: AssetStorage,
    loaded: Int = 0,
    failed: Int = 0,
    total: Int = loaded + failed,
    warn: Boolean = false,
  ) {
    if (warn) {
      val progress = storage.progress
      if (total != progress.total || loaded != progress.loaded || failed != progress.failed) {
        System.err.println(
          """
          Warning: mismatch in progress value in `$testName`.
          Value  | Expected | Actual
          total  | ${"%8d".format(total)} | ${progress.total}
          loaded | ${"%8d".format(loaded)} | ${progress.loaded}
          failed | ${"%8d".format(failed)} | ${progress.failed}
          If this warning is repeated consistently, there might be a related bug in progress reporting.
          """.trimIndent(),
        )
      }
    } else {
      storage.progress.total shouldBe total
      storage.progress.loaded shouldBe loaded
      storage.progress.failed shouldBe failed
    }
  }

  override fun `setup libGDX application`() {
    super.`setup libGDX application`()
    if (System.getenv("TEST_PROFILE") != "ci") {
      Gdx.audio = OpenALLwjgl3Audio()
    }
  }

  override fun `exit libGDX application`() {
    super.`exit libGDX application`()
    (Gdx.audio as? OpenALLwjgl3Audio)?.dispose()
  }

  init {
    beforeTest { testName = it.name.name }

    // --- Asset support tests:

    test("should load text assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"

      // When:
      val asset = storage.testLoad<String>(path)

      // Then:
      asset shouldBe "Content."
      storage.isLoaded<String>(path) shouldBe true
      storage.get<String>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<String>(path) shouldBe 1
      storage.getDependencies<String>(path) shouldBe emptyList<Identifier<*>>()
    }

    test("should load text assets with parameters") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"

      // When:
      val asset = storage.testLoad(path, parameters = TextAssetLoader.TextAssetLoaderParameters("UTF-8"))

      // Then:
      asset shouldBe "Content."
      storage.isLoaded<String>(path) shouldBe true
      storage.get<String>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<String>(path) shouldBe 1
      storage.getDependencies<String>(path) shouldBe emptyList<Identifier<*>>()
    }

    test("should unload text assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      storage.testLoad<String>(path)

      // When:
      runBlocking { storage.unload<String>(path) }

      // Then:
      storage.isLoaded<String>(path) shouldBe false
      storage.getReferenceCount<String>(path) shouldBe 0
    }

    test("should load BitmapFont assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "com/badlogic/gdx/utils/lsans-15.fnt"
      val dependency = "com/badlogic/gdx/utils/lsans-15.png"

      // When:
      val asset = storage.testLoad<BitmapFont>(path)

      // Then:
      storage.isLoaded<BitmapFont>(path) shouldBe true
      storage.get<BitmapFont>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(path) shouldBe 1
      storage.getDependencies<BitmapFont>(path) shouldBe listOf(storage.getIdentifier<Texture>(dependency))
      // Font dependencies:
      storage.isLoaded<Texture>(dependency) shouldBe true
      storage.getReferenceCount<Texture>(dependency) shouldBe 1
      storage.get<Texture>(dependency) shouldBeSameInstanceAs asset.region.texture

      storage.dispose()
    }

    test("should unload BitmapFont with dependencies") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "com/badlogic/gdx/utils/lsans-15.fnt"
      val dependency = "com/badlogic/gdx/utils/lsans-15.png"
      storage.testLoad<BitmapFont>(path)

      // When:
      runBlocking { storage.unload<BitmapFont>(path) }

      // Then:
      storage.isLoaded<BitmapFont>(path) shouldBe false
      storage.getReferenceCount<BitmapFont>(path) shouldBe 0
      storage.isLoaded<Texture>(dependency) shouldBe false
      storage.getReferenceCount<Texture>(path) shouldBe 0
    }

    test("should load Music assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/sound.ogg"

      // When:
      val asset = storage.testLoad<Music>(path)

      // Then:
      storage.isLoaded<Music>(path) shouldBe true
      storage.get<Music>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Music>(path) shouldBe 1
      storage.getDependencies<Music>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload Music assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/sound.ogg"
      storage.testLoad<Music>(path)

      // When:
      runBlocking { storage.unload<Music>(path) }

      // Then:
      storage.isLoaded<Music>(path) shouldBe false
      storage.getReferenceCount<Music>(path) shouldBe 0
    }

    test("should load Sound assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/sound.ogg"

      // When:
      val asset = storage.testLoad<Sound>(path)

      // Then:
      storage.isLoaded<Sound>(path) shouldBe true
      storage.get<Sound>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Sound>(path) shouldBe 1
      storage.getDependencies<Sound>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload Sound assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/sound.ogg"
      storage.testLoad<Sound>(path)

      // When:
      runBlocking { storage.unload<Sound>(path) }

      // Then:
      storage.isLoaded<Sound>(path) shouldBe false
      storage.getReferenceCount<Sound>(path) shouldBe 0
    }

    test("should load TextureAtlas assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/skin.atlas"
      val dependency = "ktx/assets/async/texture.png"

      // When:
      val asset = storage.testLoad<TextureAtlas>(path)

      // Then:
      storage.isLoaded<TextureAtlas>(path) shouldBe true
      storage.get<TextureAtlas>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<TextureAtlas>(path) shouldBe 1
      storage.getDependencies<TextureAtlas>(path) shouldBe listOf(storage.getIdentifier<Texture>(dependency))
      // Atlas dependencies:
      storage.isLoaded<Texture>(dependency) shouldBe true
      storage.get<Texture>(dependency) shouldBeSameInstanceAs asset.textures.first()
      storage.getReferenceCount<Texture>(dependency) shouldBe 1

      storage.dispose()
    }

    test("should unload TextureAtlas assets with dependencies") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/skin.atlas"
      val dependency = "ktx/assets/async/texture.png"
      storage.testLoad<TextureAtlas>(path)

      // When:
      runBlocking { storage.unload<TextureAtlas>(path) }

      // Then:
      storage.isLoaded<TextureAtlas>(path) shouldBe false
      storage.getReferenceCount<TextureAtlas>(path) shouldBe 0
      storage.isLoaded<Texture>(dependency) shouldBe false
      storage.getReferenceCount<Texture>(dependency) shouldBe 0
    }

    test("should load Texture assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"

      // When:
      val asset = storage.testLoad<Texture>(path)

      // Then:
      storage.isLoaded<Texture>(path) shouldBe true
      storage.get<Texture>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Texture>(path) shouldBe 1
      storage.getDependencies<Texture>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload Texture assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"
      storage.testLoad<Texture>(path)

      // When:
      runBlocking { storage.unload<Texture>(path) }

      // Then:
      storage.isLoaded<Texture>(path) shouldBe false
      storage.getReferenceCount<Texture>(path) shouldBe 0
    }

    test("should load Pixmap assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"

      // When:
      val asset = storage.testLoad<Pixmap>(path)

      // Then:
      storage.isLoaded<Pixmap>(path) shouldBe true
      storage.get<Pixmap>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Pixmap>(path) shouldBe 1
      storage.getDependencies<Pixmap>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload Pixmap assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"
      storage.testLoad<Pixmap>(path)

      // When:
      runBlocking { storage.unload<Pixmap>(path) }

      // Then:
      storage.isLoaded<Pixmap>(path) shouldBe false
      storage.getReferenceCount<Pixmap>(path) shouldBe 0
    }

    test("should load Skin assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/skin.json"
      val atlas = "ktx/assets/async/skin.atlas"
      val texture = "ktx/assets/async/texture.png"

      // When:
      val asset = storage.testLoad<Skin>(path)

      // Then:
      storage.isLoaded<Skin>(path) shouldBe true
      storage.get<Skin>(path) shouldBeSameInstanceAs asset
      asset.get("default", Button.ButtonStyle::class.java).shouldNotBeNull()
      storage.getReferenceCount<Skin>(path) shouldBe 1
      storage.getDependencies<Skin>(path) shouldBe listOf(storage.getIdentifier<TextureAtlas>(atlas))
      // Skin dependencies:
      storage.isLoaded<TextureAtlas>(atlas) shouldBe true
      storage.getReferenceCount<TextureAtlas>(atlas) shouldBe 1
      storage.get<TextureAtlas>(atlas) shouldBeSameInstanceAs asset.atlas
      storage.getDependencies<TextureAtlas>(atlas) shouldBe listOf(storage.getIdentifier<Texture>(texture))
      // Atlas dependencies:
      storage.isLoaded<Texture>(texture) shouldBe true
      storage.get<Texture>(texture) shouldBeSameInstanceAs asset.atlas.textures.first()
      storage.getReferenceCount<Texture>(texture) shouldBe 1

      storage.dispose()
    }

    test("should unload Skin assets with dependencies") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/skin.json"
      val atlas = "ktx/assets/async/skin.atlas"
      val texture = "ktx/assets/async/texture.png"
      storage.testLoad<Skin>(path)

      // When:
      runBlocking { storage.unload<Skin>(path) }

      // Then:
      storage.isLoaded<Skin>(path) shouldBe false
      storage.getReferenceCount<Skin>(path) shouldBe 0
      storage.isLoaded<TextureAtlas>(atlas) shouldBe false
      storage.getReferenceCount<TextureAtlas>(atlas) shouldBe 0
      storage.isLoaded<Texture>(texture) shouldBe false
      storage.getReferenceCount<Texture>(texture) shouldBe 0
    }

    test("should load I18NBundle assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/i18n"

      // When:
      val asset = storage.testLoad<I18NBundle>(path)

      // Then:
      storage.isLoaded<I18NBundle>(path) shouldBe true
      asset["key"] shouldBe "Value."
      storage.get<I18NBundle>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<I18NBundle>(path) shouldBe 1
      storage.getDependencies<I18NBundle>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload I18NBundle assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/i18n"
      storage.testLoad<I18NBundle>(path)

      // When:
      runBlocking { storage.unload<I18NBundle>(path) }

      // Then:
      storage.isLoaded<I18NBundle>(path) shouldBe false
      storage.getReferenceCount<I18NBundle>(path) shouldBe 0
    }

    test("should load ParticleEffect assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/particle.p2d"

      // When:
      val asset = storage.testLoad<ParticleEffect>(path)

      // Then:
      storage.isLoaded<ParticleEffect>(path) shouldBe true
      storage.get<ParticleEffect>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<ParticleEffect>(path) shouldBe 1
      storage.getDependencies<ParticleEffect>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload ParticleEffect assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/particle.p2d"
      storage.testLoad<ParticleEffect>(path)

      // When:
      runBlocking { storage.unload<ParticleEffect>(path) }

      // Then:
      storage.isLoaded<ParticleEffect>(path) shouldBe false
      storage.getReferenceCount<ParticleEffect>(path) shouldBe 0
    }

    test("should load ParticleEffect3D assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/particle.p3d"
      val dependency = "ktx/assets/async/texture.png"

      // When:
      val asset = storage.testLoad<ParticleEffect3D>(path)

      // Then:
      storage.isLoaded<ParticleEffect3D>(path) shouldBe true
      storage.get<ParticleEffect3D>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<ParticleEffect3D>(path) shouldBe 1
      storage.getDependencies<ParticleEffect3D>(path) shouldBe listOf(storage.getIdentifier<Texture>(dependency))
      // Particle dependencies:
      storage.isLoaded<Texture>(dependency) shouldBe true
      storage.get<Texture>(dependency).shouldNotBeNull()
      storage.getReferenceCount<Texture>(dependency) shouldBe 1

      storage.dispose()
    }

    test("should unload ParticleEffect3D assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/particle.p3d"
      val dependency = "ktx/assets/async/texture.png"
      storage.testLoad<ParticleEffect3D>(path)

      // When:
      runBlocking { storage.unload<ParticleEffect3D>(path) }

      // Then:
      storage.isLoaded<ParticleEffect3D>(path) shouldBe false
      storage.getReferenceCount<ParticleEffect3D>(path) shouldBe 0
      storage.isLoaded<Texture>(dependency) shouldBe false
      storage.getReferenceCount<Texture>(dependency) shouldBe 0
    }

    test("should load PolygonRegion assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/polygon.psh"
      val dependency = "ktx/assets/async/polygon.png"

      // When:
      val asset = storage.testLoad<PolygonRegion>(path)

      // Then:
      storage.isLoaded<PolygonRegion>(path) shouldBe true
      storage.get<PolygonRegion>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<PolygonRegion>(path) shouldBe 1
      storage.getDependencies<PolygonRegion>(path) shouldBe listOf(storage.getIdentifier<Texture>(dependency))
      // Polygon region dependencies:
      storage.isLoaded<Texture>(dependency) shouldBe true
      storage.get<Texture>(dependency) shouldBeSameInstanceAs asset.region.texture
      storage.getReferenceCount<Texture>(dependency) shouldBe 1

      storage.dispose()
    }

    test("should unload PolygonRegion assets with dependencies") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/polygon.psh"
      val dependency = "ktx/assets/async/polygon.png"
      storage.testLoad<PolygonRegion>(path)

      // When:
      runBlocking { storage.unload<PolygonRegion>(path) }

      // Then:
      storage.isLoaded<PolygonRegion>(path) shouldBe false
      storage.getReferenceCount<PolygonRegion>(path) shouldBe 0
      storage.isLoaded<Texture>(dependency) shouldBe false
      storage.getReferenceCount<Texture>(dependency) shouldBe 0
    }

    test("should load OBJ Model assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/model.obj"

      // When:
      val asset = storage.testLoad<Model>(path)

      // Then:
      storage.isLoaded<Model>(path) shouldBe true
      storage.get<Model>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Model>(path) shouldBe 1
      storage.getDependencies<Model>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload OBJ Model assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/model.obj"
      storage.testLoad<Model>(path)

      // When:
      runBlocking { storage.unload<Model>(path) }

      // Then:
      storage.isLoaded<Model>(path) shouldBe false
      storage.getReferenceCount<Model>(path) shouldBe 0
    }

    test("should load G3DJ Model assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/model.g3dj"

      // When:
      val asset = storage.testLoad<Model>(path)

      // Then:
      storage.isLoaded<Model>(path) shouldBe true
      storage.get<Model>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Model>(path) shouldBe 1
      storage.getDependencies<Model>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload G3DJ Model assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/model.g3dj"
      storage.testLoad<Model>(path)

      // When:
      runBlocking { storage.unload<Model>(path) }

      // Then:
      storage.isLoaded<Model>(path) shouldBe false
      storage.getReferenceCount<Model>(path) shouldBe 0
    }

    test("should load G3DB Model assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/model.g3db"

      // When:
      val asset = storage.testLoad<Model>(path)

      // Then:
      storage.isLoaded<Model>(path) shouldBe true
      storage.get<Model>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Model>(path) shouldBe 1
      storage.getDependencies<Model>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload G3DB Model assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/model.g3db"
      storage.testLoad<Model>(path)

      // When:
      runBlocking { storage.unload<Model>(path) }

      // Then:
      storage.isLoaded<Model>(path) shouldBe false
      storage.getReferenceCount<Model>(path) shouldBe 0
    }

    test("should load ShaderProgram assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/shader.frag"
      // Silencing logs - shader will fail to compile, as GL is mocked:
      storage.logger.level = Logger.NONE

      // When:
      val asset = storage.testLoad<ShaderProgram>(path)

      // Then:
      storage.isLoaded<ShaderProgram>(path) shouldBe true
      storage.get<ShaderProgram>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<ShaderProgram>(path) shouldBe 1
      storage.getDependencies<ShaderProgram>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload ShaderProgram assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/shader.frag"
      // Silencing logs - shader will fail to compile, as GL is mocked:
      storage.logger.level = Logger.NONE
      storage.testLoad<ShaderProgram>(path)

      // When:
      runBlocking { storage.unload<ShaderProgram>(path) }

      // Then:
      storage.isLoaded<ShaderProgram>(path) shouldBe false
      storage.getReferenceCount<ShaderProgram>(path) shouldBe 0
    }

    test("should load Cubemap assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/cubemap.zktx"

      // When:
      val asset = storage.testLoad<Cubemap>(path)

      // Then:
      storage.isLoaded<Cubemap>(path) shouldBe true
      storage.get<Cubemap>(path) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Cubemap>(path) shouldBe 1
      storage.getDependencies<Cubemap>(path) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should unload Cubemap assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/cubemap.zktx"
      storage.testLoad<Cubemap>(path)

      // When:
      runBlocking { storage.unload<Cubemap>(path) }

      // Then:
      storage.isLoaded<Cubemap>(path) shouldBe false
      storage.getReferenceCount<Cubemap>(path) shouldBe 0
    }

    test("should dispose of multiple assets of different types without errors") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      storage.logger.level = Logger.NONE
      val assets =
        listOf(
          storage.getIdentifier<String>("ktx/assets/async/string.txt"),
          storage.getIdentifier<BitmapFont>("com/badlogic/gdx/utils/lsans-15.fnt"),
          storage.getIdentifier<Music>("ktx/assets/async/sound.ogg"),
          storage.getIdentifier<TextureAtlas>("ktx/assets/async/skin.atlas"),
          storage.getIdentifier<Texture>("ktx/assets/async/texture.png"),
          storage.getIdentifier<Skin>("ktx/assets/async/skin.json"),
          storage.getIdentifier<I18NBundle>("ktx/assets/async/i18n"),
          storage.getIdentifier<ParticleEffect>("ktx/assets/async/particle.p2d"),
          storage.getIdentifier<ParticleEffect3D>("ktx/assets/async/particle.p3d"),
          storage.getIdentifier<PolygonRegion>("ktx/assets/async/polygon.psh"),
          storage.getIdentifier<Model>("ktx/assets/async/model.obj"),
          storage.getIdentifier<Model>("ktx/assets/async/model.g3dj"),
          storage.getIdentifier<Model>("ktx/assets/async/model.g3db"),
          storage.getIdentifier<ShaderProgram>("ktx/assets/async/shader.frag"),
          storage.getIdentifier<Cubemap>("ktx/assets/async/cubemap.zktx"),
        )
      assets.forEach {
        storage.testLoad(it.path, it.type, parameters = null)
        storage.isLoaded(it) shouldBe true
      }

      // When:
      storage.dispose()

      // Then:
      assets.forEach {
        (it in storage) shouldBe false
        storage.isLoaded(it) shouldBe false
        storage.getReferenceCount(it) shouldBe 0
        storage.getDependencies(it) shouldBe emptyList<Identifier<*>>()
        shouldThrow<MissingAssetException> {
          storage[it]
        }
      }
    }

    // --- Behavior tests:

    test("should return same asset instance with subsequent load calls on loaded asset") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"
      val loaded = storage.testLoad<Texture>(path)

      // When:
      val assets = (1..10).map { storage.testLoad<Texture>(path) }

      // Then:
      storage.getReferenceCount<Texture>(path) shouldBe 11
      assets.forEach { asset ->
        asset shouldBeSameInstanceAs loaded
      }
      checkProgress(storage, loaded = 1, warn = true)

      storage.dispose()
    }

    test("should obtain loaded asset with path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"

      // When:
      storage.testLoad<String>(path)

      // Then:
      storage.contains<String>(path) shouldBe true
      storage.isLoaded<String>(path) shouldBe true
      storage.get<String>(path) shouldBe "Content."
      storage.getOrNull<String>(path) shouldBe "Content."
      (runBlocking { storage.getAsync<String>(path).await() }) shouldBe "Content."
      storage.getDependencies<String>(path) shouldBe emptyList<Identifier<*>>()
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should obtain loaded asset with identifier") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val identifier = storage.getIdentifier<String>("ktx/assets/async/string.txt")

      // When:
      storage.testLoad<String>(identifier.path)

      // Then:
      (identifier in storage) shouldBe true
      storage.isLoaded(identifier) shouldBe true
      storage[identifier] shouldBe "Content."
      storage.getOrNull(identifier) shouldBe "Content."
      (runBlocking { storage.getAsync(identifier).await() }) shouldBe "Content."
      storage.getDependencies(identifier) shouldBe emptyList<Identifier<*>>()
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should obtain loaded asset with descriptor") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val descriptor = storage.getAssetDescriptor<String>("ktx/assets/async/string.txt")

      // When:
      storage.testLoad<String>(descriptor.fileName)

      // Then:
      (descriptor in storage) shouldBe true
      storage.isLoaded(descriptor) shouldBe true
      storage[descriptor] shouldBe "Content."
      storage.getOrNull(descriptor) shouldBe "Content."
      (runBlocking { storage.getAsync(descriptor).await() }) shouldBe "Content."
      storage.getDependencies(descriptor) shouldBe emptyList<Identifier<*>>()
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should unload assets with path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      storage.testLoad<String>(path)

      // When:
      runBlocking { storage.unload<String>(path) }

      // Then:
      storage.isLoaded<String>(path) shouldBe false
      storage.getReferenceCount<String>(path) shouldBe 0
      checkProgress(storage, total = 0, warn = true)
    }

    test("should unload assets with descriptor") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val descriptor = storage.getAssetDescriptor<String>(path)
      storage.testLoad<String>(path)

      // When:
      runBlocking { storage.unload(descriptor) }

      // Then:
      storage.isLoaded(descriptor) shouldBe false
      storage.getReferenceCount(descriptor) shouldBe 0
      checkProgress(storage, total = 0, warn = true)
    }

    test("should unload assets with identifier") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val identifier = storage.getIdentifier<String>(path)
      storage.testLoad<String>(path)

      // When:
      runBlocking { storage.unload(identifier) }

      // Then:
      storage.isLoaded(identifier) shouldBe false
      storage.getReferenceCount(identifier) shouldBe 0
      checkProgress(storage, total = 0, warn = true)
    }

    test("should allow to load multiple assets with different type and same path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"

      // When:
      storage.testLoad<Texture>(path)
      storage.testLoad<Pixmap>(path)

      // Then:
      storage.isLoaded<Texture>(path) shouldBe true
      storage.isLoaded<Pixmap>(path) shouldBe true
      storage.getReferenceCount<Texture>(path) shouldBe 1
      storage.getReferenceCount<Pixmap>(path) shouldBe 1
      (storage.get<Pixmap>(path) as Any) shouldNotBeSameInstanceAs storage.get<Texture>(path)
      checkProgress(storage, loaded = 2, warn = true)

      storage.dispose()
    }

    test("should increase references count and return the same asset when trying to load asset with same path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val elements = IdentityHashMap<String, Boolean>()

      // When:
      repeat(3) {
        val asset = storage.testLoad<String>(path)
        elements[asset] = true
      }

      // Then:
      storage.getReferenceCount<String>(path) shouldBe 3
      elements.size shouldBe 1
    }

    test("should fail to load asset with missing loader") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"

      // When:
      shouldThrow<MissingLoaderException> {
        storage.testLoad<Vector2>(path)
      }

      // Then:
      storage.contains<Vector2>(path) shouldBe false
      checkProgress(storage, total = 0)
    }

    test("should increase references counts of dependencies when loading same asset") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/skin.json"
      val dependencies =
        arrayOf(
          storage.getIdentifier<TextureAtlas>("ktx/assets/async/skin.atlas"),
          storage.getIdentifier<Texture>("ktx/assets/async/texture.png"),
        )
      val loadedAssets = IdentityHashMap<Skin, Boolean>()

      // When:
      repeat(3) {
        val asset = storage.testLoad<Skin>(path)
        loadedAssets[asset] = true
      }

      // Then:
      storage.getReferenceCount<Skin>(path) shouldBe 3
      dependencies.forEach {
        storage.getReferenceCount(it) shouldBe 3
      }
      loadedAssets.size shouldBe 1
      checkProgress(storage, loaded = 3, warn = true)
    }

    test("should handle loading exceptions") {
      // Given:
      val loader =
        AssetStorageTest.FakeSyncLoader(
          onLoad = { throw IllegalStateException("Expected.") },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }
      val path = "fake path"

      // When:
      shouldThrow<AssetLoadingException> {
        storage.testLoad<AssetStorageTest.FakeAsset>(path)
      }

      // Then: asset should still be in storage, but rethrowing original exception:
      storage.contains<AssetStorageTest.FakeAsset>(path) shouldBe true
      storage.getReferenceCount<AssetStorageTest.FakeAsset>(path) shouldBe 1
      shouldThrow<AssetLoadingException> {
        storage.get<AssetStorageTest.FakeAsset>(path)
      }
      shouldThrow<AssetLoadingException> {
        storage.getOrNull<AssetStorageTest.FakeAsset>(path)
      }
      val reference = storage.getAsync<AssetStorageTest.FakeAsset>(path)
      shouldThrow<AssetLoadingException> {
        runBlocking { reference.await() }
      }
      checkProgress(storage, failed = 1, warn = true)
    }

    test("should handle asynchronous loading exceptions") {
      // Given:
      val loader =
        AssetStorageTest.FakeAsyncLoader(
          onAsync = { throw IllegalStateException("Expected.") },
          onSync = {},
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }
      val path = "fake path"

      // When:
      shouldThrow<AssetLoadingException> {
        storage.testLoad<AssetStorageTest.FakeAsset>(path)
      }

      // Then: asset should still be in storage, but rethrowing original exception:
      storage.contains<AssetStorageTest.FakeAsset>(path) shouldBe true
      storage.getReferenceCount<AssetStorageTest.FakeAsset>(path) shouldBe 1
      shouldThrow<AssetLoadingException> {
        storage.get<AssetStorageTest.FakeAsset>(path)
      }
      shouldThrow<AssetLoadingException> {
        storage.getOrNull<AssetStorageTest.FakeAsset>(path)
      }
      val reference = storage.getAsync<AssetStorageTest.FakeAsset>(path)
      shouldThrow<AssetLoadingException> {
        runBlocking { reference.await() }
      }
      checkProgress(storage, failed = 1, warn = true)
    }

    test("should handle synchronous loading exceptions") {
      // Given:
      val loader =
        AssetStorageTest.FakeAsyncLoader(
          onAsync = { },
          onSync = { throw IllegalStateException("Expected.") },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }
      val path = "fake path"

      // When:
      shouldThrow<AssetLoadingException> {
        storage.testLoad<AssetStorageTest.FakeAsset>(path)
      }

      // Then: asset should still be in storage, but rethrowing original exception:
      storage.contains<AssetStorageTest.FakeAsset>(path) shouldBe true
      storage.getReferenceCount<AssetStorageTest.FakeAsset>(path) shouldBe 1
      shouldThrow<AssetLoadingException> {
        storage.get<AssetStorageTest.FakeAsset>(path)
      }
      shouldThrow<AssetLoadingException> {
        storage.getOrNull<AssetStorageTest.FakeAsset>(path)
      }
      val reference = storage.getAsync<AssetStorageTest.FakeAsset>(path)
      shouldThrow<AssetLoadingException> {
        runBlocking { reference.await() }
      }
      checkProgress(storage, failed = 1, warn = true)
    }

    test("should not fail to unload asset that was loaded exceptionally") {
      // Given:
      val loader =
        AssetStorageTest.FakeSyncLoader(
          onLoad = { throw IllegalStateException("Expected.") },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake path"
      storage.setLoader { loader }
      storage.logger.level = Logger.NONE // Disposing exception will be logged.
      try {
        storage.testLoad<AssetStorageTest.FakeAsset>(path)
      } catch (exception: AssetLoadingException) {
        // Expected.
      }

      // When:
      val unloaded =
        runBlocking {
          storage.unload<AssetStorageTest.FakeAsset>(path)
        }

      // Then:
      unloaded shouldBe true
      storage.contains<AssetStorageTest.FakeAsset>(path) shouldBe false
      storage.getReferenceCount<AssetStorageTest.FakeAsset>(path) shouldBe 0
    }

    beforeSpec {
      // Necessary for libGDX asset loaders to work.
      Lwjgl3NativesLoader.load()
      Gdx.graphics = mock()
      Gdx.gl20 = mock()
      Gdx.gl = Gdx.gl20
    }

    afterSpec {
      Gdx.graphics = null
      Gdx.audio = null
      Gdx.gl20 = null
      Gdx.gl = null
    }
  }
}

/**
 * Performs asset loading tests with [AssetStorage.loadAsync] consuming [AssetDescriptor].
 */
class AssetStorageLoadingTestWithAssetDescriptorLoadAsync : AbstractAssetStorageLoadingTest() {
  override fun <T> AssetStorage.testLoad(
    path: String,
    type: Class<T>,
    parameters: AssetLoaderParameters<T>?,
  ): T =
    runBlocking {
      loadAsync(AssetDescriptor(path, type, parameters)).await()
    }
}

/**
 * Performs asset loading tests with [AssetStorage.loadAsync] consuming [Identifier].
 */
class AssetStorageLoadingTestWithIdentifierLoadAsync : AbstractAssetStorageLoadingTest() {
  override fun <T> AssetStorage.testLoad(
    path: String,
    type: Class<T>,
    parameters: AssetLoaderParameters<T>?,
  ): T =
    runBlocking {
      loadAsync(Identifier(path, type), parameters).await()
    }
}

/**
 * Performs asset loading tests with [AssetStorage.load] consuming [AssetDescriptor].
 */
class AssetStorageLoadingTestWithAssetDescriptorLoad : AbstractAssetStorageLoadingTest() {
  override fun <T> AssetStorage.testLoad(
    path: String,
    type: Class<T>,
    parameters: AssetLoaderParameters<T>?,
  ): T =
    runBlocking {
      load(AssetDescriptor(path, type, parameters))
    }
}

/**
 * Performs asset loading tests with [AssetStorage.load] consuming [Identifier].
 */
class AssetStorageLoadingTestWithIdentifierLoad : AbstractAssetStorageLoadingTest() {
  override fun <T> AssetStorage.testLoad(
    path: String,
    type: Class<T>,
    parameters: AssetLoaderParameters<T>?,
  ): T =
    runBlocking {
      load(Identifier(path, type), parameters)
    }
}

/**
 * Performs asset loading tests with [AssetStorage.loadSync] consuming [AssetDescriptor].
 */
class AssetStorageLoadingTestWithAssetDescriptorLoadSync : AbstractAssetStorageLoadingTest() {
  override fun <T> AssetStorage.testLoad(
    path: String,
    type: Class<T>,
    parameters: AssetLoaderParameters<T>?,
  ): T = loadSync(AssetDescriptor(path, type, parameters))
}

/**
 * Performs asset loading tests with [AssetStorage.loadSync] consuming [Identifier].
 */
class AssetStorageLoadingTestWithIdentifierLoadSync : AbstractAssetStorageLoadingTest() {
  override fun <T> AssetStorage.testLoad(
    path: String,
    type: Class<T>,
    parameters: AssetLoaderParameters<T>?,
  ): T = loadSync(Identifier(path, type), parameters)
}
