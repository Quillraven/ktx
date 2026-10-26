package ktx.assets.async

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.loaders.BitmapFontLoader
import com.badlogic.gdx.assets.loaders.CubemapLoader
import com.badlogic.gdx.assets.loaders.I18NBundleLoader
import com.badlogic.gdx.assets.loaders.ModelLoader
import com.badlogic.gdx.assets.loaders.MusicLoader
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader
import com.badlogic.gdx.assets.loaders.PixmapLoader
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.assets.loaders.SoundLoader
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader
import com.badlogic.gdx.assets.loaders.TextureLoader
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
import com.badlogic.gdx.graphics.g2d.PolygonRegionLoader
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.I18NBundle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import ktx.assets.TextAssetLoader
import ktx.assets.assetDescriptor
import ktx.assets.disposeSafely
import ktx.async.AsyncTest
import org.mockito.kotlin.mock
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect as ParticleEffect3D
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader as ParticleEffect3dLoader

@ExperimentalCoroutinesApi
class AsyncAssetManagerTest : AsyncTest() {
  private val assetManager = AsyncAssetManager(fileResolver = ClasspathFileHandleResolver())

  override fun `setup libGDX application`() {
    super.`setup libGDX application`()
    if (System.getenv("TEST_PROFILE") != "ci") {
      Gdx.audio = OpenALLwjgl3Audio()
    }
  }

  override fun `exit libGDX application`() {
    super.`exit libGDX application`()
    (Gdx.audio as? OpenALLwjgl3Audio)?.dispose()
    assetManager.disposeSafely()
  }

  private inline fun <reified T : Any, reified P> assertCorrectParameters(extension: String = "") {
    val parameter: AssetLoaderParameters<T> = assetManager.getDefaultParameters(assetDescriptor(extension))
    (parameter is P) shouldBe true
  }

