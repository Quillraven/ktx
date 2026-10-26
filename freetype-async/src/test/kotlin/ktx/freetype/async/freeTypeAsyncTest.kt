package ktx.freetype.async

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import ktx.assets.async.AssetStorage
import ktx.assets.async.AsyncAssetManager
import ktx.assets.async.Identifier
import ktx.assets.async.MissingAssetException
import ktx.async.AsyncTest
import ktx.async.newAsyncContext
import ktx.freetype.FreeTypeTest
import ktx.freetype.freeTypeFontParameters
import ktx.freetype.registerFreeTypeFontLoaders
import org.mockito.kotlin.mock

/**
 * Tests FreeType font loading utilities for [AssetStorage].
 *
 * Uses Hack font for testing. See https://github.com/source-foundry/Hack for font details.
 *
 * Implementation note: tests use [runBlocking] to simplify the implementation. This is
 * a blocking operation and might permanently block your rendering thread. In an actual
 * application, prefer [ktx.async.KtxAsync].launch to run your coroutines.
 */
class AssetStorageFreeTypeTest : AsyncTest() {
  private val ttfFile = "ktx/freetype/async/hack.ttf"
  private val otfFile = "ktx/freetype/async/hack.otf"

  init {
    beforeSpec {
      LwjglNativesLoader.load()
      Gdx.graphics = mock()
      Gdx.gl20 = mock()
      Gdx.gl = Gdx.gl20
    }

    test("should register FreeType font loaders") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // When:
      storage.registerFreeTypeFontLoaders()

      // Then:
      (storage.getLoader<FreeTypeFontGenerator>() is FreeTypeFontGeneratorLoader) shouldBe true
      (storage.getLoader<BitmapFont>(".ttf") is FreetypeFontLoader) shouldBe true
      (storage.getLoader<BitmapFont>(".otf") is FreetypeFontLoader) shouldBe true
      // Default font loader should not be registered:
      storage.getLoader<BitmapFont>().shouldBeNull()
    }

    test("should register FreeType font loaders with custom extensions") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // When:
      storage.registerFreeTypeFontLoaders(fileExtensions = arrayOf(".custom"))

