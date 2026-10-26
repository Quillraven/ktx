package ktx.box2d

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests joint builder methods.
 */
class JointsTest : FunSpec({
  fun getBodies(): Pair<Body, Body> {
    val world = createWorld()
    val bodyA =
      world.body {
        position.set(-1f, 0f)
        box(1f, 1f)
      }
    val bodyB =
      world.body {
        position.set(1f, 0f)
        box(1f, 1f)
      }
    return bodyA to bodyB
  }

  test("should create Joint with a custom JointDef") {
    val (bodyA, bodyB) = getBodies()
    val jointDefinition = DistanceJointDef()
    val variable: Int

    val joint =
      bodyA.jointWith(bodyB, jointDefinition) {
        length = 2f
        this shouldBeSameInstanceAs jointDefinition
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    (joint is DistanceJoint) shouldBe true
    (joint as DistanceJoint).length shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create RevoluteJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.revoluteJointWith(bodyB) {
        motorSpeed = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.motorSpeed shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create PrismaticJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.prismaticJointWith(bodyB) {
        motorSpeed = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.motorSpeed shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create DistanceJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.distanceJointWith(bodyB) {
        length = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.length shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create PulleyJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.pulleyJointWith(bodyB) {
        ratio = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.ratio shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create MouseJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.mouseJointWith(bodyB) {
        dampingRatio = 0.2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    // Anchors are not checked, as initial joint's anchor positions do not match bodies' positions.
    joint.dampingRatio shouldBe 0.2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create GearJoint") {
    val (bodyA, bodyB) = getBodies()
    val jointA = bodyB.revoluteJointWith(bodyA)
    val jointB = bodyA.revoluteJointWith(bodyB)
    val variable: Int

    val joint =
      bodyA.gearJointWith(bodyB) {
        joint1 = jointA
        joint2 = jointB
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.joint1 shouldBeSameInstanceAs jointA
    joint.joint2 shouldBeSameInstanceAs jointB
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create WheelJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.wheelJointWith(bodyB) {
        motorSpeed = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.motorSpeed shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create WeldJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.weldJointWith(bodyB) {
        dampingRatio = 0.2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.dampingRatio shouldBe 0.2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create FrictionJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.frictionJointWith(bodyB) {
        maxForce = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.maxForce shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create RopeJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.ropeJointWith(bodyB) {
        maxLength = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.maxLength shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }

  test("should create MotorJoint") {
    val (bodyA, bodyB) = getBodies()
    val variable: Int

    val joint =
      bodyA.motorJointWith(bodyB) {
        maxForce = 2f
        variable = 42
      }

    joint.bodyA shouldBeSameInstanceAs bodyA
    joint.bodyB shouldBeSameInstanceAs bodyB
    joint.anchorA shouldBe bodyA.position
    joint.anchorB shouldBe bodyB.position
    joint.maxForce shouldBe 2f
    variable shouldBe 42
    bodyA.world.dispose()
  }
})
