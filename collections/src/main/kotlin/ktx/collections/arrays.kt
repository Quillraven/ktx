@file:Suppress("NOTHING_TO_INLINE", "LoopToCallChain")
@file:OptIn(ExperimentalTypeInference::class)

package ktx.collections

import com.badlogic.gdx.utils.Pool
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

/** Alias for [com.badlogic.gdx.utils.Array] avoiding name collision with the standard library. */
typealias GdxArray<Element> = com.badlogic.gdx.utils.Array<Element>

/** Alias for [com.badlogic.gdx.utils.BooleanArray] avoiding name collision with the standard library. */
typealias GdxBooleanArray = com.badlogic.gdx.utils.BooleanArray

/** Alias for [com.badlogic.gdx.utils.FloatArray] avoiding name collision with the standard library. */
typealias GdxFloatArray = com.badlogic.gdx.utils.FloatArray

/** Alias for [com.badlogic.gdx.utils.IntArray] avoiding name collision with the standard library. */
typealias GdxIntArray = com.badlogic.gdx.utils.IntArray

/** Alias for [com.badlogic.gdx.utils.CharArray] avoiding name collision with the standard library. */
typealias GdxCharArray = com.badlogic.gdx.utils.CharArray

/** Alias for [com.badlogic.gdx.utils.LongArray] avoiding name collision with the standard library. */
typealias GdxLongArray = com.badlogic.gdx.utils.LongArray

/** Alias for [com.badlogic.gdx.utils.ShortArray] avoiding name collision with the standard library. */
typealias GdxShortArray = com.badlogic.gdx.utils.ShortArray

/** Alias for [com.badlogic.gdx.utils.ByteArray] avoiding name collision with the standard library. */
typealias GdxByteArray = com.badlogic.gdx.utils.ByteArray

/**
 * Default libGDX array size used by most constructors.
 */
const val defaultArraySize = 16

/**
 * Returns the last valid index for the array. -1 if the array is empty.
 */
inline val <Type> GdxArray<Type>?.lastIndex: Int
  get() = size() - 1

/**
 * Returns the last valid index for the array. -1 if the array is empty.
 */
inline val GdxIntArray?.lastIndex: Int
  get() = size() - 1

/**
 * Returns the last valid index for the array. -1 if the array is empty.
 */
inline val GdxFloatArray?.lastIndex: Int
  get() = size() - 1

/**
 * Returns the last valid index for the array. -1 if the array is empty.
 */
inline val GdxBooleanArray?.lastIndex: Int
  get() = size() - 1

/**
 * @param ordered if false, methods that remove elements may change the order of other elements in the array,
 *      which avoids a memory copy.
 * @param initialCapacity initial size of the backing array.
 * @return a new instance of [Array].
 */
inline fun <reified Type : Any> gdxArrayOf(
  ordered: Boolean = true,
  initialCapacity: Int = defaultArraySize,
): GdxArray<Type> = GdxArray(ordered, initialCapacity, Type::class.java)

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [Array].
 */
inline fun <Type : Any> gdxArrayOf(vararg elements: Type): GdxArray<Type> = GdxArray(elements)

/**
 * A method wrapper over [Array.size] variable compatible with nullable types.
 * @return current amount of elements in the array.
 */
inline fun <Type> GdxArray<Type>?.size(): Int = this?.size ?: 0

/**
 * A method wrapper over [IntArray.size] variable compatible with nullable types.
 * @return current amount of elements in the array.
 */
inline fun GdxIntArray?.size(): Int = this?.size ?: 0

/**
 * A method wrapper over [FloatArray.size] variable compatible with nullable types.
 * @return current amount of elements in the array.
 */
inline fun GdxFloatArray?.size(): Int = this?.size ?: 0

/**
 * A method wrapper over [BooleanArray.size] variable compatible with nullable types.
 * @return current amount of elements in the array.
 */
inline fun GdxBooleanArray?.size(): Int = this?.size ?: 0

/**
 * @return true if the array is null or has no elements.
 */
@OptIn(ExperimentalContracts::class)
inline fun <Type> GdxArray<Type>?.isEmpty(): Boolean {
  contract {
    returns(false) implies (this@isEmpty != null)
  }
  return this == null || this.size == 0
}

