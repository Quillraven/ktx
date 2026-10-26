package ktx.ai

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.Task
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class BehaviorTreeTest : FunSpec({
  test("behaviorTree function without arguments should return BehaviorTree object") {
    val behaviorTree = behaviorTree<Cat>()

    behaviorTree.shouldNotBeNull()
  }

  test("behaviorTree function with non-null arguments should return BehaviorTree object with correct properties") {
    val rootTask = mock<Task<Cat>>()
    val blackboard = mock<Cat>()
    val tree = behaviorTree(rootTask, blackboard)

    tree.shouldNotBeNull()
    tree.getChild(0) shouldBe rootTask
    tree.`object` shouldBe blackboard
  }

  test("behaviorTree function init block should configure BehaviorTree exactly once") {
    val variable: Int

    behaviorTree<Cat> {
      variable = 42
    }

    variable shouldBe 42
  }

  test("add function should add task to BehaviorTree") {
    val tree = BehaviorTree<Cat>()
    val initialChildCount = tree.childCount

    val task = mock<Task<Cat>>()
    tree.add(task)

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("add function should return index where task is added") {
    val selector = GdxAiSelector<Cat>()
    val index1 = selector.add(mock<Task<Cat>>())
    val index2 = selector.add(mock<Task<Cat>>())

    index1 shouldBe 0
    index2 shouldBe 1
  }

  test("add function inside of behaviorTree function's init block should add task to BehaviorTree") {
    val initialChildCount: Int

    val tree =
      behaviorTree {
        initialChildCount = childCount
        add(mock<Task<Cat>>())
      }

    tree.childCount shouldBe (initialChildCount + 1)
  }

  test("add function's init block allows editing properties of Task subclass") {
    val receiverTask = mock<Task<Cat>>()

    val eatTask = EatTask(hunger = 1)
    val initialHunger = eatTask.hunger

    receiverTask.add(eatTask) {
      hunger -= 1
    }

    eatTask.hunger shouldBe (initialHunger - 1)
  }

  test("add function's init block should configure Task exactly once") {
    val variable: Int
    val receiverTask = mock<Task<Cat>>()
    val task = mock<Task<Cat>>()

    receiverTask.add(task) {
      variable = 42
    }

    variable shouldBe 42
  }
})
