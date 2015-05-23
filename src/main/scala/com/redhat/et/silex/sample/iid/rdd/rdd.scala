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

package com.redhat.et.silex.sample.iid.rdd

import org.apache.spark.rdd.RDD
import com.redhat.et.silex.sample.iid.IIDFeatureSamplingMethods
import com.redhat.et.silex.feature.extractor.FeatureSeq

class IIDFeatureSamplingMethodsRDD(data: RDD[Seq[Double]]) extends IIDFeatureSamplingMethods {
  def iidFeatureSeqRDD(
      n: Int,
      iSS: Int = 1000,
      oSS: Int = 1000
      ): RDD[FeatureSeq] = {
    data.map(row => FeatureSeq(row)) // placeholder
  }
}
