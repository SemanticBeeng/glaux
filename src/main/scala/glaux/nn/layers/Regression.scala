package glaux.nn.layers

import glaux.nn.Dimension
import org.nd4j.api.linalg.DSL._
import glaux.nn._

case class Regression private(inDimension: Dimension.Row) extends LossLayer {
  def loss(target: Output, actual: Output): (Loss, InGradient) = {
    assert(target.dimension == outDimension && actual.dimension == outDimension)
    val gradient: Output = actual - target
    val losses: Output = (gradient * gradient) * 0.5
    (losses.sumAll, gradient)
  }

  val outDimension: OutDimension = inDimension

  def forward(input: Input, isTraining: Boolean = false): Output = input
}

object Regression {
  def apply(numOfOutputs: Int) : Regression = Regression(Dimension.Row(numOfOutputs))
}
