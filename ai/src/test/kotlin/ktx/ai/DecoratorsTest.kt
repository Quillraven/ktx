package ktx.ai

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.Task
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class DecoratorsTest : FunSpec({
  test("alwaysFail function should add AlwaysFail to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.alwaysFail()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("alwaysFail function inside of behaviorTree function's init block should add AlwaysFail to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        alwaysFail()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("alwaysFail function should return index where AlwaysFail is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.alwaysFail()
    val index2 = receiverTask.alwaysFail()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("alwaysFail function's init block should configure AlwaysFail exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.alwaysFail {
      variable = 42
    }

    variable shouldBe 42
  }

  test("alwaysSucceed function should add AlwaysSucceed to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.alwaysSucceed()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("alwaysSucceed function inside of behaviorTree function's init block should add AlwaysSucceed to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        alwaysSucceed()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("alwaysSucceed function should return index where AlwaysSucceed is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.alwaysSucceed()
    val index2 = receiverTask.alwaysSucceed()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("alwaysSucceed function's init block should configure AlwaysSucceed exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.alwaysSucceed {
      variable = 42
    }

    variable shouldBe 42
  }

  test("include function should add Include to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.include()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("include function inside of behaviorTree function's init block should add Include to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        include()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("include function should return index where Include is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.include()
    val index2 = receiverTask.include()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("include function's init block should configure Include exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.include {
      variable = 42
    }

    variable shouldBe 42
  }

  test("invert function should add Invert to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.invert()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("invert function inside of behaviorTree function's init block should add Invert to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        invert()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("invert function should return index where Invert is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.invert()
    val index2 = receiverTask.invert()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("invert function's init block should configure Invert exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.invert {
      variable = 42
    }

    variable shouldBe 42
  }

  test("random function should add GdxAiRandom to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.random()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("random function inside of behaviorTree function's init block should add GdxAiRandom to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        random()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("random function should return index where GdxAiRandom is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.random()
    val index2 = receiverTask.random()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("random function's init block should configure GdxAiRandom exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.random {
      variable = 42
    }

    variable shouldBe 42
  }

  test("repeat function should add Repeat to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.repeat()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("repeat function inside of behaviorTree function's init block should add Repeat to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        repeat()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("repeat function should return index where Repeat is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.repeat()
    val index2 = receiverTask.repeat()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("repeat function's init block should configure Repeat exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.repeat {
      variable = 42
    }

    variable shouldBe 42
  }

  test("semaphoreGuard function should add SemaphoreGuard to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.semaphoreGuard()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("semaphoreGuard function inside of behaviorTree function's init block should add SemaphoreGuard to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        semaphoreGuard()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("semaphoreGuard function should return index where SemaphoreGuard is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.semaphoreGuard()
    val index2 = receiverTask.semaphoreGuard()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("semaphoreGuard function's init block should configure SemaphoreGuard exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.semaphoreGuard {
      variable = 42
    }

    variable shouldBe 42
  }

  test("untilFail function should add UntilFail to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.untilFail()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("untilFail function inside of behaviorTree function's init block should add UntilFail to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        untilFail()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("untilFail function should return index where UntilFail is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.untilFail()
    val index2 = receiverTask.untilFail()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("untilFail function's init block should configure UntilFail exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.untilFail {
      variable = 42
    }

    variable shouldBe 42
  }

  test("untilSuccess function should add UntilSuccess to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.untilSuccess()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("untilSuccess function inside of behaviorTree function's init block should add UntilSuccess to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        untilSuccess()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("untilSuccess function should return index where UntilSuccess is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.untilSuccess()
    val index2 = receiverTask.untilSuccess()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("untilSuccess function's init block should configure UntilSuccess exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.untilSuccess {
      variable = 42
    }

    variable shouldBe 42
  }
})
