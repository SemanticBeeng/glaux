package glaux.nn.layers

import glaux.nn.Dimension
import glaux.nn._
import glaux.nn.layers.FullyConnected.{Bias, Filter}

case class FullyConnected(filter: Filter, bias: Bias) extends HiddenLayer {
  val inDimension: InDimension = Dimension.Row(filter.dimension.x)
  val outDimension: OutDimension = Dimension.Row(filter.dimension.y)

  assert(bias.dimension == outDimension)

  type Output = RowVector
  type Input = RowVector

  def backward(input: Input, outGradient: OutGradient): (InGradient, Seq[ParamGradient]) = ???

  def updateParams(params: Iterable[LayerParam]): HiddenLayer = ???


  def forward(input: Input, isTraining: Boolean = false): Output = {
    val filtered: RowVector = input ** filter
    filtered.add(bias)
  }

}

object FullyConnected {
  type Filter = Matrix
  type Bias = RowVector

}
