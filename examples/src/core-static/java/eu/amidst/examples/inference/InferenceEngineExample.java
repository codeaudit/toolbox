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

package eu.amidst.examples.inference;


import eu.amidst.corestatic.inference.InferenceEngine;
import eu.amidst.corestatic.io.BayesianNetworkLoader;
import eu.amidst.corestatic.models.BayesianNetwork;
import eu.amidst.corestatic.variables.Assignment;
import eu.amidst.corestatic.variables.HashMapAssignment;
import eu.amidst.corestatic.variables.Variable;

/**
 * This example show how to perform inference in a Bayesian network model using the InferenceEngine class.
 * This class aims to be a much straightfoward to perform queries over a Bayesian network model.
 *
 * Created by andresmasegosa on 18/6/15.
 */
public class InferenceEngineExample {

    public static void main(String[] args) throws Exception {

        //We first load the WasteIncinerator bayesian network which has multinomial and Gaussian variables.
        BayesianNetwork bn = BayesianNetworkLoader.loadFromFile("./networks/WasteIncinerator.bn");

        //We recover the relevant variables for this example: Mout which is normally distributed, and W which is multinomial.
        Variable varMout = bn.getStaticVariables().getVariableByName("Mout");
        Variable varW = bn.getStaticVariables().getVariableByName("W");

        //Set the evidence.
        Assignment assignment = new HashMapAssignment(1);
        assignment.setValue(varW,0);

        //Then we query the posterior of
        System.out.println("P(Mout|W=0) = " + InferenceEngine.getPosterior(varMout, bn, assignment));

    }

}