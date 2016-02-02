/*
 * This file is part of the "silex" library of helpers for Apache Spark.
 *
 * Copyright (c) 2016 Red Hat, Inc.
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

package com.redhat.et.silex.sample.split

import scala.reflect.ClassTag

import org.apache.spark.storage.StorageLevel

import org.apache.spark.rdd.RDD
import org.apache.spark.Logging

class SplitSampleRDDFunctions[T :ClassTag](self: RDD[T]) extends Logging with Serializable {
  import com.redhat.et.silex.rdd.multiplex.implicits._

  import SplitSampleRDDFunctions.{defaultSL, find}

  def splitSample(n: Int,
    persist: StorageLevel = defaultSL,
    seed: Long = scala.util.Random.nextLong): Seq[RDD[T]] =
    self.flatMuxPartitions(n, (id: Int, data: Iterator[T]) => {
      scala.util.Random.setSeed(id.toLong * seed)
      val samples = Vector.fill(n) { scala.collection.mutable.ArrayBuffer.empty[T] }
      data.foreach { e => samples(scala.util.Random.nextInt(n)) += e }
      samples
    }, persist)

  def weightedSplitSample(weights: Seq[Double],
    persist: StorageLevel = defaultSL,
    seed: Long = scala.util.Random.nextLong): Seq[RDD[T]] = {
    require(weights.length > 0, "weights must be non-empty")
    require(weights.forall(_ > 0.0), "weights must be > 0")
    val n = weights.length
    val z = weights.sum
    val w = weights.scan(0.0)(_ + _).map(_ / z).toVector
    self.flatMuxPartitions(n, (id: Int, data: Iterator[T]) => {
      scala.util.Random.setSeed(id.toLong * seed)
      val samples = Vector.fill(n) { scala.collection.mutable.ArrayBuffer.empty[T] }
      data.foreach { e =>
        val x = scala.util.Random.nextDouble
        val j = find(x, w)
        samples(j) += e
      }
      samples
    }, persist)
  }

  def sampleNoMux(n: Int, seed: Long = 42L): Seq[RDD[T]] = {
    Vector.tabulate(n) { j =>
      self.mapPartitions { data =>
        scala.util.Random.setSeed(seed)
        data.filter { unused => scala.util.Random.nextInt(n) == j }
      }
    }
  }
}

object SplitSampleRDDFunctions {
  private val defaultSL = StorageLevel.MEMORY_ONLY

  private def find(x: Double, w: Seq[Double]) = {
    var (l, u) = (0, w.length - 1)
    if (x >= 1.0) u - 1
    else {
      var m = (l + u) / 2
      while (m > l) {
        if (x < w(m)) u = m
        else if (x >= w(m + 1)) l = m + 1
        else { l = m; u = m + 1 }
        m = (l + u) / 2
      }
      m
    }
  }
}

object implicits {
  import scala.language.implicitConversions
  implicit def splitSampleRDDFunctions[T :ClassTag](rdd: RDD[T]): SplitSampleRDDFunctions[T] =
    new SplitSampleRDDFunctions(rdd)

  def benchmark[T](label: String)(blk: => T) = {
    val t0 = System.nanoTime
    val t = blk
    val sec = (System.nanoTime - t0) / 1e9
    println(f"Run time for $label = $sec%.1f"); System.out.flush
    t
  }
}
