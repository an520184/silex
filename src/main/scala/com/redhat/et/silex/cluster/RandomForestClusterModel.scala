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
 * limitations under the License.
 */

package com.redhat.et.silex.cluster

import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.linalg.{
  Vector => SparkVec,
  DenseVector => DenseSparkVec
}

import com.redhat.et.silex.util.vectors.implicits._
import ClusteringRandomForestModel._

class RandomForestClusterModel[T](
  val extractor: T => Seq[Double],
  val randomForestModel: RandomForestModel,
  val kMedoidsModel: KMedoidsModel[Vector[Int]]) extends Serializable {

  val predictor = (point: T) => {
    kMedoidsModel.predict(randomForestModel.predictLeafIds(extractor(point).toSpark))
  }
}

object RandomForestClusterModel {
}
