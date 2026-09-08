package ktx.script

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URL
import kotlin.reflect.KClass
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

/**
 * Collects the current application classpath, including the directories and JARs visible to the class loaders used
 * by the calling code. This is required for scripts to access the classes of the application that uses the engine.
 */
private fun currentClasspath(): List<File> {
  val files = LinkedHashSet<File>()
  System.getProperty("java.class.path")?.splitToSequence(File.pathSeparator)?.forEach { files += File(it) }
  var loader: ClassLoader? = Thread.currentThread().contextClassLoader ?: KotlinScriptEngine::class.java.classLoader
  while (loader != null) {
    addClasspath(loader, files)
    loader = loader.parent
  }
  return files.toList()
}

private fun addClasspath(
  loader: ClassLoader,
  files: MutableSet<File>,
) {
  try {
    val urls = loader.javaClass.getMethod("getUrls").invoke(loader) as? Array<*>
    for (url in urls.orEmpty()) {
      (url as? URL)?.toFileOrNull()?.let { files += it }
    }
  } catch (_: ReflectiveOperationException) {
    try {
      val classPath = loader.javaClass.getMethod("getClassPath").invoke(loader) as? String
      classPath?.splitToSequence(File.pathSeparator)?.forEach { files += File(it) }
    } catch (_: ReflectiveOperationException) {
      // Class loader does not expose its classpath; the Java system classpath was already collected.
    }
  }
}

private fun URL.toFileOrNull(): File? = if (protocol == "file") runCatching { File(toURI()) }.getOrNull() else null

/**
 * Executes Kotlin scripts in runtime.
 *
 * Built on top of the official Kotlin scripting host from the `kotlin-scripting-jvm-host` package.
 */
class KotlinScriptEngine {
  private val host = BasicJvmScriptingHost()

  private val imports = mutableListOf<String>()
  val variables = mutableMapOf<String, Any?>()
  private var scriptPackage: String? = null

  /**
   * Imports the selected [import] using an optional [alias]. Wildcard imports using `*` are supported, but they
   * cannot have an [alias].
   *
   * If an [alias] is given, the [import] will be executed with a Kotlin import alias using the `as` operator.
   * The [import] will be available in future scripts.
   *
   * Example usage: `engine.import("com.badlogic.gdx.utils.*")`
   *
   * When performing multiple imports at once, use [importAll] instead.
   */
  fun import(
    import: String,
    alias: String? = null,
  ) {
    imports.add(if (alias.isNullOrBlank()) import else "$import as $alias")
  }

  /**
   * Imports the selected [imports] within the script context. Wildcard imports using `*` are accepted.
   * The [imports] will be available in future scripts.
   *
   * To assign an alias to a specific import, use Kotlin `as` operator after the qualified name.
   * For example: `engine.importAll("com.badlogic.gdx.utils.Array as GdxArray")`
   */
  fun importAll(vararg imports: String) {
    this.imports.addAll(imports)
  }

  /**
   * Imports the selected [imports] within the script context. Wildcard imports using `*` are accepted.
   * The [imports] will be available in future scripts.
   *
   * To assign an alias to a specific import, use Kotlin `as` operator after the qualified name.
   * For example: `engine.importAll("com.badlogic.gdx.utils.Array as GdxArray")`
   */
  fun importAll(imports: Iterable<String>) {
    this.imports.addAll(imports)
  }

  /**
   * Sets the package of the future scripts to [name].
   *
   * Note that the package can be changed at any time; the package of future scripts will be updated accordingly.
   * To execute scripts from within multiple packages at once, create multiple script engines.
   */
  fun setPackage(name: String) {
    scriptPackage = name
  }

  /**
   * Retrieves the value assigned to [variable] in the context of this [engine].
   */
  inline operator fun <reified T> get(variable: String): T? = variables[variable] as? T

  /**
   * Assigns the selected [value] to the [variable] name in the context of this [engine].
   * The [variable] will be available in the future scripts.
   */
  operator fun <T> set(
    variable: String,
    value: T,
  ) {
    variables[variable] = value
  }

  /**
   * Removes the [variable] from the context of this [engine]. Returns the value assigned to the [variable].
   * Returns null if no value is assigned to [variable].
   */
  fun remove(variable: String): Any? = variables.remove(variable)