      // Then:
      (storage.getLoader<FreeTypeFontGenerator>() is FreeTypeFontGeneratorLoader) shouldBe true
      (storage.getLoader<BitmapFont>(".custom") is FreetypeFontLoader) shouldBe true
      // Should not register loader for unlisted extensions:
      storage.getLoader<BitmapFont>(".ttf").shouldBeNull()
      storage.getLoader<BitmapFont>(".otf").shouldBeNull()
      storage.getLoader<BitmapFont>().shouldBeNull()
    }

    test("should register FreeType font loaders with default font loader override") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = true)

      // When:
      storage.registerFreeTypeFontLoaders(replaceDefaultBitmapFontLoader = true)

      // Then:
      (storage.getLoader<FreeTypeFontGenerator>() is FreeTypeFontGeneratorLoader) shouldBe true
      (storage.getLoader<BitmapFont>() is FreetypeFontLoader) shouldBe true
    }

    test("should load OTF file into BitmapFont") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = runBlocking { storage.loadFreeTypeFont(otfFile) }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.get<BitmapFont>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(otfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$otfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load OTF file into BitmapFont asynchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = runBlocking { storage.loadFreeTypeFontAsync(otfFile).await() }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.get<BitmapFont>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(otfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$otfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load OTF file into BitmapFont synchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = storage.loadFreeTypeFontSync(otfFile)

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.get<BitmapFont>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(otfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$otfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load OTF file into BitmapFont with custom params") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        runBlocking {
          storage.loadFreeTypeFont(otfFile) {
            size = 12
            borderWidth = 1f
          }
        }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.get<BitmapFont>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(otfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$otfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load OTF file into BitmapFont with custom params asynchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        runBlocking {
          storage
            .loadFreeTypeFontAsync(otfFile) {
              size = 12
              borderWidth = 1f
            }.await()
        }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.get<BitmapFont>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(otfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$otfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load OTF file into BitmapFont with custom params synchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        storage.loadFreeTypeFontSync(otfFile) {
          size = 12
          borderWidth = 1f
        }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.get<BitmapFont>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(otfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$otfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should unload BitmapFont assets loaded from OTF file") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()
      storage.loadFreeTypeFontSync(otfFile)

      // When:
      runBlocking { storage.unload<BitmapFont>(otfFile) }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe false
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 0
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe false
      storage.getReferenceCount<FreeTypeFontGenerator>("$otfFile.gen") shouldBe 0
    }

    test("should load TTF file into BitmapFont") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = runBlocking { storage.loadFreeTypeFont(ttfFile) }

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.get<BitmapFont>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(ttfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$ttfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load TTF file into BitmapFont asynchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = runBlocking { storage.loadFreeTypeFontAsync(ttfFile).await() }

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.get<BitmapFont>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(ttfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$ttfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load TTF file into BitmapFont synchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = storage.loadFreeTypeFontSync(ttfFile)

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.get<BitmapFont>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(ttfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$ttfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load TTF file into BitmapFont with custom params") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        runBlocking {
          storage.loadFreeTypeFont(ttfFile) {
            size = 12
            borderWidth = 1f
          }
        }

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.get<BitmapFont>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(ttfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$ttfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load TTF file into BitmapFont with custom params asynchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        runBlocking {
          storage
            .loadFreeTypeFontAsync(ttfFile) {
              size = 12
              borderWidth = 1f
            }.await()
        }

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.get<BitmapFont>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(ttfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$ttfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load TTF file into BitmapFont with custom params synchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        storage.loadFreeTypeFontSync(ttfFile) {
          size = 12
          borderWidth = 1f
        }

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.get<BitmapFont>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(ttfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$ttfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should unload BitmapFont assets loaded from TTF file") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()
      storage.loadFreeTypeFontSync(ttfFile)

      // When:
      runBlocking { storage.unload<BitmapFont>(ttfFile) }

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe false
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 0
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe false
      storage.getReferenceCount<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe 0
    }

    test("should use FreeType loader to load OTF file into BitmapFont") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        runBlocking {
          // Note that this method uses "raw" AssetStorage API without font loading utilities.
          // Without the freeTypeFontParameters, this will fail to load.
          storage.load<BitmapFont>(otfFile, parameters = freeTypeFontParameters(otfFile))
        }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.get<BitmapFont>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(otfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$otfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should use FreeType loader to load TTF file into BitmapFont") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset =
        runBlocking {
          // Note that this method uses "raw" AssetStorage API without font loading utilities.
          // Without the freeTypeFontParameters, this will fail to load.
          storage.load<BitmapFont>(ttfFile, parameters = freeTypeFontParameters(ttfFile))
        }

      // Then:
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.get<BitmapFont>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      // Automatically loads a generator for the font:
      storage.getDependencies<BitmapFont>(ttfFile) shouldBe listOf(storage.getIdentifier<FreeTypeFontGenerator>("$ttfFile.gen"))
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.dispose()
    }

    test("should load OTF file into FreeTypeFontGenerator") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = runBlocking { storage.load<FreeTypeFontGenerator>(otfFile) }

      // Then:
      storage.isLoaded<FreeTypeFontGenerator>(otfFile) shouldBe true
      storage.get<FreeTypeFontGenerator>(otfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<FreeTypeFontGenerator>(otfFile) shouldBe 1
      storage.getDependencies<FreeTypeFontGenerator>(otfFile) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should load TTF file into FreeTypeFontGenerator") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()

      // When:
      val asset = runBlocking { storage.load<FreeTypeFontGenerator>(ttfFile) }

      // Then:
      storage.isLoaded<FreeTypeFontGenerator>(ttfFile) shouldBe true
      storage.get<FreeTypeFontGenerator>(ttfFile) shouldBeSameInstanceAs asset
      storage.getReferenceCount<FreeTypeFontGenerator>(ttfFile) shouldBe 1
      storage.getDependencies<FreeTypeFontGenerator>(ttfFile) shouldBe emptyList<Identifier<*>>()

      storage.dispose()
    }

    test("should configure font parameters exactly once") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()
      runBlocking {
        val variable: Int

        // When:
        storage.loadFreeTypeFont(ttfFile) {
          variable = 42
        }

        // Then:
        variable shouldBe 42
      }
    }

    test("should allow to load BitmapFont and FreeTypeFontGenerator assets in parallel") {
      // Given:
      val storage =
        AssetStorage(
          useDefaultLoaders = false,
          fileResolver = ClasspathFileHandleResolver(),
          asyncContext = newAsyncContext(threads = 4),
        )
      storage.registerFreeTypeFontLoaders()

      // When:
      runBlocking {
        val otf = async { storage.loadFreeTypeFont(otfFile) }
        val ttf = async { storage.loadFreeTypeFont(ttfFile) }
        val otfGenerator = async { storage.load<FreeTypeFontGenerator>(otfFile) }
        val ttfGenerator = async { storage.load<FreeTypeFontGenerator>(ttfFile) }

        otf.await()
        ttf.await()
        otfGenerator.await()
        ttfGenerator.await()
      }

      // Then:
      storage.isLoaded<BitmapFont>(otfFile) shouldBe true
      storage.isLoaded<BitmapFont>(ttfFile) shouldBe true
      storage.isLoaded<FreeTypeFontGenerator>(otfFile) shouldBe true
      storage.isLoaded<FreeTypeFontGenerator>(ttfFile) shouldBe true
      storage.isLoaded<FreeTypeFontGenerator>("$otfFile.gen") shouldBe true
      storage.isLoaded<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe true

      storage.getReferenceCount<BitmapFont>(otfFile) shouldBe 1
      storage.getReferenceCount<BitmapFont>(ttfFile) shouldBe 1
      storage.getReferenceCount<FreeTypeFontGenerator>(otfFile) shouldBe 1
      storage.getReferenceCount<FreeTypeFontGenerator>(ttfFile) shouldBe 1
      storage.getReferenceCount<FreeTypeFontGenerator>("$otfFile.gen") shouldBe 1
      storage.getReferenceCount<FreeTypeFontGenerator>("$ttfFile.gen") shouldBe 1

      storage.dispose()
    }

    test("should dispose of multiple font assets without errors") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false, fileResolver = ClasspathFileHandleResolver())
      storage.registerFreeTypeFontLoaders()
      val assets =
        listOf(
          storage.getAssetDescriptor<BitmapFont>(otfFile, parameters = freeTypeFontParameters(otfFile)),
          storage.getAssetDescriptor<BitmapFont>(ttfFile, parameters = freeTypeFontParameters(otfFile)),
          storage.getAssetDescriptor<FreeTypeFontGenerator>(otfFile),
          storage.getAssetDescriptor<FreeTypeFontGenerator>(ttfFile),
        )
      runBlocking {
        assets.forEach {
          storage.load(it)
          storage.isLoaded(it) shouldBe true
        }
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
  }
}

/**
 * Tests FreeType font loading utilities for [AsyncAssetManager].
 *
 * Uses Hack font for testing. See https://github.com/source-foundry/Hack for font details.
 *
 * Implementation note: tests use [runBlocking] to simplify the implementation. This is
 * a blocking operation and might permanently block your rendering thread. In an actual
 * application, prefer [ktx.async.KtxAsync].launch to run your coroutines and obtain
 * [kotlinx.coroutines.Deferred] instances via await.
 */
class AsyncAssetManagerFreeTypeTest : FreeTypeTest() {
  override val ttfFile = "ktx/freetype/async/hack.ttf"
  override val otfFile = "ktx/freetype/async/hack.otf"

  override fun assetManager(): AsyncAssetManager = AsyncAssetManager(ClasspathFileHandleResolver())

  init {
    test("should load OTF file into BitmapFont asynchronously") {
      // Given:
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()

      // When:
      val reference = assetManager.loadFreeTypeFontAsync(otfFile)

      // Then:
      assetManager.finishLoading()
      val asset = runBlocking { reference.await() }
      val font = assetManager.get<BitmapFont>(otfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset
    }

    test("should load OTF file into BitmapFont with custom parameters asynchronously") {
      // Given:
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      val variable: Int

      // When:
      val reference =
        assetManager.loadFreeTypeFontAsync(otfFile) {
          size = 12
          borderWidth = 1f
          variable = 42
        }

      // Then:
      assetManager.finishLoading()
      val asset = runBlocking { reference.await() }
      val font = assetManager.get<BitmapFont>(otfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset
      variable shouldBe 42
    }

    test("should load TTF file into BitmapFont asynchronously") {
      // Given:
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()

      // When:
      val reference = assetManager.loadFreeTypeFontAsync(ttfFile)

      // Then:
      assetManager.finishLoading()
      val asset = runBlocking { reference.await() }
      val font = assetManager.get<BitmapFont>(ttfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset
    }

    test("should load TTF file into BitmapFont with custom parameters asynchronously") {
      // Given:
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      val variable: Int

      // When:
      val reference =
        assetManager.loadFreeTypeFontAsync(ttfFile) {
          size = 12
          borderWidth = 1f
          variable = 42
        }

      // Then:
      assetManager.finishLoading()
      val asset = runBlocking { reference.await() }
      val font = assetManager.get<BitmapFont>(ttfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset
      variable shouldBe 42
    }
  }
}