  init {
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

    test("should provide appropriate AssetLoaderParameters for each loader type") {
      // Expect:
      assertCorrectParameters<BitmapFont, BitmapFontLoader.BitmapFontParameter>(".fnt")
      assertCorrectParameters<Cubemap, CubemapLoader.CubemapParameter>(".zktx")
      assertCorrectParameters<I18NBundle, I18NBundleLoader.I18NBundleParameter>()
      assertCorrectParameters<Model, ModelLoader.ModelParameters>(".g3dj")
      assertCorrectParameters<Model, ModelLoader.ModelParameters>(".g3db")
      assertCorrectParameters<Music, MusicLoader.MusicParameter>(".mp3")
      assertCorrectParameters<Model, ObjLoader.ObjLoaderParameters>(".obj")
      assertCorrectParameters<ParticleEffect, ParticleEffectLoader.ParticleEffectParameter>(".p2d")
      assertCorrectParameters<ParticleEffect3D, ParticleEffect3dLoader.ParticleEffectLoadParameter>(".p3d")
      assertCorrectParameters<Pixmap, PixmapLoader.PixmapParameter>(".png")
      assertCorrectParameters<PolygonRegion, PolygonRegionLoader.PolygonRegionParameters>(".psh")
      assertCorrectParameters<ShaderProgram, ShaderProgramLoader.ShaderProgramParameter>(".frag")
      assertCorrectParameters<Skin, SkinLoader.SkinParameter>(".json")
      assertCorrectParameters<Sound, SoundLoader.SoundParameter>(".mp3")
      assertCorrectParameters<String, TextAssetLoader.TextAssetLoaderParameters>(".txt")
      assertCorrectParameters<TextureAtlas, TextureAtlasLoader.TextureAtlasParameter>(".atlas")
      assertCorrectParameters<Texture, TextureLoader.TextureParameter>(".png")
    }

    test("should return the same reference to an unloaded asset scheduled for loading multiple times") {
      // Given:
      val path = "ktx/assets/async/string.txt"

      // When:
      val reference = assetManager.loadAsync<String>(path)

      // Then:
      assetManager.loadAsync<String>(path) shouldBeSameInstanceAs reference
      assetManager.finishLoading()
      assetManager.getReferenceCount(path) shouldBe 2
    }

    test("should preserve and call custom LoadedCallback") {
      // Given:
      val path = "ktx/assets/async/string.txt"
      val loaderParameters = TextAssetLoader.TextAssetLoaderParameters()
      val callbackParameters = mutableListOf<Any>()
      val callback =
        AssetLoaderParameters.LoadedCallback { assetManager, fileName, type ->
          callbackParameters.addAll(listOf(assetManager, fileName, type))
        }
      loaderParameters.loadedCallback = callback

      // When:
      val asset = assetManager.loadAsync(path, parameters = loaderParameters)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      loaderParameters.loadedCallback shouldBeSameInstanceAs callback
      callbackParameters shouldBe listOf(assetManager, path, String::class.java)
    }

    test("should rethrow asset loading error") {
      // Given:
      val path = "ktx/assets/async/missing.png"

      // When:
      val asset = assetManager.loadAsync<TextureAtlas>(path)

      // Then:
      assetManager.finishLoading()
      shouldThrow<GdxRuntimeException> {
        runBlocking { asset.await() }
      }
      assetManager.contains(path) shouldBe false
      assetManager.isLoaded(path) shouldBe false
      // Should not throw any exception:
      assetManager.update()
    }

    test("should rethrow dependency loading errors") {
      // Given:
      val path = "ktx/assets/async/corrupted.atlas"
      val dependency = "ktx/assets/async/missing.png"

      // When:
      val asset = assetManager.loadAsync<TextureAtlas>(path)

      // Then:
      assetManager.finishLoading()
      shouldThrow<DependencyLoadingException> {
        runBlocking { asset.await() }
      }
      assetManager.contains(path) shouldBe false
      assetManager.isLoaded(path) shouldBe false
      assetManager.contains(dependency) shouldBe false
      assetManager.isLoaded(dependency) shouldBe false
      // Should not throw any exception:
      assetManager.update()
    }

    test("should load BitmapFont asynchronously") {
      // Given:
      val path = "com/badlogic/gdx/utils/lsans-15.fnt"
      val dependency = "com/badlogic/gdx/utils/lsans-15.png"

      // When:
      val asset = assetManager.loadAsync<BitmapFont>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
      assetManager.isLoaded(dependency) shouldBe true
    }

    test("should load Cubemap asynchronously") {
      // Given:
      val path = "ktx/assets/async/cubemap.zktx"

      // When:
      val asset = assetManager.loadAsync<Cubemap>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load I18NBundle asynchronously") {
      // Given:
      val path = "ktx/assets/async/i18n"

      // When:
      val asset = assetManager.loadAsync<I18NBundle>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load G3DJ Model assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/model.g3dj"

      // When:
      val asset = assetManager.loadAsync<Model>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load G3DB Model assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/model.g3db"

      // When:
      val asset = assetManager.loadAsync<Model>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load OBJ Model assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/model.obj"

      // When:
      val asset = assetManager.loadAsync<Model>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load Music assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/sound.ogg"

      // When:
      val asset = assetManager.loadAsync<Music>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load Sound assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/sound.ogg"

      // When:
      val asset = assetManager.loadAsync<Sound>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load ParticleEffect assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/particle.p2d"

      // When:
      val asset = assetManager.loadAsync<ParticleEffect>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load 3D ParticleEffect assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/particle.p3d"
      val dependency = "ktx/assets/async/texture.png"

      // When:
      val asset = assetManager.loadAsync<ParticleEffect3D>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
      assetManager.isLoaded(dependency) shouldBe true
    }

    test("should load Pixmap assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/texture.png"

      // When:
      val asset = assetManager.loadAsync<Pixmap>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load PolygonRegion assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/polygon.psh"
      val dependency = "ktx/assets/async/polygon.png"

      // When:
      val asset = assetManager.loadAsync<PolygonRegion>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
      assetManager.isLoaded(dependency) shouldBe true
    }

    test("should load ShaderProgram assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/shader.frag"

      // When:
      val asset = assetManager.loadAsync<ShaderProgram>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load Skin assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/skin.json"
      val dependency = "ktx/assets/async/skin.atlas"
      val nestedDependency = "ktx/assets/async/texture.png"

      // When:
      val asset = assetManager.loadAsync<Skin>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
      assetManager.isLoaded(dependency) shouldBe true
      assetManager.isLoaded(nestedDependency) shouldBe true
    }

    test("should load TextureAtlas assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/skin.atlas"
      val dependency = "ktx/assets/async/texture.png"

      // When:
      val asset = assetManager.loadAsync<TextureAtlas>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
      assetManager.isLoaded(dependency) shouldBe true
    }

    test("should load Texture assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/texture.png"

      // When:
      val asset = assetManager.loadAsync<Texture>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }

    test("should load text assets asynchronously") {
      // Given:
      val path = "ktx/assets/async/string.txt"

      // When:
      val asset = assetManager.loadAsync<String>(path)

      // Then:
      assetManager.finishLoading()
      asset.getCompleted().shouldNotBeNull()
      assetManager.get<Any>(path) shouldBeSameInstanceAs asset.getCompleted()
      assetManager.isLoaded(path) shouldBe true
    }
  }
}
