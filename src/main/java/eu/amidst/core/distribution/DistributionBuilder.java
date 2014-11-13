package eu.amidst.core.distribution;

import eu.amidst.core.header.DistType;
import eu.amidst.core.header.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresmasegosa on 11/11/14.
 */
public class DistributionBuilder {


    public static ConditionalDistribution newDistribution(Variable mainVar, List<Variable> conditioningVars){


        switch (mainVar.getDistributionType()) {
            case MULTINOMIAL:
                return new Multinomial_MultinomialParents(mainVar, conditioningVars);
            case GAUSSIAN:
                boolean multinomialParents = false;
                boolean normalParents = false;
                /* The parents of a gaussian variable are either multinomial and/or normal */
                for (Variable v : conditioningVars) {
                    if (v.getDistributionType().compareTo(DistType.MULTINOMIAL) == 0) {
                        multinomialParents = true;
                    } else if (v.getDistributionType().compareTo(DistType.GAUSSIAN) == 0) {
                        normalParents = true;
                    } else {
                        throw new IllegalArgumentException("Error in variable DistributionBuilder. Unrecognized DistributionType. ");
                    }
                }
                if (normalParents && !multinomialParents){
                    return new Normal_NormalParents(mainVar, conditioningVars);
                }else if ((!normalParents & multinomialParents)|| (conditioningVars.size()==0)){
                    return new Normal_MultinomialParents(mainVar, conditioningVars);
                } else if (normalParents & multinomialParents) {
                    return new Normal_MultinomialNormalParents(mainVar, conditioningVars);
                } else {
                    throw new IllegalArgumentException("Error in variable DistributionBuilder. Unrecognized DistributionType. ");
                }
            default:
                throw new IllegalArgumentException("Error in variable DistributionBuilder. Unrecognized DistributionType. ");
        }
    }















    /*public static ConditionalDistribution newDistribution(Variable mainVar, List<Variable> conditioningVars){

        if (conditioningVars.size() == 0) {
            switch (mainVar.getDistributionType()) {
                case MULTINOMIAL:
                    return new Multinomial_MultinomialParents(mainVar, new ArrayList<>());
                case GAUSSIAN:
                    return new Normal_MultinomialParents(mainVar, new ArrayList<>());
                default:
                    throw new IllegalArgumentException("Error in variable DistributionBuilder. Unrecognized DistributionType. ");
            }
        } else {
            switch (mainVar.getDistributionType()) {
                case MULTINOMIAL:
                        *//* The parents of a multinomial variable should always be multinomial *//*
                    return new Multinomial_MultinomialParents(mainVar, conditioningVars);
                case GAUSSIAN:
                    boolean multinomialParents = false;
                    boolean normalParents = false;

                    *//* The parents of a gaussian variable are either multinomial and/or normal *//*
                    for (Variable v : conditioningVars) {
                        if (v.getDistributionType().compareTo(DistType.MULTINOMIAL) == 0) {
                            multinomialParents = true;
                        } else if (v.getDistributionType().compareTo(DistType.GAUSSIAN) == 0) {
                            normalParents = true;
                        } else {
                            throw new IllegalArgumentException("Error in variable DistributionBuilder. Unrecognized DistributionType. ");
                        }
                    }
                    if (normalParents && !multinomialParents){
                        return new Normal_NormalParents(mainVar, conditioningVars);
                    }else if (!normalParents & multinomialParents) {
                        return new Normal_MultinomialParents(mainVar, conditioningVars);
                    } else if (normalParents & multinomialParents) {
                        return new Normal_MultinomialNormalParents(mainVar, conditioningVars);
                    } else {
                        throw new IllegalArgumentException("Error in variable DistributionBuilder. Unrecognized DistributionType. ");
                    }

                default:
                    throw new IllegalArgumentException("Error in variable DistributionBuilder. Unrecognized DistributionType. ");
            }
        }
    }*/

}