/**
 * @return true if the array is not null and contains at least one element.
 */
@OptIn(ExperimentalContracts::class)
inline fun <Type> GdxArray<Type>?.isNotEmpty(): Boolean {
  contract {
    returns(true) implies (this@isNotEmpty != null)
  }
  return this != null && this.size > 0
}

/**
 * @param index index of the element in the array.
 * @param alternative returned if index is out of bounds or the element is null.
 * @return a non-null value of stored element or the alternative.
 */
operator fun <Type> GdxArray<Type>.get(
  index: Int,
  alternative: Type,
): Type {
  if (index >= this.size) return alternative
  return this[index] ?: alternative
}

/**
 * @param elements will be iterated over and added to the array.
 */
fun <Type> GdxArray<Type>.addAll(elements: Iterable<Type>) = elements.forEach { this.add(it) }

/**
 * @param elements will be iterated over and removed from the array.
 * @param identity if true, values will be compared by references. If false, equals method will be invoked.
 */
fun <Type> GdxArray<Type>.removeAll(
  elements: Iterable<Type>,
  identity: Boolean = false,
) = elements.forEach { this.removeValue(it, identity) }

/**
 * @param elements will be iterated over and removed from the array.
 * @param identity if true, values will be compared by references. If false, equals method will be invoked.
 */
fun <Type> GdxArray<Type>.removeAll(
  elements: Array<out Type>,
  identity: Boolean = false,
) = elements.forEach { this.removeValue(it, identity) }

/**
 * Creates a new [GdxArray] with appended [element].
 * @param element will be added at the end of the new array.
 * @return a new [GdxArray] with elements from this array and [element].
 */
operator fun <Type> GdxArray<Type>.plus(element: Type): GdxArray<Type> {
  val result = GdxArray<Type>(size + 1)
  result.addAll(this)
  result.add(element)
  return result
}

/**
 * Creates a new [GdxArray] with appended [elements].
 * @param elements will be added at the end of the new array.
 * @return a new [GdxArray] with elements from this array and [elements].
 */
operator fun <Type> GdxArray<Type>.plus(elements: Iterable<Type>): GdxArray<Type> {
  val result = GdxArray<Type>(this)
  result.addAll(elements)
  return result
}

/**
 * Creates a new [GdxArray] with appended [elements].
 * @param elements will be added at the end of the new array.
 * @return a new [GdxArray] with elements from this array and [elements].
 */
operator fun <Type> GdxArray<Type>.plus(elements: Collection<Type>): GdxArray<Type> {
  val result = GdxArray<Type>(size + elements.size)
  result.addAll(this)
  result.addAll(elements)
  return result
}

/**
 * Allows to quickly addAll all elements of a native array to this array with a pleasant, chainable operator syntax.
 * @param elements will be added to the array.
 * @return this array.
 */
operator fun <Type> GdxArray<Type>.plus(elements: Array<out Type>): GdxArray<Type> {
  val result = GdxArray<Type>(size + elements.size)
  result.addAll(this)
  result.addAll(elements, 0, elements.size)
  return result
}

/**
 * Allows to append elements to arrays with `array += element` syntax.
 * @param element will be added to the array.
 */
operator fun <Type> GdxArray<Type>.plusAssign(element: Type) {
  add(element)
}

/**
 * Allows to quickly add all elements of another iterable to this array with += operator syntax.
 * @param elements will be added to the array.
 */
operator fun <Type> GdxArray<Type>.plusAssign(elements: Iterable<Type>) {
  addAll(elements)
}

/**
 * Allows to quickly add all elements of a native array to this array with += operator syntax.
 * @param elements will be added to the array.
 */
operator fun <Type> GdxArray<Type>.plusAssign(elements: Array<out Type>) {
  addAll(elements, 0, elements.size)
}

/**
 * Allows to remove elements from arrays with `array - element` syntax.
 * @param element will not be copied to the new array.
 * @return a new [GdxArray] with removed element.
 */
operator fun <Type> GdxArray<Type>.minus(element: Type): GdxArray<Type> {
  val result = GdxArray(this)
  result.removeValue(element, false)
  return result
}

/**
 * Allows to quickly remove all elements of another iterable from this array with - operator syntax.
 * @param elements will not be copied to the new array.
 * @return a new [GdxArray] with removed elements.
 */
