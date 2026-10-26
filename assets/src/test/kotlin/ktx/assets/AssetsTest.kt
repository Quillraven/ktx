package ktx.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import ktx.assets.MockAssetLoader.MockParameter
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Tests asset-related utilities and [AssetManager] extensions.
 */
@Suppress("USELESS_IS_CHECK") // Explicitly checking loaded asset types.
class AssetsTest : FunSpec({
  /**
   * Creates an [AssetManager] with registered [MockAssetLoader].
   */
  fun managerWithMockAssetLoader() =
    AssetManager().apply {
      setLoader(MockAssetLoader(fileHandleResolver))
    }

  val assetManager: AssetManager = managerWithMockAssetLoader()

  beforeTest {
    Gdx.files = MockFiles()
  }

  test("should schedule asset loading") {
    val assetWrapper = assetManager.load<MockAsset>("test")
    assetManager.finishLoading()

    assetWrapper.isLoaded() shouldBe true
    val asset = assetWrapper.asset
    (asset is MockAsset) shouldBe true
    asset shouldBeSameInstanceAs assetManager["test"]
    asset.data shouldBe "test"
    (assetWrapper as ManagedAsset).manager shouldBeSameInstanceAs assetManager
  }

  test("should schedule asset loading with parameters") {
    val assetWrapper = assetManager.load("test", MockParameter("additional"))
    assetManager.finishLoading()

    assetWrapper.isLoaded() shouldBe true
    val asset = assetWrapper.asset
    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
    asset.additional shouldBe "additional"
    (assetWrapper as ManagedAsset).manager shouldBeSameInstanceAs assetManager
  }

  test("should schedule asset loading with AssetDescriptor") {
    val assetWrapper = assetManager.loadAsset(assetDescriptor<MockAsset>("test"))
    assetManager.finishLoading()

    assetWrapper.isLoaded() shouldBe true
    val asset = assetWrapper.asset
    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
    (assetWrapper as ManagedAsset).manager shouldBeSameInstanceAs assetManager
  }

  test("should load on demand") {
    val assetWrapper = assetManager.loadOnDemand<MockAsset>("test")

    assetWrapper.isLoaded() shouldBe false
    assetManager.finishLoading()
    // Loading was not supposed to be scheduled, should be still unloaded:
    assetWrapper.isLoaded() shouldBe false
    // Loaded on first asset getter call:
    val asset = assetWrapper.asset
    assetWrapper.isLoaded() shouldBe true
    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
    (assetWrapper as DelayedAsset).manager shouldBeSameInstanceAs assetManager
  }

  test("should load on demand with parameters") {
    val assetWrapper = assetManager.loadOnDemand("test", MockParameter("additional"))

    assetWrapper.isLoaded() shouldBe false
    assetManager.finishLoading()
    // Loading was not supposed to be scheduled, should be still unloaded:
    assetWrapper.isLoaded() shouldBe false
    // Loaded on first asset getter call:
    val asset = assetWrapper.asset
    assetWrapper.isLoaded() shouldBe true
    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
    asset.additional shouldBe "additional"
    (assetWrapper as DelayedAsset).manager shouldBeSameInstanceAs assetManager
  }

  test("should load on demand with descriptor") {
    val assetWrapper = assetManager.loadOnDemand(assetDescriptor<MockAsset>("test"))

    assetWrapper.isLoaded() shouldBe false
    assetManager.finishLoading()
    // Loading was not supposed to be scheduled, should be still unloaded:
    assetWrapper.isLoaded() shouldBe false
    // Loaded on first asset getter call:
    val asset = assetWrapper.asset
    assetWrapper.isLoaded() shouldBe true
    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
    (assetWrapper as DelayedAsset).manager shouldBeSameInstanceAs assetManager
  }

  test("should extract loaded asset with explicit type from manager") {
    assetManager.load<MockAsset>("test")
    assetManager.finishLoading()

    val asset = assetManager.getAsset<MockAsset>("test")
    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
  }

  test("should fail to extract unloaded asset") {
    shouldThrow<GdxRuntimeException> {
      assetManager.getAsset<MockAsset>("unloaded")
    }
  }

  test("should unload asset gracefully") {
    assetManager.load<MockAsset>("test")
    assetManager.finishLoading()
    val asset = assetManager.getAsset<MockAsset>("test")

    assetManager.unloadSafely("test")
    assetManager.isLoaded("test") shouldBe false
    asset.disposed shouldBe true
  }

  test("should ignore unloading exception due to asset not being loaded") {
    assetManager.unloadSafely("unloaded") // unload("unloaded") would throw exception.
  }

  test("should ignore exceptions thrown during unloading") {
    val assetManager =
      mock<AssetManager> {
        on { unload("test") } doThrow GdxRuntimeException("Expected.")
      }
    assetManager.unloadSafely("test")
    verify(assetManager).unload("test")
  }

  test("should ignore exception due to multiple asset unload requests") {
    assetManager.load<MockAsset>("test")
    assetManager.finishLoading()

    assetManager.unloadSafely("test")
    assetManager.unloadSafely("test")
    assetManager.unloadSafely("test")
    assetManager.isLoaded("test") shouldBe false
  }

  test("should ignore exception due to prior disposal") {
    assetManager.load<MockAsset>("test")
    assetManager.finishLoading()
    val mockAsset = assetManager.get<MockAsset>("test")
    mockAsset.dispose()
    assetManager.unloadSafely("test")
  }

  test("should unload asset handling exception") {
    assetManager.load<MockAsset>("test")
    assetManager.finishLoading()
    val asset = assetManager.getAsset<MockAsset>("test")

    assetManager.unload("test") {
      fail("Exception should not be thrown.")
    }
    assetManager.isLoaded("test") shouldBe false
    asset.disposed shouldBe true
  }

  test("should handle unloading exception due to asset not being loaded") {
    var exception: Exception? = null
    assetManager.unload("unloaded") { expected ->
      (expected is GdxRuntimeException) shouldBe true
      exception = expected
    }
    exception.shouldNotBeNull()
  }

  test("should handle exceptions thrown during unloading") {
    val assetManager =
      mock<AssetManager> {
        on { unload("test") } doThrow GdxRuntimeException("Expected.")
      }
    assetManager.unload("test") { exception ->
      (exception is GdxRuntimeException) shouldBe true
    }
    verify(assetManager).unload("test")
  }

  test("should delegate field to scheduled asset") {
    class TestDelegate {
      val asset by assetManager.load<MockAsset>("test")
    }

    val test = TestDelegate()
    assetManager.finishLoading()

    (test.asset is MockAsset) shouldBe true
    test.asset shouldBe assetManager["test"]
    test.asset.data shouldBe "test"
  }

  test("should delegate local variable to scheduled asset") {
    val asset by assetManager.load<MockAsset>("test")
    assetManager.finishLoading()

    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
  }

  test("should throw if using delegate field before loading") {
    shouldThrow<GdxRuntimeException> {
      class TestDelegate {
        val asset by assetManager.load<MockAsset>("test")
      }

      val test = TestDelegate()
      test.asset.data
    }
  }

  test("should throw if using delegate local variable before loading") {
    shouldThrow<GdxRuntimeException> {
      val asset by assetManager.load<MockAsset>("test")
      asset.data
    }
  }

  test("should delegate field to eagerly loaded asset") {
    class TestDelegate {
      val asset by assetManager.loadOnDemand<MockAsset>("test")
    }

    val test = TestDelegate()

    // Should be loaded after first usage:
    assetManager.isLoaded("test") shouldBe false
    (test.asset is MockAsset) shouldBe true
    // Previous check should have loaded the asset:
    assetManager.isLoaded("test") shouldBe true
    test.asset shouldBe assetManager["test"]
    test.asset.data shouldBe "test"
    (test.asset === test.asset) shouldBe true // Is instance cached?
  }

  test("should delegate local variable to eagerly loaded asset") {
    val asset by assetManager.loadOnDemand<MockAsset>("test")

    // Should be loaded after first usage:
    assetManager.isLoaded("test") shouldBe false
    (asset is MockAsset) shouldBe true
    // Previous check should have loaded the asset:
    assetManager.isLoaded("test") shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
  }

  test("should delegate field to scheduled asset with descriptor") {
    class TestDelegate {
      val asset by assetManager.loadAsset(assetDescriptor<MockAsset>("test"))
    }

    val test = TestDelegate()
    assetManager.finishLoading()

    (test.asset is MockAsset) shouldBe true
    test.asset shouldBe assetManager["test"]
    test.asset.data shouldBe "test"
  }

  test("should delegate local variable to scheduled asset with descriptor") {
    val asset by assetManager.loadAsset(assetDescriptor<MockAsset>("test"))
    assetManager.finishLoading()

    (asset is MockAsset) shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
  }

  test("should throw if using delegate field before loading with descriptor") {
    shouldThrow<GdxRuntimeException> {
      class TestDelegate {
        val asset by assetManager.loadAsset(assetDescriptor<MockAsset>("test"))
      }

      val test = TestDelegate()
      test.asset.data
    }
  }

  test("should throw if using delegate local variable before loading with descriptor") {
    shouldThrow<GdxRuntimeException> {
      val asset by assetManager.loadAsset(assetDescriptor<MockAsset>("test"))
      asset.data
    }
  }

  test("should delegate field to eagerly loaded asset with descriptor") {
    class TestDelegate {
      val asset by assetManager.loadOnDemand(assetDescriptor<MockAsset>("test"))
    }

    val test = TestDelegate()

    // Should be loaded after first usage:
    assetManager.isLoaded("test") shouldBe false
    (test.asset is MockAsset) shouldBe true
    // Previous check should have loaded the asset:
    assetManager.isLoaded("test") shouldBe true
    test.asset shouldBe assetManager["test"]
    test.asset.data shouldBe "test"
    (test.asset === test.asset) shouldBe true // Is instance cached?
  }

  test("should delegate local variable to eagerly loaded asset with descriptor") {
    val asset by assetManager.loadOnDemand(assetDescriptor<MockAsset>("test"))

    // Should be loaded after first usage:
    assetManager.isLoaded("test") shouldBe false
    (asset is MockAsset) shouldBe true
    // Previous check should have loaded the asset:
    assetManager.isLoaded("test") shouldBe true
    asset shouldBe assetManager["test"]
    asset.data shouldBe "test"
  }

  test("should create asset descriptor with string path") {
    val descriptor = assetDescriptor<MockAsset>("mock.file")

    descriptor.type shouldBe MockAsset::class.java
    descriptor.fileName shouldBe "mock.file"
  }

  test("should create asset description with string path and loading parameters") {
    val parameter = MockParameter("mock")
    val descriptorWithParams = assetDescriptor("mock.file", parameter)

    descriptorWithParams.type shouldBe MockAsset::class.java
    descriptorWithParams.fileName shouldBe "mock.file"
    descriptorWithParams.params shouldBe parameter
  }

  test("should create asset descriptor with FileHandle") {
    val descriptor = assetDescriptor<MockAsset>(FileHandle("mock.file"))

    descriptor.type shouldBe MockAsset::class.java
    descriptor.file.path() shouldBe "mock.file"
  }

  test("should create asset descriptor with FileHandle and loading parameters") {
    val parameter = MockParameter("mock")
    val descriptorWithParams = assetDescriptor(FileHandle("mock.file"), parameter)

    descriptorWithParams.type shouldBe MockAsset::class.java
    descriptorWithParams.file.path() shouldBe "mock.file"
    descriptorWithParams.params shouldBe parameter
  }

  test("should find no AssetLoader for unknown asset type") {
    val manager = AssetManager()
    manager.getLoader<MockAsset>().shouldBeNull()
  }

  test("should find no AssetLoader for unknown suffix") {
    val manager = AssetManager()
    val loader = MockAssetLoader(manager.fileHandleResolver)
    manager.setLoader(loader, "suffix")

    manager.getLoader<MockAsset>("unknown").shouldBeNull()
  }

  test("should find AssetLoader") {
    val manager = AssetManager()
    val loader = MockAssetLoader(manager.fileHandleResolver)
    manager.setLoader(loader)

    manager.getLoader<MockAsset>() shouldBeSameInstanceAs loader
  }

  test("should find AssetLoader with suffix") {
    val manager = AssetManager()
    val loader = MockAssetLoader(manager.fileHandleResolver)
    manager.setLoader(loader, "suffix")

    manager.getLoader<MockAsset>("suffix") shouldBeSameInstanceAs loader
    manager.getLoader<MockAsset>().shouldBeNull()
  }

  test("should set AssetLoader") {
    val manager = AssetManager()
    val loader = MockAssetLoader(manager.fileHandleResolver)
    manager.setLoader(loader)

    manager.getLoader(MockAsset::class.java) shouldBeSameInstanceAs loader
  }

  test("should set AssetLoader with suffix") {
    val manager = AssetManager()
    val loader = MockAssetLoader(manager.fileHandleResolver)
    manager.setLoader(loader, ".mock")

    manager.getLoader(MockAsset::class.java, ".mock") shouldBeSameInstanceAs loader
  }

  test("should load and unload members") {
    class TestAssetGroup : AssetGroup(assetManager) {
      val member1 by asset<MockAsset>("member1")
      val member2 by asset<MockAsset>("member2")
    }

    val group = TestAssetGroup()
    group.finishLoading()
    group.isLoaded() shouldBe true

    val mockAssets = listOf(group.member1, group.member2)
    group.unloadAll()
    !group.isLoaded() shouldBe true
    for (mockAsset in mockAssets) {
      mockAsset.disposed shouldBe true
    }
  }

  test("should load by update") {
    class TestAssetGroup : AssetGroup(assetManager) {
      val member1 by asset<MockAsset>("member1")
      val member2 by asset<MockAsset>("member2")
      val member3 by asset<MockAsset>("member3")
    }

    val nonmember = assetManager.load<MockAsset>("nonmember")

    val group = TestAssetGroup()
    nonmember.load()
    while (true) {
      if (group.update()) {
        break
      }
    }
    group.isLoaded() shouldBe true
    (listOf(group.member1, group.member2, group.member3).all { !it.disposed }) shouldBe true
  }

  test("should catch exceptions") {
    class TestAssetGroup : AssetGroup(assetManager) {
      val member1 by asset<MockAsset>("member1")
      val member2 by asset<MockAsset>("member2")
    }

    val group = TestAssetGroup()
    with(group) {
      finishLoading()
      member1.dispose()
      member2.dispose()
    }

    var caught = 0
    group.unloadAllSafely { _, _ ->
      caught++
    }
    2 shouldBe caught
  }

  test("should apply prefix") {
    class TestAssetGroup : AssetGroup(assetManager, "prefix/") {
      val member1 = delayedAsset<MockAsset>("member1")
      val member2 = delayedAsset<MockAsset>("member2")
    }

    val group =
      TestAssetGroup().apply {
        loadAll()
        manager.finishLoading()
      }

    for (index in 1..2) {
      group.manager.isLoaded("prefix/member$index") shouldBe true
    }
    group.member1.shouldNotBeNull()
    group.member2.shouldNotBeNull()
  }
})

