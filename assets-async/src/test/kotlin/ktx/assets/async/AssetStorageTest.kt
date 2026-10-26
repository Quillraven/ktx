package ktx.assets.async

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Logger
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ktx.assets.TextAssetLoader.TextAssetLoaderParameters
import ktx.async.AsyncTest
import ktx.async.KtxAsync
import ktx.async.isOnRenderingThread
import ktx.async.newAsyncContext
import ktx.async.newSingleThreadAsyncContext
import ktx.async.skipFrame
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.lang.Integer.min
import java.util.IdentityHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect as ParticleEffect3D
import com.badlogic.gdx.utils.Array as GdxArray

/**
 * Tests [AssetStorage]: coroutines-based asset manager.
 *
 * This suite tests the behavior and logic of the [AssetStorage]. [AbstractAssetStorageLoadingTest]
 * and its extensions test whether [AssetStorage] can correctly load all default asset types.
 *
 * Implementation note: the tests use [runBlocking] to launch the coroutines for simplicity
 * of asserts. Normally [KtxAsync].launch is highly encouraged to build truly asynchronous applications.
 * Using [runBlocking] on the main rendering thread might lead to deadlocks, as the rendering thread
 * is necessary to load some assets (e.g. textures).
 *
 * In a similar manner, some [AssetDescriptor] instances are created manually. In an actual application,
 * using [AssetStorage.getAssetDescriptor] is a much easier way of obtaining [AssetDescriptor] instances.
 */
class AssetStorageTest : AsyncTest() {
  var testName = ""

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

  /**
   * Allows to test concurrent loading and unloading.
   *
   * During concurrent unloading, some assets are expected to be requested for unloading
   * before they are fully loaded, resulting in [UnloadedAssetException] caught by the
   * coroutines waiting for or loading the asset. This method allows to ignore these exceptions.
   */
  private suspend inline fun <reified T : Any> loadIgnoringUnloadException(
    storage: AssetStorage,
    path: String,
  ) {
    try {
      storage.load<T>(path)
    } catch (exception: UnloadedAssetException) {
      // Expected.
    }
  }

  /** For [Disposable.dispose] interface testing and loaders testing. */
  class FakeAsset : Disposable {
    val disposingFinished = CompletableFuture<Boolean>()
    var isDisposed: Boolean = false

    override fun dispose() {
      isDisposed = true
      disposingFinished.complete(true)
    }
  }

  /** For loaders testing. */
  class FakeAsyncLoader(
    private val onAsync: (assetManager: AssetManager) -> Unit,
    private val onSync: (asset: FakeAsset) -> Unit,
    private val dependencies: GdxArray<AssetDescriptor<*>> = GdxArray.with(),
  ) : AsynchronousAssetLoader<FakeAsset, FakeParameters>(ClasspathFileHandleResolver()) {
    override fun loadAsync(
      manager: AssetManager,
      fileName: String?,
      file: FileHandle?,
      parameter: FakeParameters?,
    ) {
      onAsync(manager)
    }

    override fun loadSync(
      manager: AssetManager,
      fileName: String?,
      file: FileHandle?,
      parameter: FakeParameters?,
    ): FakeAsset = FakeAsset().also(onSync)

    @Suppress("UNCHECKED_CAST")
    override fun getDependencies(
      fileName: String?,
      file: FileHandle?,
      parameter: FakeParameters?,
    ): GdxArray<AssetDescriptor<Any>> = dependencies as GdxArray<AssetDescriptor<Any>>
  }

  /** For loaders testing. */
  open class FakeSyncLoader(
    private val onLoad: (asset: FakeAsset) -> Unit = {},
    private val dependencies: GdxArray<AssetDescriptor<*>> = GdxArray.with(),
  ) : SynchronousAssetLoader<FakeAsset, FakeParameters>(ClasspathFileHandleResolver()) {
    @Suppress("UNCHECKED_CAST")
    override fun getDependencies(
      fileName: String?,
      file: FileHandle?,
      parameter: FakeParameters?,
    ): GdxArray<AssetDescriptor<Any>> = dependencies as GdxArray<AssetDescriptor<Any>>

    override fun load(
      assetManager: AssetManager,
      fileName: String?,
      file: FileHandle?,
      parameter: FakeParameters?,
    ): FakeAsset = FakeAsset().also(onLoad)
  }

  /** For loaders testing. */
  class FakeParameters : AssetLoaderParameters<FakeAsset>()

  init {
    beforeTest { testName = it.name.name }

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

    test("should throw exception when attempting to get unloaded asset") {
      // Given:
      val storage = AssetStorage()

      // When:
      shouldThrow<MissingAssetException> {
        storage.get<String>("ktx/assets/async/string.txt")
      }

      // Then:
      checkProgress(storage, total = 0)
    }

    test("should return null when attempting to get unloaded asset or null") {
      // Given:
      val storage = AssetStorage()

      // When:
      val asset = storage.getOrNull<String>("ktx/assets/async/string.txt")

      // Then:
      asset.shouldBeNull()
      checkProgress(storage, total = 0)
    }

    test("should return deferred that throws exception when attempting to get unloaded asset asynchronously") {
      // Given:
      val storage = AssetStorage()

      // When:
      val result = storage.getAsync<String>("ktx/assets/async/string.txt")

      // Then:
      shouldThrow<MissingAssetException> {
        runBlocking { result.await() }
      }
      checkProgress(storage, total = 0)
    }

    test("should throw exception when attempting to get unloaded asset with identifier") {
      // Given:
      val storage = AssetStorage()
      val identifier = storage.getIdentifier<String>("ktx/assets/async/string.txt")

      // When:
      shouldThrow<MissingAssetException> {
        storage[identifier]
      }

      // Then:
      checkProgress(storage, total = 0)
    }

    test("should return null when attempting to get unloaded asset or null with identifier") {
      // Given:
      val storage = AssetStorage()
      val identifier = storage.getIdentifier<String>("ktx/assets/async/string.txt")

      // When:
      val asset = storage.getOrNull(identifier)

      // Then:
      asset.shouldBeNull()
      checkProgress(storage, total = 0)
    }

    test("should return deferred that throws exception when attempting to get unloaded asset asynchronously with identifier") {
      // Given:
      val storage = AssetStorage()
      val identifier = storage.getIdentifier<String>("ktx/assets/async/string.txt")

      // When:
      val result = storage.getAsync(identifier)

      // Then:
      shouldThrow<MissingAssetException> {
        runBlocking { result.await() }
      }
      checkProgress(storage, total = 0)
    }

    test("should throw exception when attempting to get unloaded asset with descriptor") {
      // Given:
      val storage = AssetStorage()
      val descriptor = storage.getAssetDescriptor<String>("ktx/assets/async/string.txt")

      // When:
      shouldThrow<MissingAssetException> {
        storage[descriptor]
      }

      // Then:
      checkProgress(storage, total = 0)
    }

    test("should return null when attempting to get unloaded asset or null with descriptor") {
      // Given:
      val storage = AssetStorage()
      val descriptor = storage.getAssetDescriptor<String>("ktx/assets/async/string.txt")

      // When:
      val asset = storage.getOrNull(descriptor)

      // Then:
      asset.shouldBeNull()
      checkProgress(storage, total = 0)
    }

    test("should return deferred that throws exception when attempting to get unloaded asset asynchronously with descriptor") {
      // Given:
      val storage = AssetStorage()
      val descriptor = storage.getAssetDescriptor<String>("ktx/assets/async/string.txt")

      // When:
      val result = storage.getAsync(descriptor)

      // Then:
      shouldThrow<MissingAssetException> {
        runBlocking { result.await() }
      }
      checkProgress(storage, total = 0)
    }

    test("should obtain loaded asset with path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"

      // When:
      runBlocking { storage.load<String>(path) }

      // Then:
      storage.contains<String>(path) shouldBe true
      storage.isLoaded<String>(path) shouldBe true
      storage.get<String>(path) shouldBe "Content."
      storage.getOrNull<String>(path) shouldBe "Content."
      (runBlocking { storage.getAsync<String>(path).await() }) shouldBe "Content."
      storage.getDependencies<String>(path) shouldBe emptyList<Identifier<*>>()
      storage.getAssetIdentifiers(path).first() shouldBe storage.getIdentifier<String>(path)
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should obtain loaded asset with identifier") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val identifier = storage.getIdentifier<String>("ktx/assets/async/string.txt")

      // When:
      runBlocking { storage.loadAsync<String>(identifier.path).await() }

      // Then:
      (identifier in storage) shouldBe true
      storage.isLoaded(identifier) shouldBe true
      storage[identifier] shouldBe "Content."
      storage.getOrNull(identifier) shouldBe "Content."
      (runBlocking { storage.getAsync(identifier).await() }) shouldBe "Content."
      storage.getDependencies(identifier) shouldBe emptyList<Identifier<*>>()
      storage.getAssetIdentifiers(identifier.path).first() shouldBe identifier
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should obtain loaded asset with descriptor") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val descriptor = storage.getAssetDescriptor<String>("ktx/assets/async/string.txt")

      // When:
      storage.loadSync<String>(descriptor.fileName)

      // Then:
      (descriptor in storage) shouldBe true
      storage.isLoaded(descriptor) shouldBe true
      storage[descriptor] shouldBe "Content."
      storage.getOrNull(descriptor) shouldBe "Content."
      (runBlocking { storage.getAsync(descriptor).await() }) shouldBe "Content."
      storage.getDependencies(descriptor) shouldBe emptyList<Identifier<*>>()
      storage.getAssetIdentifiers(descriptor.fileName).first() shouldBe descriptor.toIdentifier()
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should unload assets with path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      runBlocking { storage.load<String>(path) }

      // When:
      runBlocking { storage.unload<String>(path) }

      // Then:
      storage.isLoaded<String>(path) shouldBe false
      storage.getReferenceCount<String>(path) shouldBe 0
      storage.getAssetIdentifiers(path) shouldBe emptyList<Identifier<*>>()
      checkProgress(storage, total = 0, warn = true)
    }

    test("should unload assets with descriptor") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val descriptor = storage.getAssetDescriptor<String>(path)
      runBlocking { storage.loadAsync<String>(path).await() }

      // When:
      runBlocking { storage.unload(descriptor) }

      // Then:
      storage.isLoaded(descriptor) shouldBe false
      storage.getReferenceCount(descriptor) shouldBe 0
      storage.getAssetIdentifiers(path) shouldBe emptyList<Identifier<*>>()
      checkProgress(storage, total = 0, warn = true)
    }

