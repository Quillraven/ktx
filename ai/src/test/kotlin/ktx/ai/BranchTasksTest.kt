package ktx.ai

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.Task
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class BranchTasksTest : FunSpec({
  test("dynamicGuardSelector function should add DynamicGuardSelector to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.dynamicGuardSelector()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("dynamicGuardSelector function inside of behaviorTree function's init block should add DynamicGuardSelector to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        dynamicGuardSelector()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("dynamicGuardSelector function should return index where DynamicGuardSelector is added") {
    val receiverTask = GdxAiSelector<Cat>()
    val index1 = receiverTask.dynamicGuardSelector()
    val index2 = receiverTask.dynamicGuardSelector()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("dynamicGuardSelector function's init block should configure DynamicGuardSelector exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.dynamicGuardSelector {
      variable = 42
    }

    variable shouldBe 42
  }

  test("parallel function should add Parallel to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.parallel()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("parallel function inside of behaviorTree function's init block should add Parallel to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        parallel()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("parallel function should return index where Parallel is added") {
    val receiverTask = GdxAiSelector<Cat>()
    val index1 = receiverTask.parallel()
    val index2 = receiverTask.parallel()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("parallel function's init block should configure Parallel exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.parallel {
      variable = 42
    }

    variable shouldBe 42
  }

  test("randomSelector function should add RandomSelector to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.randomSelector()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("randomSelector function inside of behaviorTree function's init block should add RandomSelector to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        randomSelector()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("randomSelector function should return index where RandomSelector is added") {
    val receiverTask = GdxAiSelector<Cat>()
    val index1 = receiverTask.randomSelector()
    val index2 = receiverTask.randomSelector()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("randomSelector function's init block should configure RandomSelector exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.randomSelector {
      variable = 42
    }

    variable shouldBe 42
  }

  test("randomSequence function should add RandomSequence to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.randomSequence()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("randomSequence inside of behaviorTree function's init block should add RandomSequence to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        randomSequence()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("randomSequence function should return index where RandomSequence is added") {
    val receiverTask = GdxAiSelector<Cat>()
    val index1 = receiverTask.randomSequence()
    val index2 = receiverTask.randomSequence()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("randomSequence function's init block should configure RandomSequence exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.randomSequence {
      variable = 42
    }

    variable shouldBe 42
  }

  test("selector function should add GdxSelector to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.selector()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("selector inside of behaviorTree function's init block adds GdxSelector to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        selector()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("selector function should return index where GdxSelector is added") {
    val receiverTask = GdxAiSequence<Cat>()
    val index1 = receiverTask.selector()
    val index2 = receiverTask.selector()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("selector function's init block should configure GdxSelector exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.selector {
      variable = 42
    }

    variable shouldBe 42
  }

  test("sequence function should add GdxSequence to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.sequence()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("sequence inside of behaviorTree function's init block should add GdxSequence to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        sequence()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("sequence function should return index where GdxSequence is added") {
    val receiverTask = GdxAiSelector<Cat>()
    val index1 = receiverTask.sequence()
    val index2 = receiverTask.sequence()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("sequence function's init block should configure GdxSequence exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.sequence {
      variable = 42
    }

    variable shouldBe 42
  }
})