/**
 * Represents a mock-up asset. Implements [Disposable] for testing utility.
 * @param data path of the file.
 * @param additional optional string value passed with [MockParameter].
 */
class MockAsset(
  val data: String,
  val additional: String?,
) : Disposable {
  var disposed = false

  override fun dispose() {
    require(!disposed) { "Was already disposed!" } // Simulate behavior of some Gdx assets
    disposed = true
  }
}

/**
 * Mocks asynchronous file loading. Sets [MockAsset.data] as file path. Extracts [MockAsset.additional] from
 * [MockParameter] (if present).
 */
class MockAssetLoader(
  fileHandleResolver: FileHandleResolver,
) : AsynchronousAssetLoader<MockAsset, MockParameter>(fileHandleResolver) {
  @Volatile
  private var additional: String? = null

  override fun loadAsync(
    manager: AssetManager,
    fileName: String,
    file: FileHandle,
    parameter: MockParameter?,
  ) {
    if (parameter != null) {
      additional = parameter.additional
    }
  }

  override fun loadSync(
    manager: AssetManager,
    fileName: String,
    file: FileHandle,
    parameter: MockParameter?,
  ): MockAsset {
    val asset = MockAsset(file.path(), additional)
    additional = null
    return asset
  }

  override fun getDependencies(
    fileName: String?,
    file: FileHandle?,
    parameter: MockParameter?,
  ): Array<AssetDescriptor<Any>>? = null

  /** Allows to set [MockAsset.additional] via loader. Tests assets parameters API. */
  class MockParameter(
    val additional: String?,
  ) : AssetLoaderParameters<MockAsset>()
}