    test("should unload assets with identifier") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val identifier = storage.getIdentifier<String>(path)
      storage.loadSync<String>(path)

      // When:
      runBlocking { storage.unload(identifier) }

      // Then:
      storage.isLoaded(identifier) shouldBe false
      storage.getReferenceCount(identifier) shouldBe 0
      storage.getAssetIdentifiers(path) shouldBe emptyList<Identifier<*>>()
      checkProgress(storage, total = 0, warn = true)
    }

    test("should differentiate assets by path and type") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // When:
      runBlocking { storage.add("path", "ASSET") }

      // Then:
      storage.contains<String>("path") shouldBe true
      storage.contains<String>("different path") shouldBe false
      storage.contains<Int>("path") shouldBe false // Different type.
    }

    test("should point to the same asset when loading with path, descriptor and identifier") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val descriptor = storage.getAssetDescriptor<String>(path)
      val identifier = storage.getIdentifier<String>(path)

      // When:
      val viaPath = runBlocking { storage.load<String>(path) }
      val viaDescriptor = runBlocking { storage.load(descriptor) }
      val viaIdentifier = runBlocking { storage.load(identifier) }

      // Then:
      storage.isLoaded<String>(path) shouldBe true
      viaDescriptor shouldBeSameInstanceAs viaPath
      viaIdentifier shouldBeSameInstanceAs viaDescriptor
      storage.getReferenceCount<String>(path) shouldBe 3
      storage.getAssetIdentifiers(path).first() shouldBe identifier
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should point to the same asset when loading with path, descriptor and identifier asynchronously") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val descriptor = storage.getAssetDescriptor<String>(path)
      val identifier = storage.getIdentifier<String>(path)
      val pathReference = storage.loadAsync<String>(path)
      val descriptorReference = storage.loadAsync(descriptor)
      val identifierReference = storage.loadAsync(identifier)

      // When:
      val viaPath = runBlocking { pathReference.await() }
      val viaDescriptor = runBlocking { descriptorReference.await() }
      val viaIdentifier = runBlocking { identifierReference.await() }

      // Then:
      storage.isLoaded<String>(path) shouldBe true
      viaDescriptor shouldBeSameInstanceAs viaPath
      viaIdentifier shouldBeSameInstanceAs viaDescriptor
      storage.getReferenceCount<String>(path) shouldBe 3
      storage.getAssetIdentifiers(path).first() shouldBe identifier
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should point to the same asset when loading with path, descriptor and identifier synchronously") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val descriptor = storage.getAssetDescriptor<String>(path)
      val identifier = storage.getIdentifier<String>(path)

      // When:
      val viaPath = storage.loadSync<String>(path)
      val viaDescriptor = storage.loadSync(descriptor)
      val viaIdentifier = storage.loadSync(identifier)

      // Then:
      storage.isLoaded<String>(path) shouldBe true
      viaDescriptor shouldBeSameInstanceAs viaPath
      viaIdentifier shouldBeSameInstanceAs viaDescriptor
      storage.getReferenceCount<String>(path) shouldBe 3
      storage.getAssetIdentifiers(path).first() shouldBe identifier
      checkProgress(storage, loaded = 1, warn = true)
    }

    test("should manually add asset to storage") {
      // Given:
      val storage = AssetStorage()
      val asset = Vector2(20f, 10f)
      val fakePath = "myVector"

      // When:
      runBlocking { storage.add(fakePath, asset) }

      // Then:
      storage.isLoaded<Vector2>(fakePath) shouldBe true
      storage.get<Vector2>(fakePath) shouldBeSameInstanceAs asset
      storage.getReferenceCount<Vector2>(fakePath) shouldBe 1
      storage.getAssetIdentifiers(fakePath).first() shouldBe storage.getIdentifier<Vector2>(fakePath)
      checkProgress(storage, loaded = 1)
    }

    test("should manually add asset to storage with descriptor") {
      // Given:
      val storage = AssetStorage()
      val asset = Vector2(20f, 10f)
      val fakePath = "myVector"
      val descriptor = storage.getAssetDescriptor<Vector2>(fakePath)

      // When:
      runBlocking { storage.add(descriptor, asset) }

      // Then:
      storage.isLoaded(descriptor) shouldBe true
      storage[descriptor] shouldBeSameInstanceAs asset
      storage.getReferenceCount(descriptor) shouldBe 1
      storage.getAssetIdentifiers(fakePath).first() shouldBe descriptor.toIdentifier()
      checkProgress(storage, loaded = 1)
    }

    test("should manually add asset to storage with identifier") {
      // Given:
      val storage = AssetStorage()
      val asset = Vector2(20f, 10f)
      val fakePath = "myVector"
      val identifier = storage.getIdentifier<Vector2>(fakePath)

      // When:
      runBlocking { storage.add(identifier, asset) }

      // Then:
      storage.isLoaded(identifier) shouldBe true
      storage[identifier] shouldBeSameInstanceAs asset
      storage.getReferenceCount(identifier) shouldBe 1
      storage.getAssetIdentifiers(fakePath).first() shouldBe identifier
      checkProgress(storage, loaded = 1)
    }

    test("should unload and dispose assets manually added to storage") {
      // Given:
      val storage = AssetStorage()
      val asset = FakeAsset()
      val fakePath = "disposable"
      runBlocking { storage.add(fakePath, asset) }

      // When:
      val unloaded = runBlocking { storage.unload<FakeAsset>(fakePath) }

      // Then:
      unloaded shouldBe true
      storage.isLoaded<FakeAsset>(fakePath) shouldBe false
      storage.getReferenceCount<FakeAsset>(fakePath) shouldBe 0
      asset.isDisposed shouldBe true
      storage.getAssetIdentifiers(fakePath) shouldBe emptyList<Identifier<*>>()
      checkProgress(storage, total = 0, warn = true)
    }

    test("should add asset after it was unloaded") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"
      val asset = mock<Texture>()
      runBlocking { storage.load<Texture>(path) }
      runBlocking { storage.unload<Texture>(path) }

      // When:
      runBlocking { storage.add(path, asset) }

      // Then:
      storage.isLoaded<Texture>(path) shouldBe true
      storage.get<Texture>(path) shouldBeSameInstanceAs asset
    }

    test("should allow to add asset with different type than loaded") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"
      val asset = mock<Pixmap>()
      runBlocking { storage.load<Texture>(path) }

      // When:
      runBlocking { storage.add(path, asset) }

      // Then:
      storage.isLoaded<Texture>(path) shouldBe true
      storage.isLoaded<Pixmap>(path) shouldBe true
      storage.get<Pixmap>(path) shouldBeSameInstanceAs asset
      storage.getAssetIdentifiers(path).toSet() shouldBe setOf(storage.getIdentifier<Pixmap>(path), storage.getIdentifier<Texture>(path))

      storage.dispose()
    }

    test("should allow to load multiple assets with different type and same path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/texture.png"

      // When:
      runBlocking {
        storage.load<Texture>(path)
        storage.load<Pixmap>(path)
      }

      // Then:
      storage.isLoaded<Texture>(path) shouldBe true
      storage.isLoaded<Pixmap>(path) shouldBe true
      storage.getReferenceCount<Texture>(path) shouldBe 1
      storage.getReferenceCount<Pixmap>(path) shouldBe 1
      (storage.get<Pixmap>(path) as Any) shouldNotBeSameInstanceAs storage.get<Texture>(path)
      storage.getAssetIdentifiers(path).toSet() shouldBe setOf(storage.getIdentifier<Pixmap>(path), storage.getIdentifier<Texture>(path))
      checkProgress(storage, loaded = 2, warn = true)

      storage.dispose()
    }

    test("should support loading assets in parallel") {
      // Given:
      val storage =
        AssetStorage(
          fileResolver = ClasspathFileHandleResolver(),
          asyncContext = newAsyncContext(2),
        )
      val firstPath = "ktx/assets/async/texture.png"
      val secondPath = "ktx/assets/async/model.obj"
      val scheduler = newAsyncContext(2)
      val tasksReference = CompletableFuture<List<Deferred<*>>>()

      // When:
      KtxAsync.launch {
        val first = async(scheduler) { storage.load<Texture>(firstPath) }
        val second = async(scheduler) { storage.load<Model>(secondPath) }
        tasksReference.complete(listOf(first, second))
      }

      // Then:
      val tasks = tasksReference.join()
      runBlocking { tasks.joinAll() }
      storage.isLoaded<Texture>(firstPath) shouldBe true
      storage.isLoaded<Model>(secondPath) shouldBe true
      storage.get<Texture>(firstPath) shouldBeSameInstanceAs tasks[0].asCompletableFuture().join()
      storage.get<Model>(secondPath) shouldBeSameInstanceAs tasks[1].asCompletableFuture().join()
      checkProgress(storage, loaded = 2, warn = true)

      storage.dispose()
    }

    test("should fail to add asset that was already loaded") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      runBlocking { storage.load<String>(path) }

      // When:
      shouldThrow<AlreadyLoadedAssetException> {
        runBlocking {
          storage.add(path, "ASSET")
        }
      }

      // Then:
      storage.getReferenceCount<String>(path) shouldBe 1
    }

    test("should fail to add asset that was already added") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      runBlocking { storage.add(path, "ASSET") }

      // When:
      shouldThrow<AlreadyLoadedAssetException> {
        runBlocking {
          // Even though the asset is the same, `add` should work only once.
          // This is because finding all dependencies of an asset is tricky
          // if it is both loaded and added. To keep the dependencies reference
          // counts in check, we're only allowing to add an asset once.
          storage.add(path, "ASSET")
        }
      }

      // Then:
      storage.getReferenceCount<String>(path) shouldBe 1
    }

    test("should increase references count and return the same asset when trying to load asset with same path") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val elements = IdentityHashMap<String, Boolean>()

      // When:
      runBlocking {
        repeat(3) {
          val asset = storage.load<String>(path)
          elements[asset] = true
        }
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
        runBlocking { storage.load<Vector2>(path) }
      }

      // Then:
      storage.contains<Vector2>(path) shouldBe false
      checkProgress(storage, total = 0)
    }

    test("should register default loaders") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = true)

      // Expect:
      storage.getLoader<String>().shouldNotBeNull()
      storage.getLoader<BitmapFont>().shouldNotBeNull()
      storage.getLoader<Music>().shouldNotBeNull()
      storage.getLoader<Pixmap>().shouldNotBeNull()
      storage.getLoader<Sound>().shouldNotBeNull()
      storage.getLoader<TextureAtlas>().shouldNotBeNull()
      storage.getLoader<Texture>().shouldNotBeNull()
      storage.getLoader<Skin>().shouldNotBeNull()
      storage.getLoader<ParticleEffect>().shouldNotBeNull()
      storage.getLoader<ParticleEffect3D>().shouldNotBeNull()
      storage.getLoader<I18NBundle>().shouldNotBeNull()
      storage.getLoader<Model>(path = ".obj").shouldNotBeNull()
      storage.getLoader<Model>(path = ".g3dj").shouldNotBeNull()
      storage.getLoader<Model>(path = ".g3db").shouldNotBeNull()
      storage.getLoader<ShaderProgram>().shouldNotBeNull()
      storage.getLoader<Cubemap>().shouldNotBeNull()
    }

    test("should not register default loaders") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // Expect:
      storage.getLoader<String>().shouldBeNull()
      storage.getLoader<BitmapFont>().shouldBeNull()
      storage.getLoader<Music>().shouldBeNull()
      storage.getLoader<Pixmap>().shouldBeNull()
      storage.getLoader<Sound>().shouldBeNull()
      storage.getLoader<TextureAtlas>().shouldBeNull()
      storage.getLoader<Texture>().shouldBeNull()
      storage.getLoader<Skin>().shouldBeNull()
      storage.getLoader<ParticleEffect>().shouldBeNull()
      storage.getLoader<ParticleEffect3D>().shouldBeNull()
      storage.getLoader<I18NBundle>().shouldBeNull()
      storage.getLoader<Model>(path = ".obj").shouldBeNull()
      storage.getLoader<Model>(path = ".g3dj").shouldBeNull()
      storage.getLoader<Model>(path = ".g3db").shouldBeNull()
      storage.getLoader<ShaderProgram>().shouldBeNull()
      storage.getLoader<Cubemap>().shouldBeNull()
    }

    test("should increase references counts of dependencies when loading asset with same path") {
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
      runBlocking {
        repeat(3) {
          val asset = storage.load<Skin>(path)
          loadedAssets[asset] = true
        }
      }

      // Then:
      storage.getReferenceCount<Skin>(path) shouldBe 3
      dependencies.forEach {
        storage.getReferenceCount(it) shouldBe 3
      }
      loadedAssets.size shouldBe 1
      checkProgress(storage, loaded = 3, warn = true)
    }

    test("should increase references counts of dependencies when loading asset with same descriptor") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val descriptor = storage.getAssetDescriptor<Skin>("ktx/assets/async/skin.json")
      val dependencies =
        arrayOf(
          storage.getIdentifier<TextureAtlas>("ktx/assets/async/skin.atlas"),
          storage.getIdentifier<Texture>("ktx/assets/async/texture.png"),
        )
      val loadedAssets = IdentityHashMap<Skin, Boolean>()

      // When:
      runBlocking {
        repeat(3) {
          val asset = storage.loadAsync(descriptor).await()
          loadedAssets[asset] = true
        }
      }

      // Then:
      storage.getReferenceCount(descriptor) shouldBe 3
      dependencies.forEach {
        storage.getReferenceCount(it) shouldBe 3
      }
      loadedAssets.size shouldBe 1
      checkProgress(storage, loaded = 3, warn = true)

      storage.dispose()
    }

    test("should increase references counts of dependencies when loading asset with same identifier") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val identifier = storage.getIdentifier<Skin>("ktx/assets/async/skin.json")
      val dependencies =
        arrayOf(
          storage.getIdentifier<TextureAtlas>("ktx/assets/async/skin.atlas"),
          storage.getIdentifier<Texture>("ktx/assets/async/texture.png"),
        )
      val loadedAssets = IdentityHashMap<Skin, Boolean>()

      // When:
      repeat(3) {
        val asset = storage.loadSync(identifier)
        loadedAssets[asset] = true
      }

      // Then:
      storage.getReferenceCount(identifier) shouldBe 3
      dependencies.forEach {
        storage.getReferenceCount(it) shouldBe 3
      }
      loadedAssets.size shouldBe 1
      checkProgress(storage, loaded = 3, warn = true)

      storage.dispose()
    }

    test("should not unload dependencies that still referenced by other assets") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/skin.json"
      val dependency = "ktx/assets/async/skin.atlas"
      val nestedDependency = "ktx/assets/async/texture.png"
      runBlocking {
        storage.load<Skin>(path)
        storage.load<TextureAtlas>(dependency)
      }

      // When:
      runBlocking { storage.unload<Skin>(path) }

      // Then:
      storage.getReferenceCount<Skin>(path) shouldBe 0
      storage.isLoaded<Skin>(path) shouldBe false
      storage.getReferenceCount<TextureAtlas>(dependency) shouldBe 1
      storage.isLoaded<TextureAtlas>(dependency) shouldBe true
      storage.getReferenceCount<Texture>(nestedDependency) shouldBe 1
      storage.isLoaded<Texture>(nestedDependency) shouldBe true
      checkProgress(storage, loaded = 2, warn = true)

      storage.dispose()
    }

    test("should support concurrent loading") {
      // Given:
      val schedulers = newAsyncContext(threads = 16)
      val loaders = newAsyncContext(threads = 4)
      val storage =
        AssetStorage(
          fileResolver = ClasspathFileHandleResolver(),
          asyncContext = loaders,
        )
      val path = "com/badlogic/gdx/utils/lsans-15.fnt"
      val dependency = "com/badlogic/gdx/utils/lsans-15.png"

      // When:
      val assets =
        (1..100).map {
          val result = CompletableDeferred<BitmapFont>()
          KtxAsync.launch(schedulers) {
            val asset = storage.load<BitmapFont>(path)
            result.complete(asset)
          }
          result
        }

      // Then:
      runBlocking { assets.joinAll() }
      storage.isLoaded<BitmapFont>(path) shouldBe true
      storage.getReferenceCount<BitmapFont>(path) shouldBe 100
      storage.isLoaded<Texture>(dependency) shouldBe true
      storage.getReferenceCount<Texture>(dependency) shouldBe 100
      (assets.map { it.asCompletableFuture().join() }.toSet().size) shouldBe 1
      checkProgress(storage, loaded = 2, warn = true)

      storage.dispose()
    }

    test("should support concurrent loading and unloading") {
      // Given:
      val schedulers = newAsyncContext(threads = 16)
      val loaders = newAsyncContext(threads = 4)
      val storage =
        AssetStorage(
          fileResolver = ClasspathFileHandleResolver(),
          asyncContext = loaders,
        )
      val path = "ktx/assets/async/string.txt"

      // When: spawning 100 coroutines that load and unload the asset, 1 of which loads it 2 times:
      val assets =
        (1..100).map { id ->
          val result = CompletableDeferred<Boolean>()
          KtxAsync.launch(schedulers) {
            loadIgnoringUnloadException<String>(storage, path)
            storage.unload<String>(path)
            if (id == 99) {
              // Loading 1 additional asset:
              loadIgnoringUnloadException<String>(storage, path)
            }
            result.complete(true)
          }
          result
        }

      // Then:
      runBlocking { assets.joinAll() }
      storage.isLoaded<String>(path) shouldBe true
      storage.getReferenceCount<String>(path) shouldBe 1
      storage.get<String>(path) shouldBe "Content."
      checkProgress(storage, loaded = 1, warn = true)

      storage.dispose()
    }

    test("should support concurrent loading and unloading with dependencies") {
      // Given:
      val schedulers = newAsyncContext(threads = 16)
      val loaders = newAsyncContext(threads = 4)
      val storage =
        AssetStorage(
          fileResolver = ClasspathFileHandleResolver(),
          asyncContext = loaders,
        )
      val path = "com/badlogic/gdx/utils/lsans-15.fnt"
      val dependency = "com/badlogic/gdx/utils/lsans-15.png"

      // When: spawning 100 coroutines that load and unload the asset, 1 of which loads it 2 times:
      val assets =
        (1..100).map { id ->
          val result = CompletableDeferred<Boolean>()
          KtxAsync.launch(schedulers) {
            loadIgnoringUnloadException<BitmapFont>(storage, path)
            storage.unload<BitmapFont>(path)
            if (id == 1) {
              // Loading 1 additional asset:
              loadIgnoringUnloadException<BitmapFont>(storage, path)
            }
            result.complete(true)
          }
          result
        }

      // Then:
      runBlocking { assets.joinAll() }
      storage.isLoaded<BitmapFont>(path) shouldBe true
      storage.getReferenceCount<BitmapFont>(path) shouldBe 1
      storage.isLoaded<Texture>(dependency) shouldBe true
      storage.getReferenceCount<Texture>(dependency) shouldBe 1
      storage.get<Texture>(dependency) shouldBeSameInstanceAs storage.get<BitmapFont>(path).region.texture
      checkProgress(storage, loaded = 2, warn = true)

      storage.dispose()
    }

    test("should support concurrent loading and unloading with deeply nested dependencies") {
      // Given:
      val schedulers = newAsyncContext(threads = 16)
      val loaders = newAsyncContext(threads = 4)
      val storage =
        AssetStorage(
          fileResolver = ClasspathFileHandleResolver(),
          asyncContext = loaders,
        )
      val path = "ktx/assets/async/skin.json"
      val dependency = "ktx/assets/async/skin.atlas"
      val nestedDependency = "ktx/assets/async/texture.png"

      // When: spawning 100 coroutines that load and unload the asset, 1 of which loads it 2 times:
      val assets =
        (1..100).map { id ->
          val result = CompletableDeferred<Boolean>()
          KtxAsync.launch(schedulers) {
            loadIgnoringUnloadException<Skin>(storage, path)
            storage.unload<Skin>(path)
            if (id == 1) {
              loadIgnoringUnloadException<Skin>(storage, path)
            }
            result.complete(true)
          }
          result
        }

      // Then:
      runBlocking { assets.joinAll() }
      storage.isLoaded<Skin>(path) shouldBe true
      storage.getReferenceCount<Skin>(path) shouldBe 1
      storage.isLoaded<TextureAtlas>(dependency) shouldBe true
      storage.getReferenceCount<TextureAtlas>(dependency) shouldBe 1
      storage.isLoaded<Texture>(nestedDependency) shouldBe true
      storage.getReferenceCount<Texture>(nestedDependency) shouldBe 1
      val skin = storage.get<Skin>(path)
      val atlas = storage.get<TextureAtlas>(dependency)
      val texture = storage.get<Texture>(nestedDependency)
      atlas shouldBeSameInstanceAs skin.atlas
      texture shouldBeSameInstanceAs atlas.textures.first()
      checkProgress(storage, loaded = 3, warn = true)

      storage.dispose()
    }

    test("should handle stress test") {
      // Given:
      val schedulers = newAsyncContext(threads = 16)
      val loaders = newAsyncContext(threads = 4)
      val storage =
        AssetStorage(
          fileResolver = ClasspathFileHandleResolver(),
          asyncContext = loaders,
        )
      val path = "ktx/assets/async/skin.json"
      val dependency = "ktx/assets/async/skin.atlas"
      val nestedDependency = "ktx/assets/async/texture.png"
      val loads = AtomicInteger()
      val unloads = AtomicInteger()

      // When: spawning 1000 coroutines that randomly load or unload the asset and try to access it:
      val assets =
        (1..1000).map {
          val result = CompletableDeferred<Boolean>()
          KtxAsync.launch(schedulers) {
            repeat(ThreadLocalRandom.current().nextInt(50)) { skipFrame() }
            if (ThreadLocalRandom.current().nextFloat() < 0.4f) {
              // Some unloads are expected to miss and loads are longer,
              // so there's a lower probability for load.
              loads.incrementAndGet()
              loadIgnoringUnloadException<Skin>(storage, path)
            } else {
              val unloaded = storage.unload<Skin>(path)
              if (unloaded) unloads.incrementAndGet()
            }
            try {
              // Concurrent access:
              storage.getOrNull<Skin>(path)
            } catch (expected: UnloadedAssetException) {
              // Assets can be unloaded asynchronously. This is OK.
            }
            result.complete(true)
          }
          result
        }

      // Then:
      runBlocking { assets.joinAll() }
      val expectedReferences = max(0, loads.get() - unloads.get())
      storage.getReferenceCount<Skin>(path) shouldBe expectedReferences
      storage.getReferenceCount<TextureAtlas>(dependency) shouldBe expectedReferences
      storage.getReferenceCount<Texture>(nestedDependency) shouldBe expectedReferences
      // Either the skin is unloaded or there are 3 assets - skin, atlas, texture:
      val assetsCount = min(1, expectedReferences) * 3
      checkProgress(storage, loaded = assetsCount, warn = true)

      storage.dispose()
    }

    test("should register asset loader") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val loader = mock<SynchronousLoader<Int>>()

      // When:
      storage.setLoader { loader }

      // Then:
      storage.getLoader<Int>() shouldBeSameInstanceAs loader

      // Implementation note: normally you create a new instance in the setLoader lambda,
      // this is just for verification.
    }

    test("should register asset loader with suffix") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val loader = mock<SynchronousLoader<Int>>()

      // When:
      storage.setLoader(suffix = ".txt") { loader }

      // Then:
      storage.getLoader<Int>("file.txt") shouldBeSameInstanceAs loader
      storage.getLoader<Int>("file.md").shouldBeNull()
    }

    test("should return null if asset loader is not available") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // Expect:
      storage.getLoader<Int>().shouldBeNull()
    }

    test("should reject invalid asset loader implementations") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // When: loader does not extend Synchronous/AsynchronousAssetLoader:
      val invalidLoader = mock<Loader<Int>>()

      // Then:
      shouldThrow<InvalidLoaderException> {
        storage.setLoader { invalidLoader }
      }
    }

    test("should dispose of all assets") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val paths = (1..5).map { storage.getIdentifier<FakeAsset>(it.toString()) }
      val assets =
        paths
          .map { it to FakeAsset() }
          .onEach { (identifier, asset) -> runBlocking { storage.add(identifier, asset) } }
          .map { (_, asset) -> asset }

      // When:
      storage.dispose()

      // Then:
      (assets.all { it.isDisposed }) shouldBe true
      (paths.all { it !in storage }) shouldBe true
      checkProgress(storage, total = 0)
    }

    test("should dispose of all assets with optional error handling") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val paths = (1..5).map { storage.getIdentifier<FakeAsset>(it.toString()) }
      val assets =
        paths
          .map { it to FakeAsset() }
          .onEach { (identifier, asset) -> runBlocking { storage.add(identifier, asset) } }
          .map { (_, asset) -> asset }

      // When:
      runBlocking {
        storage.dispose { identifier, cause ->
          fail("Unexpected exception when unloading $identifier: $cause")
          throw cause
        }
      }

      // Then:
      (assets.all { it.isDisposed }) shouldBe true
      (paths.all { it !in storage }) shouldBe true
    }

    test("should catch errors during disposing with handler") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val validAsset = mock<Disposable>()
      val brokenAsset =
        mock<Disposable> {
          on(it.dispose()) doThrow GdxRuntimeException("Expected.")
        }
      runBlocking {
        storage.add("broken", brokenAsset)
        storage.add("valid", validAsset)
      }

      // When:
      runBlocking {
        storage.dispose { identifier, error ->
          identifier shouldBe storage.getIdentifier<Disposable>("broken")
          error.message shouldBe "Expected."
          (error is GdxRuntimeException) shouldBe true
        }
      }

      // Then:
      verify(brokenAsset).dispose()
      verify(validAsset).dispose()
    }

    test("should log errors during disposing") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val logger = mock<Logger>()
      storage.logger = logger
      val validAsset = mock<Disposable>()
      val exception = GdxRuntimeException("Expected.")
      val brokenAsset =
        mock<Disposable> {
          on(it.dispose()) doThrow exception
        }
      runBlocking {
        storage.add("broken", brokenAsset)
        storage.add("valid", validAsset)
      }

      // When:
      storage.dispose()

      // Then:
      verify(brokenAsset).dispose()
      verify(validAsset).dispose()
      verify(logger).error(any(), eq(exception))
    }

    test("should create AssetDescriptor") {
      // When:
      val storage = AssetStorage(useDefaultLoaders = false)
      val descriptor = storage.getAssetDescriptor<String>("ktx/assets/async/string.txt")

      // Then:
      descriptor.fileName shouldBe "ktx/assets/async/string.txt"
      descriptor.file.path() shouldBe "ktx/assets/async/string.txt"
      descriptor.type shouldBe String::class.java
      descriptor.params.shouldBeNull()
    }

    test("should create AssetDescriptor with loading parameters") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val parameters = mock<AssetLoaderParameters<String>>()

      // When:
      val descriptor = storage.getAssetDescriptor("ktx/assets/async/string.txt", parameters)

      // Then:
      descriptor.fileName shouldBe "ktx/assets/async/string.txt"
      descriptor.file.path() shouldBe "ktx/assets/async/string.txt"
      descriptor.type shouldBe String::class.java
      descriptor.params shouldBeSameInstanceAs parameters
    }

    test("should create AssetDescriptor with a custom file") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val file = mock<FileHandle>()

      // When:
      val descriptor = storage.getAssetDescriptor<String>("ktx/assets/async/string.txt", fileHandle = file)

      // Then:
      descriptor.fileName shouldBe "ktx/assets/async/string.txt"
      descriptor.type shouldBe String::class.java
      descriptor.file shouldBeSameInstanceAs file
    }

    test("should create Identifier") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "my\\file.png"

      // When:
      val identifier = storage.getIdentifier<Vector2>(path)

      // Then: should normalize path and extract reified type:
      identifier.path shouldBe "my/file.png"
      identifier.type shouldBeSameInstanceAs Vector2::class.java
    }

    test("should normalize file paths") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val paths =
        mapOf(
          "path.txt" to "path.txt",
          "dir/path.txt" to "dir/path.txt",
          "\\path.txt" to "/path.txt",
          "dir\\path.txt" to "dir/path.txt",
          "home\\dir\\path.txt" to "home/dir/path.txt",
          "\\home\\dir\\dir\\" to "/home/dir/dir/",
        )

      paths.forEach { (original, expected) ->
        // When:
        val normalized = with(storage) { original.normalizePath() }

        // Then: Should match AssetDescriptor logic.
        normalized shouldBe expected
        normalized shouldBe AssetDescriptor(expected, String::class.java).fileName
      }
    }

    test("should convert AssetDescriptor to Identifier") {
      // Given:
      val assetDescriptor = AssetDescriptor("file.path", String::class.java, TextAssetLoaderParameters("UTF-8"))

      // When:
      val identifier = assetDescriptor.toIdentifier()

      // Then: should copy path and class without loading parameters:
      identifier.path shouldBe assetDescriptor.fileName
      identifier.type shouldBe assetDescriptor.type
    }

    test("should convert Identifier to AssetDescriptor") {
      // Given:
      val identifier = Identifier("file.path", String::class.java)

      // When:
      val assetDescriptor = identifier.toAssetDescriptor()

      // Then:
      assetDescriptor.fileName shouldBe "file.path"
      assetDescriptor.type shouldBeSameInstanceAs String::class.java
    }

    test("should convert Identifier to AssetDescriptor with loading parameters") {
      // Given:
      val identifier = Identifier("file.path", String::class.java)
      val parameters = mock<AssetLoaderParameters<String>>()

      // When:
      val assetDescriptor = identifier.toAssetDescriptor(parameters)

      // Then:
      assetDescriptor.fileName shouldBe "file.path"
      assetDescriptor.type shouldBeSameInstanceAs String::class.java
      assetDescriptor.params shouldBeSameInstanceAs parameters
    }

    test("should convert Identifier to AssetDescriptor with a custom file") {
      // Given:
      val identifier = Identifier("file.path", String::class.java)
      val file = mock<FileHandle>()

      // When:
      val assetDescriptor = identifier.toAssetDescriptor(fileHandle = file)

      // Then:
      assetDescriptor.fileName shouldBe "file.path"
      assetDescriptor.type shouldBeSameInstanceAs String::class.java
      assetDescriptor.file shouldBeSameInstanceAs file
    }

    test("should invoke loaded callback on rendering thread to match AssetManager behavior") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val callbackFinished = CompletableFuture<Boolean>()
      val callbackExecutions = AtomicInteger()
      var callbackExecutedOnRenderingThread = false
      lateinit var callbackManager: AssetManager
      var callbackPath = ""
      var callbackType = Any::class.java
      val parameters =
        TextAssetLoaderParameters().apply {
          loadedCallback =
            AssetLoaderParameters.LoadedCallback { assetManager, fileName, type ->
              callbackExecutions.incrementAndGet()
              callbackExecutedOnRenderingThread = KtxAsync.isOnRenderingThread()
              callbackManager = assetManager
              callbackPath = fileName
              callbackType = type
              callbackFinished.complete(true)
            }
        }

      // When:
      runBlocking { storage.load(path, parameters) }

      // Then:
      callbackFinished.join()
      callbackExecutions.get() shouldBe 1
      callbackExecutedOnRenderingThread shouldBe true
      (callbackManager is AssetManagerWrapper) shouldBe true
      (callbackManager as AssetManagerWrapper).assetStorage shouldBeSameInstanceAs storage
      callbackPath shouldBe path
      callbackType shouldBeSameInstanceAs String::class.java
    }

    test("should log exceptions thrown by loading callbacks") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"
      val loggingFinished = CompletableFuture<Boolean>()
      val exception = IllegalStateException("Expected.")
      val logger =
        mock<Logger> {
          on(it.error(any(), any())) doAnswer {
            loggingFinished.complete(true)
            Unit
          }
        }
      storage.logger = logger
      val parameters =
        TextAssetLoaderParameters().apply {
          loadedCallback =
            AssetLoaderParameters.LoadedCallback { _, _, _ ->
              throw exception
            }
        }

      // When:
      runBlocking { storage.load(path, parameters) }

      // Then: asset should still be loaded, but the callback exception must be logged:
      loggingFinished.join()
      storage.isLoaded<String>(path) shouldBe true
      storage.get<String>(path) shouldBe "Content."
      verify(logger).error(any(), eq(exception))
    }

    test("should not block the main rendering thread when loading assets synchronously") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/string.txt"

      // When:
      val asset =
        runBlocking(KtxAsync.coroutineContext) {
          storage.loadSync<String>(path)
        }

      // Then:
      asset shouldBe "Content."
      storage.isLoaded<String>(path) shouldBe true
    }

    test("should not block the main rendering thread when loading assets with dependencies synchronously") {
      // Given:
      val storage = AssetStorage(fileResolver = ClasspathFileHandleResolver())
      val path = "ktx/assets/async/skin.json"
      val dependencies =
        arrayOf(
          storage.getIdentifier<TextureAtlas>("ktx/assets/async/skin.atlas"),
          storage.getIdentifier<Texture>("ktx/assets/async/texture.png"),
        )

      // When:
      val asset =
        runBlocking(KtxAsync.coroutineContext) {
          storage.loadSync<Skin>(path)
        }

      // Then:
      storage.isLoaded<Skin>(path) shouldBe true
      dependencies.forEach {
        storage.isLoaded(it) shouldBe true
      }
      storage[dependencies[0]] shouldBeSameInstanceAs asset.atlas
    }

    test("should execute asynchronous loading on specified context and synchronous loading on rendering thread") {
      // Given:
      val asyncContext = newSingleThreadAsyncContext(threadName = "CustomThreadName")
      val asyncThread = getExecutionThread(asyncContext.executor)
      val isAsyncThread = CompletableFuture<Boolean>()
      val isRenderingThread = CompletableFuture<Boolean>()
      val isRenderingThreadDuringAsync = CompletableFuture<Boolean>()
      val loader =
        FakeAsyncLoader(
          onSync = { isRenderingThread.complete(KtxAsync.isOnRenderingThread()) },
          onAsync = {
            isAsyncThread.complete(asyncThread === Thread.currentThread())
            isRenderingThreadDuringAsync.complete(KtxAsync.isOnRenderingThread())
          },
        )
      val storage = AssetStorage(asyncContext = asyncContext, useDefaultLoaders = false)
      storage.setLoader { loader }

      // When:
      runBlocking { storage.load<FakeAsset>("fake.path") }

      // Then:
      isRenderingThread.getNow(false) shouldBe true
      isAsyncThread.getNow(false) shouldBe true
      isRenderingThreadDuringAsync.getNow(true) shouldBe false
    }

    test("should load assets on rendering thread with synchronous loading ") {
      // Given:
      val isRenderingThread = CompletableFuture<Boolean>()
      val loader =
        FakeSyncLoader(
          onLoad = { isRenderingThread.complete(KtxAsync.isOnRenderingThread()) },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }

      // When:
      storage.loadSync<FakeAsset>("fake.path")

      // Then:
      isRenderingThread.getNow(false) shouldBe true
    }

    test("should load assets synchronously on rendering thread with synchronous loader") {
      // Given:
      val isRenderingThread = CompletableFuture<Boolean>()
      val loader =
        FakeSyncLoader(
          onLoad = { isRenderingThread.complete(KtxAsync.isOnRenderingThread()) },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }

      // When:
      storage.loadSync<FakeAsset>("fake.path")

      // Then:
      isRenderingThread.getNow(false) shouldBe true
    }

    test("should load assets synchronously on rendering thread with asynchronous loader") {
      // Given:
      val isRenderingThreadDuringAsync = CompletableFuture<Boolean>()
      val isRenderingThreadDuringSync = CompletableFuture<Boolean>()
      val loader =
        FakeAsyncLoader(
          onAsync = { isRenderingThreadDuringAsync.complete(KtxAsync.isOnRenderingThread()) },
          onSync = { isRenderingThreadDuringSync.complete(KtxAsync.isOnRenderingThread()) },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }

      // When:
      storage.loadSync<FakeAsset>("fake.path")

      // Then: entire synchronous loading should be executed on the rendering thread:
      isRenderingThreadDuringAsync.getNow(false) shouldBe true
      isRenderingThreadDuringSync.getNow(false) shouldBe true
    }

    test("should handle loading exceptions") {
      // Given:
      val loader =
        FakeSyncLoader(
          onLoad = { throw IllegalStateException("Expected.") },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }
      val path = "fake path"

      // When:
      shouldThrow<AssetLoadingException> {
        runBlocking { storage.load<FakeAsset>(path) }
      }

      // Then: asset should still be in storage, but rethrowing original exception:
      storage.contains<FakeAsset>(path) shouldBe true
      storage.getReferenceCount<FakeAsset>(path) shouldBe 1
      shouldThrow<AssetLoadingException> {
        storage.get<FakeAsset>(path)
      }
      shouldThrow<AssetLoadingException> {
        storage.getOrNull<FakeAsset>(path)
      }
      val reference = storage.getAsync<FakeAsset>(path)
      shouldThrow<AssetLoadingException> {
        runBlocking { reference.await() }
      }
      checkProgress(storage, failed = 1, warn = true)
    }

    test("should handle asynchronous loading exceptions") {
      // Given:
      val loader =
        FakeAsyncLoader(
          onAsync = { throw IllegalStateException("Expected.") },
          onSync = {},
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }
      val path = "fake path"

      // When:
      shouldThrow<AssetLoadingException> {
        runBlocking { storage.load<FakeAsset>(path) }
      }

      // Then: asset should still be in storage, but rethrowing original exception:
      storage.contains<FakeAsset>(path) shouldBe true
      storage.getReferenceCount<FakeAsset>(path) shouldBe 1
      shouldThrow<AssetLoadingException> {
        storage.get<FakeAsset>(path)
      }
      shouldThrow<AssetLoadingException> {
        storage.getOrNull<FakeAsset>(path)
      }
      val reference = storage.getAsync<FakeAsset>(path)
      shouldThrow<AssetLoadingException> {
        runBlocking { reference.await() }
      }
      checkProgress(storage, failed = 1, warn = true)
    }

    test("should handle synchronous loading exceptions") {
      // Given:
      val loader =
        FakeAsyncLoader(
          onAsync = { },
          onSync = { throw IllegalStateException("Expected.") },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader { loader }
      val path = "fake path"

      // When:
      shouldThrow<AssetLoadingException> {
        runBlocking { storage.load<FakeAsset>(path) }
      }

      // Then: asset should still be in storage, but rethrowing original exception:
      storage.contains<FakeAsset>(path) shouldBe true
      storage.getReferenceCount<FakeAsset>(path) shouldBe 1
      shouldThrow<AssetLoadingException> {
        storage.get<FakeAsset>(path)
      }
      shouldThrow<AssetLoadingException> {
        storage.getOrNull<FakeAsset>(path)
      }
      val reference = storage.getAsync<FakeAsset>(path)
      shouldThrow<AssetLoadingException> {
        runBlocking { reference.await() }
      }
      checkProgress(storage, failed = 1, warn = true)
    }

    test("should throw exception when asset is unloaded asynchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val loadingStarted = CompletableFuture<Boolean>()
      val unloadingFinished = CompletableFuture<Boolean>()
      val exceptionCaught = CompletableFuture<Boolean>()
      val path = "fake path"
      val unloader = newSingleThreadAsyncContext()
      lateinit var exception: Throwable
      storage.setLoader {
        FakeSyncLoader(
          onLoad = {
            loadingStarted.complete(true)
            unloadingFinished.join()
          },
        )
      }
      KtxAsync.launch {
        try {
          storage.load<FakeAsset>(path)
        } catch (expected: Throwable) {
          exception = expected
        }
        exceptionCaught.complete(true)
      }

      // When:
      KtxAsync.launch(unloader) {
        loadingStarted.join()
        storage.unload<FakeAsset>(path)
        unloadingFinished.complete(true)
      }

      // Then:
      exceptionCaught.join()
      (exception is UnloadedAssetException) shouldBe true
      storage.contains<FakeAsset>(path) shouldBe false
      shouldThrow<MissingAssetException> {
        storage.get<FakeAsset>(path)
      }
      checkProgress(storage, total = 0, warn = true)
    }

    test("should fail to load assets with loaders that use unsupported AssetManagerWrapper methods") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake path"
      storage.setLoader {
        FakeAsyncLoader(
          onAsync = { assetManager -> assetManager.containsAsset("Calling an unsupported method.") },
          onSync = {},
        )
      }

      // When:
      shouldThrow<UnsupportedMethodException> {
        runBlocking {
          storage.load<FakeAsset>(path)
        }
      }

      // Then:
      storage.contains<FakeAsset>(path) shouldBe true
      storage.getReferenceCount<FakeAsset>(path) shouldBe 1
      shouldThrow<UnsupportedMethodException> {
        storage.get<FakeAsset>(path)
      }
    }

    test("should fail to load assets when loaders try to access unregistered unloaded dependency") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake path"
      storage.setLoader {
        FakeAsyncLoader(
          onAsync = { assetManager -> assetManager.get("Missing", FakeAsset::class.java) },
          onSync = {},
        )
      }

      // When:
      shouldThrow<MissingDependencyException> {
        runBlocking {
          storage.load<FakeAsset>(path)
        }
      }

      // Then:
      storage.contains<FakeAsset>(path) shouldBe true
      storage.getReferenceCount<FakeAsset>(path) shouldBe 1
      shouldThrow<MissingDependencyException> {
        storage.get<FakeAsset>(path)
      }
    }

    test("should not fail to unload asset that was loaded exceptionally") {
      // Given:
      val loader =
        FakeSyncLoader(
          onLoad = { throw IllegalStateException("Expected.") },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake path"
      storage.setLoader { loader }
      storage.logger.level = Logger.NONE // Disposing exception will be logged.
      runBlocking {
        try {
          storage.load<FakeAsset>(path)
        } catch (exception: AssetLoadingException) {
          // Expected.
        }
      }

      // When:
      val unloaded =
        runBlocking {
          storage.unload<FakeAsset>(path)
        }

      // Then:
      unloaded shouldBe true
      storage.contains<FakeAsset>(path) shouldBe false
      storage.getReferenceCount<FakeAsset>(path) shouldBe 0
    }

    test("should fail to load an asset or any of its dependencies with a dependency loader is missing") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake path"
      val dependency = "dependency with missing loader"
      storage.setLoader {
        FakeSyncLoader(
          onLoad = {},
          dependencies = GdxArray.with(storage.getAssetDescriptor<Vector2>(dependency)),
        )
      }

      // When:
      shouldThrow<MissingLoaderException> {
        runBlocking { storage.load<FakeAsset>("path") }
      }

      // Then:
      storage.contains<FakeAsset>(path) shouldBe false
      storage.contains<Vector2>(dependency) shouldBe false
      checkProgress(storage, total = 0)
    }

    test("should not fail to unload asset that was loaded exceptionally with dependencies") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "path.sync"
      val dependency = "path.async"
      val loader =
        FakeSyncLoader(
          onLoad = {},
          dependencies = GdxArray.with(storage.getAssetDescriptor<FakeAsset>(dependency)),
        )
      val dependencyLoader =
        FakeAsyncLoader(
          onAsync = {},
          onSync = { throw IllegalStateException("Expected.") },
        )
      storage.setLoader(suffix = ".sync") { loader }
      storage.setLoader(suffix = ".async") { dependencyLoader }
      storage.logger.level = Logger.NONE // Disposing exception will be logged.
      runBlocking {
        try {
          storage.load<FakeAsset>(path)
        } catch (exception: AssetLoadingException) {
          // Expected. Asset fails to load due to dependency.
        }
      }
      storage.contains<FakeAsset>(path) shouldBe true
      storage.contains<FakeAsset>(dependency) shouldBe true

      // When:
      val unloaded =
        runBlocking {
          storage.unload<FakeAsset>(path)
        }

      // Then:
      unloaded shouldBe true
      storage.contains<FakeAsset>(path) shouldBe false
      storage.getReferenceCount<FakeAsset>(path) shouldBe 0
      storage.contains<FakeAsset>(dependency) shouldBe false
      storage.getReferenceCount<FakeAsset>(dependency) shouldBe 0
    }

    test("should report as unloaded until the asset has finished loading") {
      // Given:
      val loadingStarted = CompletableFuture<Boolean>()
      val loading = CompletableFuture<Boolean>()
      val loadingFinished = CompletableFuture<Boolean>()
      val loader =
        FakeSyncLoader(
          onLoad = {
            loadingStarted.join()
            loading.complete(true)
            loadingFinished.join()
          },
        )
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake.path"
      val identifier = storage.getIdentifier<FakeAsset>(path)
      storage.setLoader { loader }

      // When:
      KtxAsync.launch { storage.load(identifier) }

      // Then:
      storage.isLoaded(identifier) shouldBe false

      loadingStarted.complete(true)
      loading.join()
      (identifier in storage) shouldBe true
      storage.isLoaded(identifier) shouldBe false
      checkProgress(storage, loaded = 0, total = 1)

      loadingFinished.complete(true)
      runBlocking { storage.getAsync<FakeAsset>(path).await() }
      (identifier in storage) shouldBe true
      storage.isLoaded(identifier) shouldBe true
      checkProgress(storage, loaded = 1, total = 1, warn = true)
    }

    test("should utilize asynchronous threads for asset loading") {
      // Given:
      val asyncThreadNameSuffix = "AssetStorageAsyncThread"
      val renderingThread = getMainRenderingThread()
      val loaders = newAsyncContext(4, threadName = asyncThreadNameSuffix)
      val schedulers = newAsyncContext(4)
      val asyncLoadingThreads: MutableSet<Thread> = ConcurrentHashMap.newKeySet()
      val syncLoadingThreads: MutableSet<Thread> = ConcurrentHashMap.newKeySet()
      val storage = AssetStorage(asyncContext = loaders, useDefaultLoaders = false)
      storage.setLoader {
        FakeAsyncLoader(
          onAsync = { asyncLoadingThreads.add(Thread.currentThread()) },
          onSync = { syncLoadingThreads.add(Thread.currentThread()) },
        )
      }

      // When: asynchronously loading multiple assets:
      val tasks =
        (1..100).map { index ->
          val finished = CompletableDeferred<Boolean>()
          KtxAsync.launch(schedulers) {
            storage.load<FakeAsset>(path = index.toString())
            finished.complete(true)
          }
          finished
        }

      // Then:
      runBlocking { tasks.joinAll() }
      syncLoadingThreads.size shouldBe 1
      (renderingThread in syncLoadingThreads) shouldBe true
      (asyncLoadingThreads.size in 2..4) shouldBe true
      (asyncLoadingThreads.all { asyncThreadNameSuffix in it.name }) shouldBe true
    }

    test("should dispose of assets that were unloaded before assignment") {
      // Given:
      val path = "fake path"
      val loadingStarted = CompletableFuture<Boolean>()
      val unloadingFinished = CompletableFuture<Boolean>()
      val loadingFinished = CompletableFuture<Boolean>()
      lateinit var asset: FakeAsset
      lateinit var exception: Throwable
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.setLoader {
        FakeSyncLoader(
          onLoad = {
            asset = it
            loadingStarted.complete(true)
            unloadingFinished.join()
          },
        )
      }

      // When:
      KtxAsync.launch {
        try {
          // Scheduling asset for loading - will be unloaded before it is finished:
          storage.load<FakeAsset>(path)
        } catch (expected: AssetStorageException) {
          // UnloadedAssetException should be caught:
          exception = expected
          loadingFinished.complete(true)
        }
      }
      // Waiting for the loading to start:
      loadingStarted.join()
      runBlocking {
        // Unloading the asset:
        storage.unload<FakeAsset>(path)
        // Loader is waiting for the unloading to finish - unlocking thread:
        unloadingFinished.complete(true)
      }

      // Then:
      asset.disposingFinished.join()
      asset.isDisposed shouldBe true
      loadingFinished.join()
      shouldThrow<UnloadedAssetException> {
        throw exception
      }
    }

    test("should safely dispose of assets that failed to load") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.logger.level = Logger.NONE
      val path = "fake path"
      storage.setLoader {
        FakeSyncLoader(
          onLoad = { throw IllegalStateException("Expected.") },
        )
      }
      runBlocking {
        try {
          storage.load<FakeAsset>(path)
        } catch (exception: AssetLoadingException) {
          // Expected.
        }
      }

      // When:
      storage.dispose()

      // Then:
      storage.contains<FakeAsset>(path) shouldBe false
    }

    test("should safely dispose of assets that failed to load with error handling") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      storage.logger.level = Logger.NONE
      val path = "fake path"
      lateinit var exception: Throwable
      lateinit var identifier: Identifier<*>
      storage.setLoader {
        FakeSyncLoader(
          onLoad = { throw IllegalStateException("Expected.") },
        )
      }
      runBlocking {
        try {
          storage.load<FakeAsset>(path)
        } catch (exception: AssetLoadingException) {
          // Expected.
        }
      }

      // When:
      runBlocking {
        storage.dispose { id, cause ->
          exception = cause
          identifier = id
        }
      }

      // Then:
      storage.contains<FakeAsset>(path) shouldBe false
      (exception is AssetLoadingException) shouldBe true
      identifier shouldBe storage.getIdentifier<FakeAsset>(path)
    }

    test("should immediately throw exception when attempting to synchronously load already scheduled asset") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake path"
      val loadingStarted = CompletableFuture<Boolean>()
      val loadingFinished = CompletableFuture<Boolean>()
      storage.setLoader {
        FakeSyncLoader(
          onLoad = {
            loadingStarted.complete(true)
            loadingFinished.join()
          },
        )
      }
      val reference = storage.loadAsync<FakeAsset>(path)
      loadingStarted.join()

      // When: asset is not loaded yet:
      shouldThrow<MissingAssetException> {
        storage.loadSync<FakeAsset>(path)
      }

      // Then:
      storage.isLoaded<FakeAsset>(path) shouldBe false
      loadingFinished.complete(true)
      runBlocking { reference.await() }
      storage.isLoaded<FakeAsset>(path) shouldBe true
      // Should still count the reference, since load was called:
      storage.getReferenceCount<FakeAsset>(path) shouldBe 2
    }

    test("should throw exception when a dependency of a synchronously loaded asset is being loaded asynchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake.path"
      val dependency = "path.dep"
      val loadingStarted = CompletableFuture<Boolean>()
      val loadingFinished = CompletableFuture<Boolean>()
      storage.setLoader {
        object : FakeSyncLoader(
          dependencies = GdxArray.with(storage.getAssetDescriptor<FakeAsset>(dependency)),
          onLoad = {},
        ) {
          override fun load(
            assetManager: AssetManager,
            fileName: String?,
            file: FileHandle?,
            parameter: FakeParameters?,
          ): FakeAsset {
            assetManager.get(dependency, FakeAsset::class.java)
            return super.load(assetManager, fileName, file, parameter)
          }
        }
      }
      storage.setLoader(suffix = ".dep") {
        FakeSyncLoader(
          onLoad = {
            loadingStarted.complete(true)
            loadingFinished.join()
          },
        )
      }
      val reference = storage.loadAsync<FakeAsset>(dependency)
      loadingStarted.join()

      // When: dependency is not loaded yet:
      shouldThrow<MissingDependencyException> {
        storage.loadSync<FakeAsset>(path)
      }

      // Then:
      storage.isLoaded<FakeAsset>(path) shouldBe true
      storage.getReferenceCount<FakeAsset>(path) shouldBe 1
      storage.isLoaded<FakeAsset>(dependency) shouldBe false
      loadingFinished.complete(true)
      runBlocking { reference.await() }
      storage.isLoaded<FakeAsset>(dependency) shouldBe true
      // Should still count the reference, since load was called:
      storage.getReferenceCount<FakeAsset>(dependency) shouldBe 2
    }

    test("should return empty snapshot of the storage state") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // When:
      val snapshot = storage.takeSnapshot()

      // Then:
      snapshot shouldBe AssetStorageSnapshot(mapOf())
    }

    test("should return empty snapshot of the storage state asynchronously") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)

      // When:
      val snapshot = runBlocking { storage.takeSnapshotAsync() }

      // Then:
      snapshot shouldBe AssetStorageSnapshot(mapOf())
    }

    test("should take snapshot of the storage state") {
      // Given:
      val storage = AssetStorage(useDefaultLoaders = false)
      val path = "fake path"
      val id = storage.getIdentifier<FakeAsset>(path)
      val loadingStarted = CompletableFuture<Boolean>()
      val loadingFinished = CompletableFuture<Boolean>()
      storage.setLoader {
        FakeSyncLoader(
          onLoad = {
            loadingStarted.complete(true)
            loadingFinished.join()
          },
        )
      }
      val reference = storage.loadAsync<FakeAsset>(path)
      loadingStarted.join()

      // When: asset is not loaded yet:
      var snapshot = storage.takeSnapshot()

      // Then:
      storage.isLoaded<FakeAsset>(path) shouldBe false
      val asset: Asset<FakeAsset> = snapshot.assets[id] as Asset<FakeAsset>
      asset.identifier shouldBe id
      asset.reference.isCompleted shouldBe false
      asset.reference.isCancelled shouldBe false
      asset.referenceCount shouldBe 1
      asset.dependencies shouldBe listOf<Asset<*>>()

      // When: modifying asset loading manually:
      asset.reference.complete(FakeAsset())
      asset.referenceCount = 10

      // Then: internal state should be unmodified:
      storage.isLoaded<FakeAsset>(path) shouldBe false
      storage.getReferenceCount(id) shouldBe 1

      // When: asset is loaded:
      loadingFinished.complete(true)
      val instance = runBlocking { reference.await() }
      snapshot = storage.takeSnapshot()

      // Then:
      storage.isLoaded<FakeAsset>(path) shouldBe true
      val loadedAsset = snapshot.assets[id]!!
      loadedAsset.identifier shouldBe id
      loadedAsset.reference.isCompleted shouldBe true
      (runBlocking { loadedAsset.reference.await() }) shouldBeSameInstanceAs instance
      loadedAsset.referenceCount shouldBe 1
      loadedAsset.dependencies shouldBe listOf<Asset<*>>()
    }

    test("should pretty print snapshot") {
      // Given:
      val firstId = Identifier("first.file", String::class.java)

      val first =
        Asset(
          descriptor = firstId.toAssetDescriptor(),
          identifier = firstId,
          reference = CompletableDeferred("test"),
          dependencies = listOf(),
          loader = FakeSyncLoader() as Loader<String>,
          referenceCount = 2,
        )
      val secondId = Identifier("second.file", Int::class.java)

      val second =
        Asset(
          descriptor = secondId.toAssetDescriptor(),
          identifier = secondId,
          reference = CompletableDeferred(),
          dependencies = listOf(first),
          loader = FakeSyncLoader() as Loader<Int>,
          referenceCount = 1,
        )
      val snapshot =
        AssetStorageSnapshot(
          assets =
            mapOf(
              firstId to first,
              secondId to second,
            ),
        )

      // When:
      val output = snapshot.prettyPrint()

      // Then:
      output shouldBe """[
  "first.file" (java.lang.String) {
    references=2,
    dependencies=[],
    loaded=true,
    loader=ktx.assets.async.AssetStorageTest${"$"}FakeSyncLoader,
  },
  "second.file" (int) {
    references=1,
    dependencies=["first.file" (java.lang.String)],
    loaded=false,
    loader=ktx.assets.async.AssetStorageTest${"$"}FakeSyncLoader,
  },
]"""
    }
  }
}
