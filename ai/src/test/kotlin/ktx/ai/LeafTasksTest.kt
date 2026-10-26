package ktx.ai

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.Task
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class LeafTasksTest : FunSpec({
  test("failureLeaf function should add Failure to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.failureLeaf()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("failureLeaf function inside of behaviorTree function's init block should add Failure to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        failureLeaf()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("failureLeaf function should return index where Failure is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.failureLeaf()
    val index2 = receiverTask.failureLeaf()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("failureLeaf function's init block should configure Failure exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.failureLeaf {
      variable = 42
    }

    variable shouldBe 42
  }

  test("successLeaf function should add Success to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.successLeaf()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("successLeaf function inside of behaviorTree function's init block should add Success to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        successLeaf()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("successLeaf function should return index where Success is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.successLeaf()
    val index2 = receiverTask.successLeaf()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("successLeaf function's init block should configure Success exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.successLeaf {
      variable = 42
    }

    variable shouldBe 42
  }

  test("waitLeaf function with explicit seconds parameter should add Wait to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.waitLeaf(1f)

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("waitLeaf function with explicit seconds parameter inside of behaviorTree function's init block should add Wait to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        waitLeaf(1f)
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("waitLeaf function should add Wait to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    tree.waitLeaf()

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("waitLeaf function inside of behaviorTree function's init block should add Wait to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree<Cat> {
        initialChildCount = childCount
        waitLeaf()
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("waitLeaf function should return index where Wait is added") {
    val receiverTask = GdxAiSequence<Cat>()

    val index1 = receiverTask.waitLeaf()
    val index2 = receiverTask.waitLeaf()

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("waitLeaf function's init block should configure Wait exactly once") {
    val receiverTask = mock<Task<Cat>>()
    val variable: Int

    receiverTask.waitLeaf {
      variable = 42
    }

    variable shouldBe 42
  }
})