operator fun <Type> GdxArray<Type>.minus(elements: Iterable<Type>): GdxArray<Type> {
  val result = GdxArray(this)
  result.removeAll(elements)
  return result
}

/**
 * Allows to quickly remove all elements of a native array from this array with -= operator syntax.
 * @param elements will not be copied to the new array.
 * @return a new [GdxArray] with removed elements.
 */
operator fun <Type> GdxArray<Type>.minus(elements: Array<out Type>): GdxArray<Type> {
  val result = GdxArray(this)
  result.removeAll(elements)
  return result
}

/**
 * Allows to remove elements from arrays with `array -= element` syntax.
 * @param element will be removed from the array.
 * @return this array.
 */
operator fun <Type> GdxArray<Type>.minusAssign(element: Type) {
  removeValue(element, false)
}

/**
 * Allows to quickly remove all elements of another iterable from this array with -= operator syntax.
 * @param elements will be removed from the array.
 */
operator fun <Type> GdxArray<Type>.minusAssign(elements: Iterable<Type>) {
  removeAll(elements)
}

/**
 * Allows to quickly remove all elements of a native array from this array with -= operator syntax.
 * @param elements will be removed from the array.
 */
operator fun <Type> GdxArray<Type>.minusAssign(elements: Array<out Type>) {
  removeAll(elements)
}

/**
 * Removes the element at the given [index], like [GdxArray.removeIndex].
 * @param index index of the element to remove.
 * @return the removed element.
 */
inline fun <Type> GdxArray<Type>.removeAt(index: Int): Type = removeIndex(index)

/**
 * Allows to check if an array contains an element using the "in" operator.
 * @param element might be in the array.
 * @return true if the element is equal to any value stored in the array.
 */
operator fun <Type> GdxArray<Type>.contains(element: Type): Boolean = this.contains(element, false)

/**
 * Checks if the array contains the [element] using reference comparison (`===`). Use the `in` operator to compare
 * elements with `equals` instead.
 * @param element might be in the array.
 * @return true if the exact same instance is stored in the array.
 */
inline fun <Type> GdxArray<Type>.containsIdentity(element: Type): Boolean = this.contains(element, true)

/**
 * Allows to iterate over the array with access to [MutableIterator], which allows to remove elements from the collection
 * during iteration.
 * @param action will be invoked for each array element. Allows to remove elements during iteration. The first function
 *      argument is the element from the array, the second is the array iterator. The iterator is guaranteed to be the
 *      same instance during one iteration.
 */
inline fun <Type> GdxArray<Type>.iterate(action: (Type, MutableIterator<Type>) -> Unit) {
  val iterator = iterator()
  while (iterator.hasNext()) action.invoke(iterator.next(), iterator)
}

/**
 * Sorts elements in the array in-place descending according to their natural sort order.
 */
fun <Type : Comparable<Type>> GdxArray<out Type>.sortDescending() {
  this.sort(reverseOrder())
}

/**
 * Sorts elements in the array in-place according to natural sort order of the value returned by specified [selector] function.
 */
inline fun <Type, R : Comparable<R>> GdxArray<out Type>.sortBy(crossinline selector: (Type) -> R?) {
  if (size > 1) this.sort(compareBy(selector))
}

/**
 * Sorts elements in the array in-place descending according to natural sort order of the value returned by specified [selector] function.
 */
inline fun <Type, R : Comparable<R>> GdxArray<out Type>.sortByDescending(crossinline selector: (Type) -> R?) {
  if (size > 1) this.sort(compareByDescending(selector))
}

/**
 * Removes elements from the array that satisfy the [predicate].
 * @param pool Removed items are freed to this pool.
 * @return true if the array was modified, false otherwise.
 */
inline fun <Type> GdxArray<Type>.removeAll(
  pool: Pool<Type>? = null,
  predicate: (Type) -> Boolean,
): Boolean {
  var currentWriteIndex = 0
  for (i in 0 until size) {
    val value = items[i]
    if (!predicate(value)) {
      if (currentWriteIndex != i) {
        items[currentWriteIndex] = value
      }
      currentWriteIndex++
    } else {
      pool?.free(value)
    }
  }
  if (currentWriteIndex < size) {
    truncate(currentWriteIndex)
    return true
  }
  return false
}

