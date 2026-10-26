package ktx.async

import com.badlogic.gdx.Gdx
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@OptIn(DelicateCoroutinesApi::class)
class KtxAsyncTest : AsyncTest() {
  init {
    test("should execute tasks on the main rendering thread when launched via KtxAsync") {
      // Given:
      val thread = CompletableFuture<Thread>()

      // When:
      KtxAsync.launch {
        thread.complete(Thread.currentThread())
      }

      // Then:
      getMainRenderingThread() shouldBeSameInstanceAs thread.join()
      Thread.currentThread() shouldNotBeSameInstanceAs thread.get()
    }

    test("should expose main KTX dispatcher via Dispatchers object") {
      // Expect:
      Dispatchers.KTX shouldBeSameInstanceAs MainDispatcher
    }

    test("should create a single-threaded AsyncExecutorDispatcher") {
      // When:
      val dispatcher: AsyncExecutorDispatcher = newSingleThreadAsyncContext()

      // Then:
      dispatcher.threads shouldBe 1
    }

    test("should create a single-threaded AsyncExecutorDispatcher with selected name") {
      // When:
      val dispatcher: AsyncExecutorDispatcher = newSingleThreadAsyncContext(threadName = "MyThreadName")

      // Then:
      dispatcher.threads shouldBe 1
      ("MyThreadName" in getExecutionThread(dispatcher.executor).name) shouldBe true
    }

    test("should create a multi-threaded AsyncExecutorDispatcher") {
      // When:
      val dispatcher: AsyncExecutorDispatcher = newAsyncContext(threads = 4)

      // Then:
      dispatcher.threads shouldBe 4
    }

    test("should create a multi-threaded AsyncExecutorDispatcher with selected name") {
      // When:
      val dispatcher: AsyncExecutorDispatcher = newAsyncContext(threads = 4, threadName = "MyThreadName")

      // Then:
      dispatcher.threads shouldBe 4
      ("MyThreadName" in getExecutionThread(dispatcher.executor).name) shouldBe true
    }

    test("should execute on the main rendering thread") {
      // Given:
      val mainThread = CompletableFuture<Thread>()
      val executionThread = CompletableFuture<Thread>()

      // When:
      GlobalScope.launch {
        onRenderingThread {
          executionThread.complete(Thread.currentThread())
        }
        mainThread.complete(Thread.currentThread())
      }

      // Then:
      mainThread.join()
      executionThread.get() shouldNotBeSameInstanceAs mainThread.get()
      executionThread.get() shouldBeSameInstanceAs getMainRenderingThread()
    }

    test("should execute on the main rendering thread and return the result") {
      // Given:
      val mainThread = CompletableFuture<Thread>()
      val executionThread = CompletableFuture<Thread>()
      val result = AtomicInteger()

      // When:
      GlobalScope.launch {
        val value =
          onRenderingThread {
            executionThread.complete(Thread.currentThread())
            42
          }
        result.set(value)
        mainThread.complete(Thread.currentThread())
      }

      // Then:
      mainThread.join()
      result.get() shouldBe 42
      executionThread.get() shouldNotBeSameInstanceAs mainThread.get()
      executionThread.get() shouldBeSameInstanceAs getMainRenderingThread()
    }

    test("should skip a single frame on the main rendering thread") {
      // Given:
      val initialFrame = AtomicLong()
      val finalFrame = AtomicLong()
      val threadAfterSkip = CompletableFuture<Thread>()

      // When:
      KtxAsync.launch {
        initialFrame.set(Gdx.graphics.frameId)
        skipFrame()
        finalFrame.set(Gdx.graphics.frameId)
        threadAfterSkip.complete(Thread.currentThread())
      }

      // Then:
      getMainRenderingThread() shouldBeSameInstanceAs threadAfterSkip.join()
      finalFrame.get() shouldBe (initialFrame.get() + 1)
    }

    test("should detect rendering thread") {
      // Given:
      val mainThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(false)

      // When:
      KtxAsync.launch {
        isOnRenderingThread.set(isOnRenderingThread())
        mainThread.complete(Thread.currentThread())
      }

      // Then:
      mainThread.join() shouldBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe true
    }

    test("should detect rendering thread from coroutine with different context") {
      // Given:
      val executionThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(true)
      val dispatcher: AsyncExecutorDispatcher = newSingleThreadAsyncContext()

      // When:
      KtxAsync.launch(dispatcher) {
        onRenderingThread {
          isOnRenderingThread.set(isOnRenderingThread())
          executionThread.complete(Thread.currentThread())
        }
      }

      // Then:
      executionThread.join() shouldBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe true
    }

    test("should detect rendering thread from coroutine launched from global context") {
      // Given:
      val executionThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(false)

      // When:
      GlobalScope.launch {
        onRenderingThread {
          isOnRenderingThread.set(isOnRenderingThread())
          executionThread.complete(Thread.currentThread())
        }
      }

      // Then:
      executionThread.join() shouldBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe true
    }

    test("should detect non-rendering threads") {
      // Given:
      val executionThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(true)
      val dispatcher: AsyncExecutorDispatcher = newSingleThreadAsyncContext()

      // When:
      KtxAsync.launch(dispatcher) {
        isOnRenderingThread.set(isOnRenderingThread())
        executionThread.complete(Thread.currentThread())
      }

      // Then:
      executionThread.join() shouldNotBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe false
    }

    test("should detect non-rendering thread launched from global scope") {
      // Given:
      val executionThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(true)

      // When:
      GlobalScope.launch {
        isOnRenderingThread.set(isOnRenderingThread())
        executionThread.complete(Thread.currentThread())
      }

      // Then:
      executionThread.join() shouldNotBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe false
    }

    test("should detect nested non-rendering threads") {
      // Given:
      val executionThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(true)
      val dispatcher: AsyncExecutorDispatcher = newSingleThreadAsyncContext()

      // When:
      KtxAsync.launch(Dispatchers.KTX) {
        KtxAsync.launch(dispatcher) {
          isOnRenderingThread.set(isOnRenderingThread())
          executionThread.complete(Thread.currentThread())
        }
      }

      // Then:
      executionThread.join() shouldNotBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe false
    }

    test("should detect nested non-rendering threads with separate context") {
      // Given:
      val executionThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(true)
      val dispatcher: AsyncExecutorDispatcher = newSingleThreadAsyncContext()

      // When:
      KtxAsync.launch(Dispatchers.KTX) {
        withContext(dispatcher) {
          isOnRenderingThread.set(isOnRenderingThread())
          executionThread.complete(Thread.currentThread())
        }
      }

      // Then:
      executionThread.join() shouldNotBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe false
    }

    test("should detect non-rendering threads with context switch") {
      // Given:
      val executionThread = CompletableFuture<Thread>()
      val isOnRenderingThread = AtomicBoolean(true)
      val dispatcher: AsyncExecutorDispatcher = newSingleThreadAsyncContext()

      // When:
      KtxAsync.launch(Dispatchers.KTX) {
        withContext(dispatcher) {
          isOnRenderingThread.set(isOnRenderingThread())
          executionThread.complete(Thread.currentThread())
        }
      }

      // Then:
      executionThread.join() shouldNotBeSameInstanceAs getMainRenderingThread()
      isOnRenderingThread.get() shouldBe false
    }

    test("should create non-global scope") {
      // Given:
      val scope = RenderingScope()
      val localJob =
        scope.launch {
          while (true) {
            delay(50)
          }
        }
      val globalJob =
        KtxAsync.launch {
          while (true) {
            delay(50)
          }
        }

      // When:
      scope.cancel()

      // Then:
      assert(localJob.isCancelled)
      assert(globalJob.isActive)
    }
  }
}
