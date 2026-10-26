package ktx.assets

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests [FileHandleResolver] utilities.
 */
class ResolversTest : FunSpec({
  test("should convert Classpath file type to ClasspathFileHandleResolver") {
    val resolver: FileHandleResolver = FileType.Classpath.getResolver()

    (resolver is ClasspathFileHandleResolver) shouldBe true
  }

  test("should convert Internal file type to InternalFileHandleResolver") {
    val resolver: FileHandleResolver = FileType.Internal.getResolver()

    (resolver is InternalFileHandleResolver) shouldBe true
  }

  test("should convert Local file type to LocalFileHandleResolver") {
    val resolver: FileHandleResolver = FileType.Local.getResolver()

    (resolver is LocalFileHandleResolver) shouldBe true
  }

  test("should convert External file type to ExternalFileHandleResolver") {
    val resolver: FileHandleResolver = FileType.External.getResolver()

    (resolver is ExternalFileHandleResolver) shouldBe true
  }

  test("should convert Absolute file type to AbsoluteFileHandleResolver") {
    val resolver: FileHandleResolver = FileType.Absolute.getResolver()

    (resolver is AbsoluteFileHandleResolver) shouldBe true
  }

  test("should decorate FileHandleResolver with PrefixFileHandleResolver") {
    val resolver = InternalFileHandleResolver()

    val decorated = resolver.withPrefix("test")

    decorated.baseResolver shouldBeSameInstanceAs resolver
    decorated.prefix shouldBe "test"
  }

  test("should decorate FileHandleResolver with ResolutionFileResolver") {
    val resolver = InternalFileHandleResolver()

    val decorated = resolver.forResolutions(Resolution(600, 400, "mock"))

    decorated.baseResolver shouldBeSameInstanceAs resolver
    decorated.resolutions.size shouldBe 1
    val resolution = decorated.resolutions[0]
    resolution.portraitWidth shouldBe 600
    resolution.portraitHeight shouldBe 400
    resolution.folder shouldBe "mock"
  }

  test("should not decorate FileHandleResolver with ResolutionFileResolver given no resolutions") {
    shouldThrow<IllegalArgumentException> {
      val resolver = InternalFileHandleResolver()

      resolver.forResolutions()
    }
  }

  test("should construct Resolution") {
    val resolution = resolution(width = 800, height = 600, folder = "test")

    resolution.portraitWidth shouldBe 800
    resolution.portraitHeight shouldBe 600
    resolution.folder shouldBe "test"
  }

  test("should construct Resolution with default folder name") {
    val resolution = resolution(width = 1024, height = 768)

    resolution.portraitWidth shouldBe 1024
    resolution.portraitHeight shouldBe 768
    resolution.folder shouldBe "1024x768"
  }
})

/**
 * Extracts protected [FileHandleResolver] field.
 */
val ResolutionFileResolver.baseResolver: FileHandleResolver
  get() =
    ResolutionFileResolver::class.java
      .getDeclaredField("baseResolver")
      .apply { isAccessible = true }
      .get(this) as FileHandleResolver

/**
 * Extracts protected [Resolution] array field.
 */
@Suppress("UNCHECKED_CAST")
val ResolutionFileResolver.resolutions: Array<Resolution>
  get() =
    ResolutionFileResolver::class.java
      .getDeclaredField("descriptors")
      .apply { isAccessible = true }
      .get(this) as Array<Resolution>