/**
 * Removes elements from the array that do not satisfy the [predicate].
 * @param pool Removed items are freed to this optional pool.
 * @return true if the array was modified, false otherwise.
 */
inline fun <Type> GdxArray<Type>.retainAll(
  pool: Pool<Type>? = null,
  predicate: (Type) -> Boolean,
): Boolean {
  var currentWriteIndex = 0
  for (i in 0 until size) {
    val value = items[i]
    if (predicate(value)) {
      if (currentWriteIndex != i) {
        items[currentWriteIndex] = value
      }
      currentWriteIndex++
    } else {
      pool?.free(value)
    }
  }
  if (currentWriteIndex < size) {
    truncate(currentWriteIndex)
    return true
  }
  return false
}

/**
 * Transfers elements that match the [predicate] into the selected [toArray].
 * The elements will be removed from this array and added [toArray].
 */
inline fun <Type : T, T> GdxArray<Type>.transfer(
  toArray: GdxArray<T>,
  predicate: (Type) -> Boolean,
) {
  var currentWriteIndex = 0
  for (i in 0 until size) {
    val value = items[i]
    if (predicate(value)) {
      toArray.add(value)
    } else {
      if (currentWriteIndex != i) {
        items[currentWriteIndex] = value
      }
      currentWriteIndex++
    }
  }
  truncate(currentWriteIndex)
}

/**
 * Returns a [GdxArray] containing the results of applying the given [transform] function
 * to each element in the original [GdxArray].
 */
inline fun <Type, R> GdxArray<Type>.map(transform: (Type) -> R): GdxArray<R> {
  val destination = GdxArray<R>(this.size)
  for (item in this) {
    destination.add(transform(item))
  }
  return destination
}

/**
 * Invokes [action] for each element of the array in order. Unlike [Iterable.forEach] and [iterate], this does not
 * use [GdxArray.iterator], which means it does not allocate and can be safely nested - even with other iterations
 * over the same array. The array must not be structurally modified by [action].
 *
 * Named `forEachItem` because an extension called `forEach` would be shadowed by the member
 * `java.lang.Iterable.forEach` inherited by [GdxArray], which does use the shared iterator.
 */
inline fun <Type> GdxArray<Type>.forEachItem(action: (Type) -> Unit) {
  for (i in 0 until size) action(items[i])
}

/**
 * Invokes [action] for each element of the array in order, passing its index. Does not use [GdxArray.iterator], so it
 * does not allocate and can be safely nested. The array must not be structurally modified by [action].
 */
inline fun <Type> GdxArray<Type>.forEachIndexed(action: (Int, Type) -> Unit) {
  for (i in 0 until size) action(i, items[i])
}

/**
 * @return true if at least one element matches the [predicate]. False for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
inline fun <Type> GdxArray<Type>.any(predicate: (Type) -> Boolean): Boolean {
  for (i in 0 until size) if (predicate(items[i])) return true
  return false
}

/**
 * @return true if all elements match the [predicate]. True for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
inline fun <Type> GdxArray<Type>.all(predicate: (Type) -> Boolean): Boolean {
  for (i in 0 until size) if (!predicate(items[i])) return false
  return true
}

/**
 * @return true if no elements match the [predicate]. True for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
inline fun <Type> GdxArray<Type>.none(predicate: (Type) -> Boolean): Boolean = !any(predicate)

/**
 * @return the number of elements matching the [predicate]. Zero for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
inline fun <Type> GdxArray<Type>.count(predicate: (Type) -> Boolean): Int {
  var result = 0
  for (i in 0 until size) if (predicate(items[i])) result++
  return result
}

/**
 * @return the first element or null if the array is empty.
 */
inline fun <Type> GdxArray<Type>.firstOrNull(): Type? = if (size == 0) null else items[0]

/**
 * @return the first element matching the [predicate] or null if there is none.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
inline fun <Type> GdxArray<Type>.firstOrNull(predicate: (Type) -> Boolean): Type? {
  for (i in 0 until size) {
    val value = items[i]
    if (predicate(value)) return value
  }
  return null
}

/**
 * @return the first element matching the [predicate] or null if there is none. Alias for [firstOrNull].
 */
