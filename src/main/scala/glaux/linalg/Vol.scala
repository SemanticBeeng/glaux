package glaux.linalg

import glaux.linalg.Dimension.DimensionFactory
import glaux.linalg.Dimension._
import org.nd4j.api.linalg.DSL._
import org.nd4j.api.linalg.RichNDArray
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import Vol.CanBuildFrom

trait Vol {
  def indArray: INDArray
  type Dimensionality <: Dimension
  def dimension: Dimensionality
  def sumAll: Double

  def iterable: Iterable[Double]
  def toArray: Array[Double] = iterable.toArray
}

sealed abstract class ND4JBackedVol[D <: Dimension : DimensionFactory](indArray: INDArray) extends Vol {
  type Dimensionality = D
  val dimension: Dimensionality =
    implicitly[DimensionFactory[Dimensionality]].create(indArray.shape())
  def sumAll: Double = { indArray.linearView().sum(Row.dimIndexOfData).getDouble(0) }

  def iterable: Iterable[Double] = {
    val lv = indArray.linearView()
    val myLength = lv.size(Row.dimIndexOfData)

    new Iterable[Double] {
      def iterator = new Iterator[Double] {
        var index: Int = 0
        override def hasNext: Boolean = index < myLength
        override def next(): Double = {
          index += 1
          lv.getDouble(index - 1)
        }
      }
    }
  }

}

case class Vol3D(indArray: INDArray)      extends ND4JBackedVol[ThreeD](indArray)
case class Matrix(indArray: INDArray)     extends ND4JBackedVol[TwoD](indArray)
case class RowVector(indArray: INDArray)  extends ND4JBackedVol[Row](indArray) {
  def apply(index: Int) : Double = indArray.getDouble(index)
}


trait VolCompanion[V <: Vol] {
  implicit val cb : CanBuildFrom[V]

  private def createINDArray(dimension: Dimension, data: Seq[Double]): INDArray = {
    assert(dimension.totalSize == data.length, s"data length ${data.length} does not conform to $dimension" )
    Nd4j.create(data.toArray, dimension.shape)
  }

  def apply(dimension: Dimension, data: Seq[Double]): V = createINDArray(dimension, data)

  def uniform(dimension: Dimension, value: Double): V = Nd4j.create(dimension.shape:_*).assign(value)

  def normal(dimension: Dimension, mean: Double, std: Double): V =
    Nd4j.getDistributions.createNormal(mean, std).sample(dimension.shape)
}

object RowVector extends VolCompanion[RowVector]{
  implicit val cb : CanBuildFrom[RowVector] = RowVector.apply
  def apply(values: Double*): RowVector = RowVector(Dimension.Row(values.length), values)
}

object Vol3D extends VolCompanion[Vol3D] {
  implicit val cb : CanBuildFrom[Vol3D] = Vol3D.apply
  def apply(x: Int, y: Int, z: Int, data: Seq[Double]): Vol3D = apply(Dimension.ThreeD(x,y,z), data)
}

object Matrix extends VolCompanion[Matrix]{
  implicit val cb : CanBuildFrom[Matrix] = Matrix.apply
  def apply(x: Int, y: Int, data: Seq[Double]): Matrix = apply(Dimension.TwoD(x,y), data)
}


object Vol extends VolCompanion[Vol]{

  implicit val cb : CanBuildFrom[Vol] = indArray => Dimension.of(indArray) match {
    case d @ ThreeD(_,_,_) => Vol3D(indArray)
    case d @ TwoD(_,_) => Matrix(indArray)
    case d @ Row(_) => RowVector(indArray)
  }

  type CanBuildFrom[T <: Vol] = INDArray => T

  implicit def toINDArray(vol: Vol) : INDArray = vol.indArray
  implicit def toRichIndArray(vol: Vol) : RichNDArray = toRichNDArray(vol.indArray)


  implicit class VolOps[V <: Vol: CanBuildFrom](v: V) {

    def map(f: Double => Double): V = mapWithIndex((value, i) => f(value))

    def mapWithIndex(f: (Double, Int) => Double): V = {
      val dup = v.indArray.dup
      val linear = dup.linearView
      Range(0, linear.length).foreach { i =>
        linear.putScalar(i, f(linear.getDouble(i), i))
      }
      dup
    }

    def merge(v2: V)(f: (Double, Double) => Double) : V = {
      assert(v.dimension == v2.dimension)
      val linear2 = v2.indArray.linearView
      mapWithIndex((value, i) => f(value, linear2.getDouble(i) ))
    }

  }




}