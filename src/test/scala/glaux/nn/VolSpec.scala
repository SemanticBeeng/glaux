package glaux.nn

import glaux.nn.Dimension.{ThreeD}
import org.specs2.mutable.Specification

class VolSpec
  extends Specification {

  "3D Vols" >> {
    val dim = ThreeD(1,1,3)

    "* op keeps dimension" >> {
      val vol = Vol(dim, Seq(2, 3, 4))
      val vol2: Vol = vol * vol
      vol2 must_== Vol(dim, Seq(4, 9, 16))
    }

    "* scalar keeps dimension" >> {
      val vol = Vol(dim, Seq(2, 3, 4))
      val vol2: Vol = vol * 0.5
      vol2 must_== Vol(dim, Seq(1, 1.5, 2))
    }

    "sum correctly" >> {
      val m = Matrix(2, 2, Seq(1,2,3,4))
      m.sumAll must_== 10
    }

    "map" >> {
      "with immutability" >> {
        val m = Matrix(2, 2, Seq(1,2,3,4))
        Matrix.ops.map(m, _ * 2 )
        m must_== Matrix(2, 2, Seq(1,2,3,4))
      }

      "correctly" >> {
        val m = Matrix(2, 2, Seq(1,2,3,4))
        val result = Matrix.ops.map(m, _ * 2 )
        result must_== Matrix(2, 2, Seq(2,4,6,8))
      }
    }

  }

  "RowVector Vols" >> {
    "sum works correctly" >> {
      RowVector(1, 3, 4, 5).sumAll === 13
    }
    "* works correctly" >> {
      val result: Vol = RowVector(1, 3, 4, 5) * 3
      result must_==(RowVector(3, 9, 12, 15))
    }


  }


}