inline fun <Type> GdxArray<Type>.find(predicate: (Type) -> Boolean): Type? = firstOrNull(predicate)

/**
 * @return the sum of values returned by [selector] for each element. Zero for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
@OverloadResolutionByLambdaReturnType
@kotlin.jvm.JvmName("sumOfInt")
inline fun <Type> GdxArray<Type>.sumOf(selector: (Type) -> Int): Int {
  var sum = 0
  for (i in 0 until size) sum += selector(items[i])
  return sum
}

/**
 * @return the sum of values returned by [selector] for each element. Zero for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
@OverloadResolutionByLambdaReturnType
@kotlin.jvm.JvmName("sumOfLong")
inline fun <Type> GdxArray<Type>.sumOf(selector: (Type) -> Long): Long {
  var sum = 0L
  for (i in 0 until size) sum += selector(items[i])
  return sum
}

/**
 * @return the sum of values returned by [selector] for each element. Zero for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
@OverloadResolutionByLambdaReturnType
@kotlin.jvm.JvmName("sumOfFloat")
inline fun <Type> GdxArray<Type>.sumOf(selector: (Type) -> Float): Float {
  var sum = 0f
  for (i in 0 until size) sum += selector(items[i])
  return sum
}

/**
 * @return the sum of values returned by [selector] for each element. Zero for an empty array.
 * Does not use [GdxArray.iterator], so it does not allocate and can be safely nested.
 */
@OverloadResolutionByLambdaReturnType
@kotlin.jvm.JvmName("sumOfDouble")
inline fun <Type> GdxArray<Type>.sumOf(selector: (Type) -> Double): Double {
  var sum = 0.0
  for (i in 0 until size) sum += selector(items[i])
  return sum
}

/**
 * Invokes [action] for each element of the array in order. Does not allocate and cannot cause nested iterator errors.
 * The array must not be structurally modified by [action].
 */
inline fun GdxIntArray.forEach(action: (Int) -> Unit) {
  for (i in 0 until size) action(items[i])
}

/**
 * Appends the results of applying [transform] to each element of this array to the [destination] array.
 * Does not use [GdxArray.iterator], so it can be safely nested.
 * @return the [destination] array.
 */
inline fun <Type, R, C : GdxArray<in R>> GdxArray<Type>.mapTo(
  destination: C,
  transform: (Type) -> R,
): C {
  for (i in 0 until size) destination.add(transform(items[i]))
  return destination
}

/**
 * Returns a [GdxArray] containing the results of applying [transform] to each element of this iterable.
 * Avoids the intermediate list created by `map { }.toGdxArray()`.
 * @param ordered if false, methods that remove elements may change the order of other elements in the array,
 *      which avoids a memory copy.
 * @param initialCapacity initial size of the backing array. Defaults to the collection size if known.
 */
inline fun <Type, reified R : Any> Iterable<Type>.mapToGdxArray(
  ordered: Boolean = true,
  initialCapacity: Int = if (this is Collection<*>) this.size else defaultArraySize,
  transform: (Type) -> R,
): GdxArray<R> {
  val destination = GdxArray<R>(ordered, initialCapacity, R::class.java)
  for (item in this) destination.add(transform(item))
  return destination
}

/**
 * Returns a [GdxArray] containing only elements matching the given [predicate].
 */
inline fun <Type> GdxArray<Type>.filter(predicate: (Type) -> Boolean): GdxArray<Type> {
  val destination = GdxArray<Type>()
  for (item in this) {
    if (predicate(item)) {
      destination.add(item)
    }
  }
  return destination
}

/**
 * Returns a single [GdxArray] of all elements from all collections in the given [GdxArray].
 */
inline fun <Type, C : Iterable<Type>> GdxArray<out C>.flatten(): GdxArray<Type> {
  val destination = GdxArray<Type>()
  for (item in this) {
    destination.addAll(item)
  }
  return destination
}

/**
 * Returns a single [GdxArray] of all elements yielded from results of transform function being invoked
 * on each entry of original [GdxArray].
 */
inline fun <Type, R> GdxArray<Type>.flatMap(transform: (Type) -> Iterable<R>): GdxArray<R> = this.map(transform).flatten()

