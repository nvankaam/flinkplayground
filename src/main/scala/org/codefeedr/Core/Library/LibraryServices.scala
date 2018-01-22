/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.codefeedr.Core.Library

import com.typesafe.config.{Config, ConfigFactory}
import org.codefeedr.Core.Library.Internal.Zookeeper.ZkClient
import org.codefeedr.Core.Library.Metastore.SubjectLibrary

trait LibraryServices {
  @transient lazy val zkClient: ZkClient = LibraryServices.zkClient
  @transient lazy val subjectLibrary: SubjectLibrary = LibraryServices.subjectLibrary
  @transient lazy val conf: Config = ConfigFactory.load
}

object LibraryServices {
  @transient lazy val zkClient: ZkClient = new ZkClient()
  @transient lazy val subjectLibrary: SubjectLibrary = new SubjectLibrary(zkClient)
  @transient lazy val conf: Config = ConfigFactory.load
}
