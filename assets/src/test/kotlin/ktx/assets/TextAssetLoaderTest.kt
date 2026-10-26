package ktx.assets

import com.badlogic.gdx.assets.loaders.AssetLoader
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import ktx.assets.TextAssetLoader.TextAssetLoaderParameters
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Tests [TextAssetLoader]: [AssetLoader] implementation for asynchronous reading of text files.
 */
class TextAssetLoaderTest : FunSpec({
  test("should read text data from FileHandle") {
    // Given:
    val loader = TextAssetLoader(charset = "UTF-8")
    val file =
      mock<FileHandle> {
        on(it.readString("UTF-8")) doReturn "Content."
      }

    // When:
    loader.loadAsync(mock(), "test.txt", file, null)
    val result = loader.loadSync(mock(), "test.txt", file, null)

    // Then:
    result shouldBe "Content."
    verify(file).readString("UTF-8")
  }

  test("should read text data from FileHandle with loading parameters") {
    // Given:
    val loader = TextAssetLoader(charset = "UTF-8")
    val file =
      mock<FileHandle> {
        on(it.readString("UTF-16")) doReturn "Content."
      }

    // When:
    loader.loadAsync(mock(), "test.txt", file, TextAssetLoaderParameters(charset = "UTF-16"))
    val result = loader.loadSync(mock(), "test.txt", file, TextAssetLoaderParameters(charset = "UTF-16"))

    // Then:
    result shouldBe "Content."
    verify(file).readString("UTF-16")
  }

  test("should throw exception when trying to read file without loading it asynchronously") {
    // Given:
    val loader = TextAssetLoader(charset = "UTF-8")

    // Expect:
    shouldThrow<GdxRuntimeException> {
      loader.loadSync(mock(), "test.txt", mock(), null)
    }
  }
})
