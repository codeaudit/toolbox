/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package eu.amidst.cajamareval;

import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.models.BayesianNetwork;

/**
 * Created by andresmasegosa on 9/12/15.
 */
public class Main {


    public static void main(String[] args) throws Exception {

        BayesianNetwork bn = BayesianNetworkLoader.loadFromFile("./networks/pigs.bn");
        System.out.println(bn.getNumberOfVars());
        //System.out.println(BayesianNetworkLoader.loadFromFile("./datasets/cajamar/outputTANOperativos.txt_TAN_model.bn"));

    }


}