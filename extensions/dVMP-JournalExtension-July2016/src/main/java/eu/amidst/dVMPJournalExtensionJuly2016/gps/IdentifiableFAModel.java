
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
package eu.amidst.dVMPJournalExtensionJuly2016.gps;

import eu.amidst.core.variables.Variable;
import eu.amidst.flinklink.core.learning.parametric.utils.IdenitifableModelling;

import java.io.Serializable;

/**
 * Created by ana@cs.aau.dk <mailto:ana@cs.aau.dk> on 15/02/16.
 */
public class IdentifiableFAModel implements IdenitifableModelling, Serializable {

    private int numLocalHiddenVariables;

    public IdentifiableFAModel(int numLocalHiddenVariables){
        this.numLocalHiddenVariables = numLocalHiddenVariables;
    }

    /**
     * Represents the serial version ID for serializing the object.
     */
    private static final long serialVersionUID =4107783324901370839L;

    @Override
    public int getNumberOfEpochs() {
        return numLocalHiddenVariables+2;
    }


    @Override
    public boolean isActiveAtEpoch(Variable variable,int epoch) {

        if (epoch==0)
            return true;

        if (variable.getName().startsWith("GPS")) {
            if (variable.getName().contains("_Gamma_"))
                return epoch % getNumberOfEpochs() == 0;
            else if (variable.getName().contains("Beta0"))
                return epoch % getNumberOfEpochs() == 1;
            else if (variable.getName().contains("Beta_LocalHidden")) {
                for (int i = 0; i < this.numLocalHiddenVariables; i++) {
                    if (variable.getName().contains("Beta_LocalHidden_" + i))
                        return epoch % getNumberOfEpochs() == i + 2;
                }
                return true;
            } else
                return true;
        }


        for (int k=0; k<this.numLocalHiddenVariables-1; k++) {

            if (variable.getName().startsWith("LocalHidden_"+k)) {
                int epochs = this.numLocalHiddenVariables - 1 - k + 2;
                if (variable.getName().contains("_Gamma_"))
                    return epoch % epochs == 0;
                else if (variable.getName().contains("Beta0"))
                    return epoch % epochs == 1;
                else if (variable.getName().contains("Beta_LocalHidden")) {
                    for (int i = k+1; i < this.numLocalHiddenVariables; i++) {
                        if (variable.getName().contains("Beta_LocalHidden_" + i))
                            return epoch % epochs == (i-k-1) + 2 ;
                    }
                    return true;
                } else
                    return true;
            }

        }

        return true;
    }

}

