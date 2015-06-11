package glaux.linalg

import Dimension._
import Tensor._
import glaux.statistics.{RealDistribution, Distribution}

trait Tensor extends TensorOperations {

  type Dimensionality <: Dimension
  def dimension: Dimensionality
  def sumAll: Double = seqView.sum
  def seqView: Seq[Double]
  def head: Double = seqView.head
  def toArray: Array[Double] = seqView.toArray
  def toRowVector: RowVector =
    if(isInstanceOf[RowVector]) this.asInstanceOf[RowVector] else (Row(dimension.totalSize), seqView)
}



trait TensorOperations {

  def +(that: Tensor): Tensor

  def -(that: Tensor): Tensor

  /** element-by-element multiplication */
  def *(that: Tensor): Tensor

  /** matrix multiplication */
  def **(that: Tensor): Tensor

  def /(that: Tensor): Tensor

  /** right division ... is this the correct symbol? */
  def \(that: Tensor): Tensor

  def +(that: Number): Tensor

  def -(that: Number): Tensor

  def *(that: Number): Tensor

  def /(that: Number): Tensor

  def \(that: Number): Tensor

  def T: Tensor

}

object Tensor {
  
  type CanBuildFrom[From, V <: Tensor] = From => V
  type TensorBuilder[V <: Tensor] = CanBuildFrom[(V#Dimensionality, Seq[Double]), V] //Seq instead of Iterable for performance concerns
  type GenVolBuilder[V <: Tensor] = CanBuildFrom[(Dimension, Seq[Double]), V]
  type RowBuilder = TensorBuilder[RowVector]
  type MatrixBuilder = TensorBuilder[Matrix]
  type Vol3DBuilder = TensorBuilder[Vol]


  implicit val toRow: CanBuildFrom[Tensor, RowVector] = v => v.asInstanceOf[RowVector]
  implicit val toMatrix: CanBuildFrom[Tensor, Matrix] = v => v.asInstanceOf[Matrix]

  implicit def toGen[V <: Tensor](implicit gb: GenVolBuilder[V]): TensorBuilder[V] = gb
  implicit class TensorOps[V <: Tensor : TensorBuilder](self: V) {

    def map(f: Double => Double): V = (self.dimension, self.seqView.map(f))

    def fill(value: Double): V = map(_ => 0)

    def merge(v2: V)(f: (Double, Double) => Double) : V = {
      assert(self.dimension == v2.dimension)
      (self.dimension, self.seqView.zip(v2.seqView).map(f.tupled))
    }

    def normalize: V = map(_ / self.sumAll)
  }

  def apply(dimension: Dimension, data: Seq[Double]) : Tensor = (dimension, data)

}

trait RowVector extends Tensor {
  type Dimensionality = Row
  def apply(index: Int) : Double
  def dot(that: RowVector): Double = {
    assert(dimension == that.dimension, "can only dot vector with the same dimension")
    (this ** that.T).head
  }
}

trait Matrix extends Tensor {
  type Dimensionality = TwoD
}

trait Vol extends Tensor {
  type Dimensionality = ThreeD
}


trait TensorFactory[V <: Tensor] {
  def apply(dimension: V#Dimensionality, data: Seq[Double])(implicit b: TensorBuilder[V]) : V = b((dimension, data))

  def fill(dimension: V#Dimensionality, value: Double)(implicit b: TensorBuilder[V]): V = apply(dimension, Array.fill(dimension.totalSize)(value))

  def sampleOf(dimension: V#Dimensionality, dist: RealDistribution, size: Int)(implicit b: TensorBuilder[V]): Iterable[V] =
    1.until(size).map(_ => sampleOf(dimension, dist))

  def sampleOf(dimension: V#Dimensionality, dist: RealDistribution)(implicit b: TensorBuilder[V]): V =
    apply(dimension, dist.sample(dimension.totalSize).toSeq)

}

object RowVector extends TensorFactory[RowVector]{
  def apply(values: Double*): RowVector = apply(Dimension.Row(values.length), values)
}

object Vol extends TensorFactory[Vol] {
  def apply(x: Int, y: Int, z: Int, data: Seq[Double]): Vol = apply(Dimension.ThreeD(x,y,z), data)
}

object Matrix extends TensorFactory[Matrix]{
  def apply(x: Int, y: Int, data: Seq[Double]): Matrix = apply(Dimension.TwoD(x,y), data)
}