package ktx.freetype

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.backends.lwjgl.LwjglFiles
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.mock

/**
 * Tests FreeType font loading utilities.
 *
 * Uses Hack font for testing. See https://github.com/source-foundry/Hack for details.
 */
open class FreeTypeTest : FunSpec() {
  protected open val ttfFile = "ktx/freetype/hack.ttf"
  protected open val otfFile = "ktx/freetype/hack.otf"

  protected open fun assetManager() = AssetManager(ClasspathFileHandleResolver())

  init {
    test("should initiate font loading parameters") {
      val variable: Int

      val parameters =
        freeTypeFontParameters("file.ttf") {
          size = 12
          flip = false
          variable = 42
        }

      parameters.fontFileName shouldBe "file.ttf"
      parameters.fontParameters.size shouldBe 12
      parameters.fontParameters.flip shouldBe false
      variable shouldBe 42
    }

    test("should register FreeType font loaders") {
      val assetManager = assetManager()

      assetManager.registerFreeTypeFontLoaders()

      (assetManager.getLoader(FreeTypeFontGenerator::class.java) is FreeTypeFontGeneratorLoader) shouldBe true
      (assetManager.getLoader(BitmapFont::class.java, ".ttf") is FreetypeFontLoader) shouldBe true
      (assetManager.getLoader(BitmapFont::class.java, ".otf") is FreetypeFontLoader) shouldBe true
    }

    test("should register FreeType font loaders with custom extensions") {
      val assetManager = assetManager()

      assetManager.registerFreeTypeFontLoaders(fileExtensions = arrayOf(".custom"))

      (assetManager.getLoader(FreeTypeFontGenerator::class.java) is FreeTypeFontGeneratorLoader) shouldBe true
      (assetManager.getLoader(BitmapFont::class.java, ".custom") is FreetypeFontLoader) shouldBe true
      // Should not register loader for unlisted extensions:
      (assetManager.getLoader(BitmapFont::class.java, ".ttf") is FreetypeFontLoader) shouldBe false
      (assetManager.getLoader(BitmapFont::class.java, ".otf") is FreetypeFontLoader) shouldBe false
    }

    test("should register FreeType font loaders and override default BitmapFont loader") {
      val assetManager = assetManager()

      assetManager.registerFreeTypeFontLoaders(replaceDefaultBitmapFontLoader = true)

      (assetManager.getLoader(FreeTypeFontGenerator::class.java) is FreeTypeFontGeneratorLoader) shouldBe true
      (assetManager.getLoader(BitmapFont::class.java) is FreetypeFontLoader) shouldBe true
    }

    test("should load OTF file into BitmapFont") {
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()

      val asset = assetManager.loadFreeTypeFont(otfFile)

      asset.finishLoading()
      val font = assetManager.get<BitmapFont>(otfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset.asset
    }

    test("should load OTF file into BitmapFont with custom parameters") {
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      val variable: Int

      val asset =
        assetManager.loadFreeTypeFont(otfFile) {
          size = 12
          borderWidth = 1f
          variable = 42
        }

      asset.finishLoading()
      val font = assetManager.get<BitmapFont>(otfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset.asset
      variable shouldBe 42
    }

    test("should load TTF file into BitmapFont") {
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()

      val asset = assetManager.loadFreeTypeFont(ttfFile)

      asset.finishLoading()
      val font = assetManager.get<BitmapFont>(ttfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset.asset
    }

    test("should load TTF file into BitmapFont with custom parameters") {
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      val variable: Int

      val asset =
        assetManager.loadFreeTypeFont(ttfFile) {
          size = 12
          borderWidth = 1f
          variable = 42
        }

      asset.finishLoading()
      val font = assetManager.get<BitmapFont>(ttfFile)
      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
      font shouldBeSameInstanceAs asset.asset
      variable shouldBe 42
    }

    test("should use FreeType loader to load OTF file into BitmapFont") {
      // Note that this tests uses "raw" libGDX AssetManager API without font loading utilities.
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      assetManager.load(otfFile, BitmapFont::class.java, freeTypeFontParameters(otfFile))
      assetManager.finishLoading()

      val font = assetManager.get<BitmapFont>(otfFile)

      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
    }

    test("should use FreeType loader to load TTF file into BitmapFont") {
      // Note that this tests uses "raw" libGDX AssetManager API without font loading utilities.
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      assetManager.load(ttfFile, BitmapFont::class.java, freeTypeFontParameters(ttfFile))
      assetManager.finishLoading()

      val font = assetManager.get<BitmapFont>(ttfFile)

      font.shouldNotBeNull()
      (font is BitmapFont) shouldBe true
    }

    test("should load OTF file into FreeTypeFontGenerator") {
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      assetManager.load(otfFile, FreeTypeFontGenerator::class.java)
      assetManager.finishLoading()

      val fontGenerator = assetManager.get<FreeTypeFontGenerator>(otfFile)

      fontGenerator.shouldNotBeNull()
      (fontGenerator is FreeTypeFontGenerator) shouldBe true
    }

    test("should load TTF file into FreeTypeFontGenerator") {
      val assetManager = assetManager()
      assetManager.registerFreeTypeFontLoaders()
      assetManager.load(ttfFile, FreeTypeFontGenerator::class.java)
      assetManager.finishLoading()

      val fontGenerator = assetManager.get<FreeTypeFontGenerator>(ttfFile)

      fontGenerator.shouldNotBeNull()
      (fontGenerator is FreeTypeFontGenerator) shouldBe true
    }

    test("should generate font from TTF file") {
      val generator = FreeTypeFontGenerator(Gdx.files.classpath(ttfFile))

      val font = generator.generateFont()

      font.shouldNotBeNull()
    }

    test("should generate font from TTF file with custom parameters ") {
      val generator = FreeTypeFontGenerator(Gdx.files.classpath(ttfFile))
      val variable: Int

      val font =
        generator.generateFont {
          size = 40
          variable = 42
        }

      font.shouldNotBeNull()
      variable shouldBe 42
    }

    test("should generate font from OTF file") {
      val generator = FreeTypeFontGenerator(Gdx.files.classpath(otfFile))

      val font = generator.generateFont()

      font.shouldNotBeNull()
    }

    test("should generate font from OTF file with custom parameters ") {
      val generator = FreeTypeFontGenerator(Gdx.files.classpath(otfFile))
      val variable: Int

      val font =
        generator.generateFont {
          size = 40
          variable = 42
        }

      font.shouldNotBeNull()
      variable shouldBe 42
    }

    beforeTest {
      LwjglNativesLoader.load()
      Gdx.gl = mock()
      Gdx.gl20 = Gdx.gl
      Gdx.graphics = mock()
      Gdx.files = LwjglFiles()
    }

    afterTest {
      Gdx.gl = null
      Gdx.gl20 = null
      Gdx.files = null
      Gdx.graphics = null
    }
  }
}