  /**
   * Executes the selected [script]. Returns the last script's expression as the result.
   * If unable to execute the script, [ScriptEngineException] will be thrown.
   */
  fun evaluate(script: String): Any? = evaluate(script, "script.kts")

  /**
   * Executes the selected [scriptFile]. Returns the last script's expression as the result.
   * If unable to execute the script, [ScriptEngineException] will be thrown.
   */
  fun evaluate(scriptFile: FileHandle): Any? = evaluate(scriptFile.readString(), scriptFile.name())

  /**
   * Executes the selected [script] on the [receiver] object. The [receiver] will be available as `this`
   * throughout the script.
   *
   * Note that the script can contain import statements.
   * Use [import] or [importAll] instead to reuse them in future scripts.
   */
  fun evaluateOn(
    receiver: Any,
    script: String,
  ): Any? = evaluate(script, "script.kts", receiver)

  /**
   * Executes the selected [scriptFile] on the [receiver] object. The [receiver] will be available as `this`
   * throughout the script.
   *
   * Note that the script can contain import statements.
   * Use [import] or [importAll] instead to reuse them in future scripts.
   */
  fun evaluateOn(
    receiver: Any,
    scriptFile: FileHandle,
  ): Any? = evaluate(scriptFile.readString(), scriptFile.name(), receiver)

  /**
   * Executes the selected [script] and returns an instance of [T]. If the script is not an instance of [T],
   * [ClassCastException] will be thrown. If unable to execute the script, [ScriptEngineException] will be thrown.
   */
  inline fun <reified T> evaluateAs(script: String): T = evaluate(script) as T

  /**
   * Executes the selected [scriptFile] and returns an instance of [T]. If the script is not an instance of [T],
   * [ClassCastException] will be thrown. If unable to execute the script, [ScriptEngineException] will be thrown.
   */
  inline fun <reified T> evaluateAs(scriptFile: FileHandle): T = evaluate(scriptFile) as T

  private fun evaluate(
    script: String,
    name: String,
    receiver: Any? = null,
  ): Any? {
    val scriptSource =
      buildString {
        scriptPackage?.let { append("package $it\n") }
        for (imported in imports) append("import $imported\n")
        append(script)
      }
    val nonNullVariables = variables.filterValues { it != null }
    val result =
      runBlocking {
        host.eval(
          scriptSource.toScriptSource(name),
          compilationConfiguration(receiver?.javaClass?.kotlin, nonNullVariables),
          evaluationConfiguration(receiver, nonNullVariables),
        )
      }
    return when (result) {
      is ResultWithDiagnostics.Success -> {
        when (val resultValue = result.value.returnValue) {
          is ResultValue.Value -> {
            resultValue.value
          }

          is ResultValue.Error -> {
            throw ScriptEngineException("Unable to execute Kotlin script:\n$script", resultValue.error)
          }

          else -> {
            null
          }
        }
      }

      is ResultWithDiagnostics.Failure -> {
        throw ScriptEngineException(
          "Unable to execute Kotlin script:\n$script\n${result.reports.joinToString(separator = "\n") { it.message }}",
          result.reports.firstNotNullOfOrNull { it.exception },
        )
      }
    }
  }

  private fun compilationConfiguration(
    receiverType: KClass<*>?,
    variables: Map<String, Any?>,
  ): ScriptCompilationConfiguration =
    ScriptCompilationConfiguration {
      providedProperties(
        *variables
          .mapNotNull { (name, value) -> value?.let { name to it.javaClass.kotlin } }
          .toTypedArray(),
      )
      if (receiverType != null) implicitReceivers(receiverType)
      jvm {
        updateClasspath(currentClasspath())
      }
    }

  private fun evaluationConfiguration(
    receiver: Any?,
    variables: Map<String, Any?>,
  ): ScriptEvaluationConfiguration =
    ScriptEvaluationConfiguration {
      set(providedProperties, variables)
      if (receiver != null) set(implicitReceivers, listOf(receiver))
    }
}

/**
 * Thrown when unable to execute a script or configure the scripting engine.
 */
class ScriptEngineException(
  message: String,
  cause: Throwable? = null,
) : GdxRuntimeException(message, cause)
