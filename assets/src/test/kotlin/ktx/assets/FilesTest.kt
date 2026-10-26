package ktx.assets

import com.badlogic.gdx.Files.FileType.Absolute
import com.badlogic.gdx.Files.FileType.Classpath
import com.badlogic.gdx.Files.FileType.External
import com.badlogic.gdx.Files.FileType.Internal
import com.badlogic.gdx.Files.FileType.Local
import com.badlogic.gdx.Gdx
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests files-related utilities.
 */
class FilesTest : FunSpec({
  beforeTest {
    Gdx.files = MockFiles()
  }

  test("should convert string to classpath FileHandle") {
    val file = "my/package/classpath.file".toClasspathFile()

    file.shouldNotBeNull()
    file.type() shouldBe Classpath
    file.path() shouldBe "my/package/classpath.file"
  }

  test("should convert string to internal FileHandle") {
    val file = "internal.file".toInternalFile()

    file.shouldNotBeNull()
    file.type() shouldBe Internal
    file.path() shouldBe "internal.file"
  }

  test("should convert string to local FileHandle") {
    val file = "local.file".toLocalFile()

    file.shouldNotBeNull()
    file.type() shouldBe Local
    file.path() shouldBe "local.file"
  }

  test("should convert string to external FileHandle") {
    val file = "some/directory/external.file".toExternalFile()

    file.shouldNotBeNull()
    file.type() shouldBe External
    file.path() shouldBe "some/directory/external.file"
  }

  test("should convert string to absolute FileHandle") {
    val file = "/home/mock/absolute.file".toAbsoluteFile()

    file.shouldNotBeNull()
    file.type() shouldBe Absolute
    file.path() shouldBe "/home/mock/absolute.file"
  }

  test("should create FileHandle with default type") {
    val file = file("mock.file")

    file.shouldNotBeNull()
    file.type() shouldBe Internal
    file.path() shouldBe "mock.file"
  }

  test("should create FileHandle with custom type") {
    val file = file("/home/ktx/mock.file", type = Absolute)

    file.shouldNotBeNull()
    file.type() shouldBe Absolute
    file.path() shouldBe "/home/ktx/mock.file"
  }
})
