package ktx.actors

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests [Action] utilities.
 */
class ActionsTest : FunSpec({
  test("should attach global actions to stage with +=") {
    val stage = getMockStage()
    val action = MockAction()

    stage += action

    (action in stage.root.actions) shouldBe true
    action.actor shouldBeSameInstanceAs stage.root
  }

  test("should remove global actions from stage with -=") {
    val stage = getMockStage()
    val action = MockAction()
    stage.addAction(action)

    stage -= action

    (action in stage.root.actions) shouldBe false
    action.actor shouldNotBe stage.root
  }

  test("should attach actions to actors with +=") {
    val actor = Actor()
    val action = MockAction()

    actor += action

    (action in actor.actions) shouldBe true
    action.actor shouldBe actor
  }

  test("should remove actions from actors with -=") {
    val actor = Actor()
    val action = MockAction()
    actor.addAction(action)

    actor -= action

    (action in actor.actions) shouldBe false
    action.actor shouldNotBe actor
  }

  test("should chain actions into sequence with action then action") {
    val firstAction = MockAction()
    val secondAction = MockAction()

    val sequence = firstAction then secondAction

    sequence.actions[0] shouldBe firstAction
    sequence.actions[1] shouldBe secondAction
    sequence.actions.size shouldBe 2
  }

  test("should chain underling actions into sequence with sequence then action") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()

    val sequence = Actions.sequence(firstAction, secondAction) then thirdAction

    sequence.actions[0] shouldBe firstAction
    sequence.actions[1] shouldBe secondAction
    sequence.actions[2] shouldBe thirdAction
    sequence.actions.size shouldBe 3
  }

  test("should use existing sequence with then") {
    val firstAction = SequenceAction(MockAction(), MockAction())
    val secondAction = MockAction()

    val sequence = firstAction then secondAction

    sequence.actions.size shouldBe 3
    sequence shouldBeSameInstanceAs firstAction
  }

  test("should not mutate multiple actions with +") {
    // Given:
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()
    val sequence = firstAction + secondAction

    // When:
    sequence + thirdAction

    // Then: should not mutate firstSequence.
    sequence.actions.size shouldBe 2
    sequence.actions[0] shouldBe firstAction
    sequence.actions[1] shouldBe secondAction
  }

  test("should add action to sequence with +=") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()
    val sequence = Actions.sequence(firstAction, secondAction)

    sequence += thirdAction

    sequence.actions[0] shouldBe firstAction
    sequence.actions[1] shouldBe secondAction
    sequence.actions[2] shouldBe thirdAction
    sequence.actions.size shouldBe 3
  }

  test("should create parallel action given two actions") {
    val firstAction = MockAction()
    val secondAction = MockAction()

    val parallel = firstAction along secondAction // === firstAction parallelTo secondAction

    (firstAction in parallel.actions) shouldBe true
    (secondAction in parallel.actions) shouldBe true
    parallel.actions.size shouldBe 2
  }

  test("should create parallel action given a parallel action and regular action") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()

    val parallel = Actions.parallel(firstAction, secondAction) along thirdAction

    (firstAction in parallel.actions) shouldBe true
    (secondAction in parallel.actions) shouldBe true
    (thirdAction in parallel.actions) shouldBe true
    parallel.actions.size shouldBe 3
  }

  test("should wrap a regular action and parallel action with action along") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()

    val parallel = firstAction along Actions.parallel(secondAction, thirdAction)

    (firstAction in parallel.actions) shouldBe true
    parallel.actions.size shouldBe 2
    (parallel.actions[1] as ParallelAction).actions[0] shouldBeSameInstanceAs secondAction
    (parallel.actions[1] as ParallelAction).actions[1] shouldBeSameInstanceAs thirdAction
  }

  test("should chain parallel actions") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()

    val parallel = firstAction along secondAction along thirdAction

    (firstAction in parallel.actions) shouldBe true
    (secondAction in parallel.actions) shouldBe true
    (thirdAction in parallel.actions) shouldBe true
    parallel.actions.size shouldBe 3
  }

  test("should not mutate parallel actions with +") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()
    val parallel = Actions.parallel(firstAction, secondAction)

    parallel + thirdAction

    (firstAction in parallel.actions) shouldBe true
    (secondAction in parallel.actions) shouldBe true
    (thirdAction in parallel.actions) shouldBe false
    parallel.actions.size shouldBe 2
  }

  test("should not mutate multiple actions with div") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()
    val sequence = Actions.sequence(firstAction, secondAction)

    sequence / thirdAction

    (firstAction in sequence.actions) shouldBe true
    (secondAction in sequence.actions) shouldBe true
    (thirdAction in sequence.actions) shouldBe false
    sequence.actions.size shouldBe 2
  }

  test("should use existing parallel with along") {
    val firstAction = ParallelAction(MockAction(), MockAction())
    val secondAction = MockAction()

    val parallel = firstAction along secondAction

    parallel.actions.size shouldBe 3
    parallel shouldBeSameInstanceAs firstAction
  }

  test("should add action to parallel action group with +=") {
    val firstAction = MockAction()
    val secondAction = MockAction()
    val thirdAction = MockAction()
    val parallel = Actions.parallel(firstAction, secondAction)

    parallel += thirdAction

    (firstAction in parallel.actions) shouldBe true
    (secondAction in parallel.actions) shouldBe true
    (thirdAction in parallel.actions) shouldBe true
    parallel.actions.size shouldBe 3
  }

  test("should not unwrap sequences with along") {
    val firstSequence = SequenceAction(MockAction(), MockAction())
    val secondSequence = SequenceAction(MockAction(), MockAction())

    val parallel = firstSequence along secondSequence

    (firstSequence in parallel.actions) shouldBe true
    (secondSequence in parallel.actions) shouldBe true
    parallel.actions.size shouldBe 2
  }

  test("should make sequence actions parallel with div") {
    val firstSequence = SequenceAction(MockAction(), MockAction())
    val secondSequence = SequenceAction(MockAction(), MockAction())

    val parallel = firstSequence / secondSequence

    (firstSequence in parallel.actions) shouldBe true
    (secondSequence in parallel.actions) shouldBe true
    parallel.actions.size shouldBe 2
  }

  test("should create actions repeating forever") {
    val action = MockAction().repeatForever()

    action.count shouldBe RepeatAction.FOREVER
  }

  test("should create actions repeating given amount of times") {
    val action = MockAction().repeat(10)

    action.count shouldBe 10
  }

  test("should prohibit from using negative repetitions count") {
    val action = MockAction().repeat(-10)

    // To avoid unexpected unlimited repetitions count, to switch negative numbers to 0.
    action.count shouldBe 0
  }
})

/** Action testing utility. */
class MockAction : Action() {
  private var acted = false

  override fun act(delta: Float): Boolean {
    acted = true
    return true
  }
}