/**
 * @param initialCapacity initial capacity of the set. Will be resized if necessary. Defaults to array size.
 * @param loadFactor decides how many elements the set might contain in relation to its total capacity before it is resized.
 * @return values copied from this array stored in a libGDX set.
 */
fun <Type : Any> GdxArray<Type>.toGdxSet(
  initialCapacity: Int = this.size,
  loadFactor: Float = defaultLoadFactor,
): GdxSet<Type> {
  val set = GdxSet<Type>(initialCapacity, loadFactor)
  set.addAll(this)
  return set
}

/**
 * @param ordered if false, methods that remove elements may change the order of other elements in the array,
 *      which avoids a memory copy.
 * @param initialCapacity initial size of the backing array.
 * @return values copied from this iterable stored in a libGDX array.
 */
inline fun <reified Type : Any> Iterable<Type>.toGdxArray(
  ordered: Boolean = true,
  initialCapacity: Int = defaultArraySize,
): GdxArray<Type> {
  val array = GdxArray<Type>(ordered, initialCapacity, Type::class.java)
  array.addAll(this)
  return array
}

/**
 * @param ordered if false, methods that remove elements may change the order of other elements in the array,
 *      which avoids a memory copy.
 * @param initialCapacity initial size of the backing array. Defaults to this array size.
 * @return values copied from this array stored in a libGDX array.
 */
inline fun <reified Type : Any> Array<Type>.toGdxArray(
  ordered: Boolean = true,
  initialCapacity: Int = this.size,
): GdxArray<Type> {
  val array = GdxArray<Type>(ordered, initialCapacity, Type::class.java)
  array.addAll(this, 0, this.size)
  return array
}

/**
 * @param ordered if false, methods that remove elements may change the order of other elements in the array,
 *      which avoids a memory copy.
 * @param initialCapacity initial size of the backing array. Defaults to this array size.
 * @return values copied from this array stored in an optimized libGDX int array.
 */
fun IntArray.toGdxArray(
  ordered: Boolean = true,
  initialCapacity: Int = this.size,
): GdxIntArray {
  val array = GdxIntArray(ordered, initialCapacity)
  array.addAll(this, 0, this.size)
  return array
}

/**
 * @param ordered if false, methods that remove elements may change the order of other elements in the array,
 *      which avoids a memory copy.
 * @param initialCapacity initial size of the backing array. Defaults to this array size.
 * @return values copied from this array stored in an optimized libGDX float array.
 */
fun FloatArray.toGdxArray(
  ordered: Boolean = true,
  initialCapacity: Int = this.size,
): GdxFloatArray {
  val array = GdxFloatArray(ordered, initialCapacity)
  array.addAll(this, 0, this.size)
  return array
}

/**
 * @param ordered if false, methods that remove elements may change the order of other elements in the array,
 *      which avoids a memory copy.
 * @param initialCapacity initial size of the backing array. Defaults to this array size.
 * @return values copied from this array stored in an optimized libGDX boolean array.
 */
fun BooleanArray.toGdxArray(
  ordered: Boolean = true,
  initialCapacity: Int = this.size,
): GdxBooleanArray {
  val array = GdxBooleanArray(ordered, initialCapacity)
  array.addAll(this, 0, this.size)
  return array
}

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [GdxBooleanArray].
 */
fun gdxBooleanArrayOf(vararg elements: Boolean): GdxBooleanArray = GdxBooleanArray(elements)

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [GdxByteArray].
 */
fun gdxByteArrayOf(vararg elements: Byte): GdxByteArray = GdxByteArray(elements)

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [GdxCharArray].
 */
fun gdxCharArrayOf(vararg elements: Char): GdxCharArray = GdxCharArray(elements)

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [GdxShortArray].
 */
fun gdxShortArrayOf(vararg elements: Short): GdxShortArray = GdxShortArray(elements)

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [GdxIntArray].
 */
fun gdxIntArrayOf(vararg elements: Int): GdxIntArray = GdxIntArray(elements)

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [GdxLongArray].
 */
fun gdxLongArrayOf(vararg elements: Long): GdxLongArray = GdxLongArray(elements)

/**
 * @param elements will be initially stored in the array.
 * @return a new instance of [GdxFloatArray].
 */
fun gdxFloatArrayOf(vararg elements: Float): GdxFloatArray = GdxFloatArray(elements)
