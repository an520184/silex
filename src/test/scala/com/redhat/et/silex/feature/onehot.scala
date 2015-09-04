/*
 * This file is part of the "silex" library of helpers for Apache Spark.
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.c
 */

package com.redhat.et.silex.feature.onehot

import org.scalatest._

import com.redhat.et.silex.testing.PerTestSparkContext
import com.redhat.et.silex.testing.matchers._

class OneHotModelSpec extends FlatSpec with Matchers {
  it should "provide oneHotExtractor" in {
    val hist = Seq(("a", 3.0), ("b", 2.0), ("c", 1.0))
    val model = new OneHotModel(hist)
    model.oneHotExtractor()("a") should beEqSeq(Seq(1.0, 0.0, 0.0))
    model.oneHotExtractor()("b") should beEqSeq(Seq(0.0, 1.0, 0.0))
    model.oneHotExtractor()("c") should beEqSeq(Seq(0.0, 0.0, 1.0))
    model.oneHotExtractor()("d") should beEqSeq(Seq(0.0, 0.0, 0.0))
    model.oneHotExtractor().width should be (3)
    model.oneHotExtractor().names.range.toSeq should beEqSeq(Seq("v=a","v=b","v=c"))
    model.oneHotExtractor().categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1, 2))
    model.oneHotExtractor().categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.oneHotExtractor(undefName = "*")("a") should beEqSeq(Seq(1.0, 0.0, 0.0, 0.0))
    model.oneHotExtractor(undefName = "*")("b") should beEqSeq(Seq(0.0, 1.0, 0.0, 0.0))
    model.oneHotExtractor(undefName = "*")("c") should beEqSeq(Seq(0.0, 0.0, 1.0, 0.0))
    model.oneHotExtractor(undefName = "*")("d") should beEqSeq(Seq(0.0, 0.0, 0.0, 1.0))
    model.oneHotExtractor(undefName = "*").width should be (4)
    model.oneHotExtractor(undefName = "*").names.range.toSeq should beEqSeq(
      Seq("v=a","v=b","v=c","v=*"))
    model.oneHotExtractor(undefName = "*").categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1, 2, 3))
    model.oneHotExtractor(undefName = "*").categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.oneHotExtractor(minFreq = 2)("a") should beEqSeq(Seq(1.0, 0.0))
    model.oneHotExtractor(minFreq = 2)("b") should beEqSeq(Seq(0.0, 1.0))
    model.oneHotExtractor(minFreq = 2)("c") should beEqSeq(Seq(0.0, 0.0))
    model.oneHotExtractor(minFreq = 2).width should be (2)
    model.oneHotExtractor(minFreq = 2).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.oneHotExtractor(minFreq = 2).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.oneHotExtractor(minFreq = 2).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.oneHotExtractor(maxFreq = 2)("a") should beEqSeq(Seq(0.0, 0.0))
    model.oneHotExtractor(maxFreq = 2)("b") should beEqSeq(Seq(1.0, 0.0))
    model.oneHotExtractor(maxFreq = 2)("c") should beEqSeq(Seq(0.0, 1.0))
    model.oneHotExtractor(maxFreq = 2).width should be (2)
    model.oneHotExtractor(maxFreq = 2).names.range.toSeq should beEqSeq(Seq("v=b","v=c"))
    model.oneHotExtractor(maxFreq = 2).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.oneHotExtractor(maxFreq = 2).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.oneHotExtractor(minProb = 0.33)("a") should beEqSeq(Seq(1.0, 0.0))
    model.oneHotExtractor(minProb = 0.33)("b") should beEqSeq(Seq(0.0, 1.0))
    model.oneHotExtractor(minProb = 0.33)("c") should beEqSeq(Seq(0.0, 0.0))
    model.oneHotExtractor(minProb = 0.33).width should be (2)
    model.oneHotExtractor(minProb = 0.33).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.oneHotExtractor(minProb = 0.33).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.oneHotExtractor(minProb = 0.33).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.oneHotExtractor(maxProb = 0.34)("a") should beEqSeq(Seq(0.0, 0.0))
    model.oneHotExtractor(maxProb = 0.34)("b") should beEqSeq(Seq(1.0, 0.0))
    model.oneHotExtractor(maxProb = 0.34)("c") should beEqSeq(Seq(0.0, 1.0))
    model.oneHotExtractor(maxProb = 0.34).width should be (2)
    model.oneHotExtractor(maxProb = 0.34).names.range.toSeq should beEqSeq(Seq("v=b","v=c"))
    model.oneHotExtractor(maxProb = 0.34).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.oneHotExtractor(maxProb = 0.34).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.oneHotExtractor(maxSize = 2)("a") should beEqSeq(Seq(1.0, 0.0))
    model.oneHotExtractor(maxSize = 2)("b") should beEqSeq(Seq(0.0, 1.0))
    model.oneHotExtractor(maxSize = 2)("c") should beEqSeq(Seq(0.0, 0.0))
    model.oneHotExtractor(maxSize = 2).width should be (2)
    model.oneHotExtractor(maxSize = 2).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.oneHotExtractor(maxSize = 2).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.oneHotExtractor(maxSize = 2).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.oneHotExtractor(minFreq = 4)("a") should beEqSeq(Seq.empty[Double])
    model.oneHotExtractor(maxFreq = 0)("a") should beEqSeq(Seq.empty[Double])
    model.oneHotExtractor(minProb = 1.0)("a") should beEqSeq(Seq.empty[Double])
    model.oneHotExtractor(maxProb = 0.0)("a") should beEqSeq(Seq.empty[Double])
    model.oneHotExtractor(maxSize = 0)("a") should beEqSeq(Seq.empty[Double])
    model.oneHotExtractor(maxSize = 0).width should be (0)
    model.oneHotExtractor(maxSize = 0).names.range.toSeq should beEqSeq(Seq[String]())
    model.oneHotExtractor(maxSize = 0).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.oneHotExtractor(maxSize = 0).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())
  }

  it should "provide multiHotExtractor" in {
    val hist = Seq(("a", 3.0), ("b", 2.0), ("c", 1.0))
    val model = new OneHotModel(hist)
    model.multiHotExtractor()(Seq("a")) should beEqSeq(Seq(1.0, 0.0, 0.0))
    model.multiHotExtractor()(Seq("b")) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model.multiHotExtractor()(Seq("c")) should beEqSeq(Seq(0.0, 0.0, 1.0))
    model.multiHotExtractor()(Seq("d")) should beEqSeq(Seq(0.0, 0.0, 0.0))
    model.multiHotExtractor()(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0, 1.0))
    model.multiHotExtractor()(Seq("b", "d")) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model.multiHotExtractor().width should be (3)
    model.multiHotExtractor().names.range.toSeq should beEqSeq(Seq("v=a","v=b","v=c"))
    model.multiHotExtractor().categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1, 2))
    model.multiHotExtractor().categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.multiHotExtractor(undefName = "*")(Seq("a")) should beEqSeq(Seq(1.0, 0.0, 0.0, 0.0))
    model.multiHotExtractor(undefName = "*")(Seq("b")) should beEqSeq(Seq(0.0, 1.0, 0.0, 0.0))
    model.multiHotExtractor(undefName = "*")(Seq("c")) should beEqSeq(Seq(0.0, 0.0, 1.0, 0.0))
    model.multiHotExtractor(undefName = "*")(Seq("d")) should beEqSeq(Seq(0.0, 0.0, 0.0, 1.0))
    model.multiHotExtractor(undefName = "*")(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0, 1.0, 0.0))
    model.multiHotExtractor(undefName = "*")(Seq("b", "d")) should beEqSeq(Seq(0.0, 1.0, 0.0, 1.0))
    model.multiHotExtractor(undefName = "*").width should be (4)
    model.multiHotExtractor(undefName = "*").names.range.toSeq should beEqSeq(
      Seq("v=a","v=b","v=c","v=*"))
    model.multiHotExtractor(undefName = "*").categoryInfo.domain.toSeq should beEqSeq(
      Seq(0, 1, 2, 3))
    model.multiHotExtractor(undefName = "*").categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.multiHotExtractor(minFreq = 2)(Seq("a")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(minFreq = 2)(Seq("b")) should beEqSeq(Seq(0.0, 1.0))
    model.multiHotExtractor(minFreq = 2)(Seq("c")) should beEqSeq(Seq(0.0, 0.0))
    model.multiHotExtractor(minFreq = 2)(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(minFreq = 2).width should be (2)
    model.multiHotExtractor(minFreq = 2).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.multiHotExtractor(minFreq = 2).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.multiHotExtractor(minFreq = 2).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.multiHotExtractor(maxFreq = 2)(Seq("a")) should beEqSeq(Seq(0.0, 0.0))
    model.multiHotExtractor(maxFreq = 2)(Seq("b")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(maxFreq = 2)(Seq("c")) should beEqSeq(Seq(0.0, 1.0))
    model.multiHotExtractor(maxFreq = 2)(Seq("a", "c")) should beEqSeq(Seq(0.0, 1.0))
    model.multiHotExtractor(maxFreq = 2).width should be (2)
    model.multiHotExtractor(maxFreq = 2).names.range.toSeq should beEqSeq(Seq("v=b","v=c"))
    model.multiHotExtractor(maxFreq = 2).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.multiHotExtractor(maxFreq = 2).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.multiHotExtractor(minProb = 0.33)(Seq("a")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(minProb = 0.33)(Seq("b")) should beEqSeq(Seq(0.0, 1.0))
    model.multiHotExtractor(minProb = 0.33)(Seq("c")) should beEqSeq(Seq(0.0, 0.0))
    model.multiHotExtractor(minProb = 0.33)(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(minProb = 0.33).width should be (2)
    model.multiHotExtractor(minProb = 0.33).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.multiHotExtractor(minProb = 0.33).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.multiHotExtractor(minProb = 0.33).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.multiHotExtractor(maxProb = 0.34)(Seq("a")) should beEqSeq(Seq(0.0, 0.0))
    model.multiHotExtractor(maxProb = 0.34)(Seq("b")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(maxProb = 0.34)(Seq("c")) should beEqSeq(Seq(0.0, 1.0))
    model.multiHotExtractor(maxProb = 0.34)(Seq("a", "c")) should beEqSeq(Seq(0.0, 1.0))
    model.multiHotExtractor(maxProb = 0.34).width should be (2)
    model.multiHotExtractor(maxProb = 0.34).names.range.toSeq should beEqSeq(Seq("v=b","v=c"))
    model.multiHotExtractor(maxProb = 0.34).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.multiHotExtractor(maxProb = 0.34).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.multiHotExtractor(maxSize = 2)(Seq("a")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(maxSize = 2)(Seq("b")) should beEqSeq(Seq(0.0, 1.0))
    model.multiHotExtractor(maxSize = 2)(Seq("c")) should beEqSeq(Seq(0.0, 0.0))
    model.multiHotExtractor(maxSize = 2)(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0))
    model.multiHotExtractor(maxSize = 2).width should be (2)
    model.multiHotExtractor(maxSize = 2).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.multiHotExtractor(maxSize = 2).categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1))
    model.multiHotExtractor(maxSize = 2).categoryInfo.range.toSeq should beEqSeq(Seq(2))

    model.multiHotExtractor(minFreq = 4)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.multiHotExtractor(maxFreq = 0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.multiHotExtractor(minProb = 1.0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.multiHotExtractor(maxProb = 0.0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.multiHotExtractor(maxSize = 0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.multiHotExtractor(maxSize = 0).width should be (0)
    model.multiHotExtractor(maxSize = 0).names.range.toSeq should beEqSeq(Seq[String]())
    model.multiHotExtractor(maxSize = 0).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.multiHotExtractor(maxSize = 0).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())
  }

  it should "provide histExtractor" in {
    val hist = Seq(("a", 3.0), ("b", 2.0), ("c", 1.0))
    val model = new OneHotModel(hist)
    model.histExtractor()(Seq("a")) should beEqSeq(Seq(1.0, 0.0, 0.0))
    model.histExtractor()(Seq("b")) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model.histExtractor()(Seq("c")) should beEqSeq(Seq(0.0, 0.0, 1.0))
    model.histExtractor()(Seq("d")) should beEqSeq(Seq(0.0, 0.0, 0.0))
    model.histExtractor()(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0, 1.0))
    model.histExtractor()(Seq("b", "d")) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model.histExtractor()(Seq("a", "a")) should beEqSeq(Seq(2.0, 0.0, 0.0))
    model.histExtractor()(Seq("a", "b", "a", "b", "a")) should beEqSeq(Seq(3.0, 2.0, 0.0))
    model.histExtractor().width should be (3)
    model.histExtractor().names.range.toSeq should beEqSeq(Seq("v=a","v=b","v=c"))
    model.histExtractor().categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor().categoryInfo.range.toSeq should beEqSeq(Seq[Int]())

    model.histExtractor(undefName = "*")(Seq("a")) should beEqSeq(Seq(1.0, 0.0, 0.0, 0.0))
    model.histExtractor(undefName = "*")(Seq("b")) should beEqSeq(Seq(0.0, 1.0, 0.0, 0.0))
    model.histExtractor(undefName = "*")(Seq("c")) should beEqSeq(Seq(0.0, 0.0, 1.0, 0.0))
    model.histExtractor(undefName = "*")(Seq("d")) should beEqSeq(Seq(0.0, 0.0, 0.0, 1.0))
    model.histExtractor(undefName = "*")(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0, 1.0, 0.0))
    model.histExtractor(undefName = "*")(Seq("a", "c", "a")) should beEqSeq(Seq(2.0, 0.0, 1.0, 0.0))
    model.histExtractor(undefName = "*")(Seq("b", "d", "b")) should beEqSeq(Seq(0.0, 2.0, 0.0, 1.0))
    model.histExtractor(undefName = "*")(Seq("d", "b", "e")) should beEqSeq(Seq(0.0, 1.0, 0.0, 2.0))
    model.histExtractor(undefName = "*").width should be (4)
    model.histExtractor(undefName = "*").names.range.toSeq should beEqSeq(
      Seq("v=a","v=b","v=c","v=*"))
    model.histExtractor(undefName = "*").categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor(undefName = "*").categoryInfo.range.toSeq should beEqSeq(Seq[Int]())

    model.histExtractor(minFreq = 2)(Seq("a")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(minFreq = 2)(Seq("b")) should beEqSeq(Seq(0.0, 1.0))
    model.histExtractor(minFreq = 2)(Seq("c")) should beEqSeq(Seq(0.0, 0.0))
    model.histExtractor(minFreq = 2)(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(minFreq = 2)(Seq("a", "c", "a", "b")) should beEqSeq(Seq(2.0, 1.0))
    model.histExtractor(minFreq = 2).width should be (2)
    model.histExtractor(minFreq = 2).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.histExtractor(minFreq = 2).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor(minFreq = 2).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())

    model.histExtractor(maxFreq = 2)(Seq("a")) should beEqSeq(Seq(0.0, 0.0))
    model.histExtractor(maxFreq = 2)(Seq("b")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(maxFreq = 2)(Seq("c")) should beEqSeq(Seq(0.0, 1.0))
    model.histExtractor(maxFreq = 2)(Seq("a", "c")) should beEqSeq(Seq(0.0, 1.0))
    model.histExtractor(maxFreq = 2)(Seq("a", "c", "b", "c")) should beEqSeq(Seq(1.0, 2.0))
    model.histExtractor(maxFreq = 2).width should be (2)
    model.histExtractor(maxFreq = 2).names.range.toSeq should beEqSeq(Seq("v=b","v=c"))
    model.histExtractor(maxFreq = 2).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor(maxFreq = 2).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())

    model.histExtractor(minProb = 0.33)(Seq("a")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(minProb = 0.33)(Seq("b")) should beEqSeq(Seq(0.0, 1.0))
    model.histExtractor(minProb = 0.33)(Seq("c")) should beEqSeq(Seq(0.0, 0.0))
    model.histExtractor(minProb = 0.33)(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(minProb = 0.33)(Seq("b", "c", "b", "a")) should beEqSeq(Seq(1.0, 2.0))
    model.histExtractor(minProb = 0.33).width should be (2)
    model.histExtractor(minProb = 0.33).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.histExtractor(minProb = 0.33).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor(minProb = 0.33).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())

    model.histExtractor(maxProb = 0.34)(Seq("a")) should beEqSeq(Seq(0.0, 0.0))
    model.histExtractor(maxProb = 0.34)(Seq("b")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(maxProb = 0.34)(Seq("c")) should beEqSeq(Seq(0.0, 1.0))
    model.histExtractor(maxProb = 0.34)(Seq("a", "c")) should beEqSeq(Seq(0.0, 1.0))
    model.histExtractor(maxProb = 0.34)(Seq("a", "c", "b", "c")) should beEqSeq(Seq(1.0, 2.0))
    model.histExtractor(maxProb = 0.34).width should be (2)
    model.histExtractor(maxProb = 0.34).names.range.toSeq should beEqSeq(Seq("v=b","v=c"))
    model.histExtractor(maxProb = 0.34).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor(maxProb = 0.34).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())

    model.histExtractor(maxSize = 2)(Seq("a")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(maxSize = 2)(Seq("b")) should beEqSeq(Seq(0.0, 1.0))
    model.histExtractor(maxSize = 2)(Seq("c")) should beEqSeq(Seq(0.0, 0.0))
    model.histExtractor(maxSize = 2)(Seq("a", "c")) should beEqSeq(Seq(1.0, 0.0))
    model.histExtractor(maxSize = 2)(Seq("a", "c", "a", "b")) should beEqSeq(Seq(2.0, 1.0))
    model.histExtractor(maxSize = 2).width should be (2)
    model.histExtractor(maxSize = 2).names.range.toSeq should beEqSeq(Seq("v=a","v=b"))
    model.histExtractor(maxSize = 2).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor(maxSize = 2).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())

    model.histExtractor(minFreq = 4)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.histExtractor(maxFreq = 0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.histExtractor(minProb = 1.0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.histExtractor(maxProb = 0.0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.histExtractor(maxSize = 0)(Seq("a")) should beEqSeq(Seq.empty[Double])
    model.histExtractor(maxSize = 0).width should be (0)
    model.histExtractor(maxSize = 0).names.range.toSeq should beEqSeq(Seq[String]())
    model.histExtractor(maxSize = 0).categoryInfo.domain.toSeq should beEqSeq(Seq[Int]())
    model.histExtractor(maxSize = 0).categoryInfo.range.toSeq should beEqSeq(Seq[Int]())
  }
}

class OneHotMethodsRDDSpec extends FlatSpec with Matchers  with PerTestSparkContext {
  import com.redhat.et.silex.feature.onehot.implicits._

  it should "provide oneHotBy" in {
    val data = context.parallelize(Seq(3, 2, 1, 2, 1, 1), 2)
    val model1 = data.oneHotBy(identity _)
    model1.oneHotExtractor()(1) should beEqSeq(Seq(1.0, 0.0, 0.0))
    model1.oneHotExtractor()(2) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model1.oneHotExtractor()(3) should beEqSeq(Seq(0.0, 0.0, 1.0))
    model1.oneHotExtractor()(4) should beEqSeq(Seq(0.0, 0.0, 0.0))
    model1.oneHotExtractor().width should be (3)
    model1.oneHotExtractor().names.range.toSeq should beEqSeq(Seq("v=1","v=2","v=3"))
    model1.oneHotExtractor().categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1, 2))
    model1.oneHotExtractor().categoryInfo.range.toSeq should beEqSeq(Seq(2))

    val model2 = data.oneHotBy(_ * 2)
    model2.oneHotExtractor()(2) should beEqSeq(Seq(1.0, 0.0, 0.0))
    model2.oneHotExtractor()(4) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model2.oneHotExtractor()(6) should beEqSeq(Seq(0.0, 0.0, 1.0))
    model2.oneHotExtractor()(8) should beEqSeq(Seq(0.0, 0.0, 0.0))
    model2.oneHotExtractor().width should be (3)
    model2.oneHotExtractor().names.range.toSeq should beEqSeq(Seq("v=2","v=4","v=6"))
    model2.oneHotExtractor().categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1, 2))
    model2.oneHotExtractor().categoryInfo.range.toSeq should beEqSeq(Seq(2))
  }

  it should "provide oneHotByFlat" in {
    val data = context.parallelize(Seq(3, 2, 1, 2, 1, 1), 2)
    val model1 = data.oneHotByFlat(x => Seq(x))
    model1.oneHotExtractor()(1) should beEqSeq(Seq(1.0, 0.0, 0.0))
    model1.oneHotExtractor()(2) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model1.oneHotExtractor()(3) should beEqSeq(Seq(0.0, 0.0, 1.0))
    model1.oneHotExtractor()(4) should beEqSeq(Seq(0.0, 0.0, 0.0))
    model1.oneHotExtractor().width should be (3)
    model1.oneHotExtractor().names.range.toSeq should beEqSeq(Seq("v=1","v=2","v=3"))
    model1.oneHotExtractor().categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1, 2))
    model1.oneHotExtractor().categoryInfo.range.toSeq should beEqSeq(Seq(2))

    val model2 = data.oneHotByFlat(x => Seq(x * 2))
    model2.oneHotExtractor()(2) should beEqSeq(Seq(1.0, 0.0, 0.0))
    model2.oneHotExtractor()(4) should beEqSeq(Seq(0.0, 1.0, 0.0))
    model2.oneHotExtractor()(6) should beEqSeq(Seq(0.0, 0.0, 1.0))
    model2.oneHotExtractor()(8) should beEqSeq(Seq(0.0, 0.0, 0.0))
    model2.oneHotExtractor().width should be (3)
    model2.oneHotExtractor().names.range.toSeq should beEqSeq(Seq("v=2","v=4","v=6"))
    model2.oneHotExtractor().categoryInfo.domain.toSeq should beEqSeq(Seq(0, 1, 2))
    model2.oneHotExtractor().categoryInfo.range.toSeq should beEqSeq(Seq(2))
  }
}
