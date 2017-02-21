import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.datastream.DataStream;
import eu.amidst.core.distribution.GaussianMixture;
import eu.amidst.core.distribution.Normal;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.utils.BayesianNetworkGenerator;
import eu.amidst.core.utils.BayesianNetworkSampler;
import eu.amidst.core.variables.Assignment;
import eu.amidst.core.variables.HashMapAssignment;
import eu.amidst.core.variables.Variable;
import eu.amidst.flinklink.core.inference.DistributedImportanceSamplingCLG;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by dario on 19/1/17.
 */
public class DistributedISPrecision {

    public static void main(String[] args) throws Exception {

        int seedBN;
        int nDiscreteVars;
        int nContVars;

        int seedIS;
        int minimumSampleSize;

        double evidenceVarsRatio;

        int nSamplesForLikelihood;

        if (args.length!=7) {

            seedBN = 326762;
            nDiscreteVars = 500;
            nContVars = 500;

            seedIS = 111235236;
            minimumSampleSize = 100;

            evidenceVarsRatio = 0.3;

            nSamplesForLikelihood = 1000000;

        }
        else {

            seedBN = Integer.parseInt(args[0]);
            nDiscreteVars = Integer.parseInt(args[1]);
            nContVars = Integer.parseInt(args[2]);

            seedIS = Integer.parseInt(args[3]);
            minimumSampleSize = Integer.parseInt(args[4]);

            evidenceVarsRatio = Double.parseDouble(args[5]);

            nSamplesForLikelihood = Integer.parseInt(args[6]);

        }

        final int numberOfRepetitions = 3;
        final int numberOfSampleSizes = 3;

        int[] sampleSizes = new int[numberOfSampleSizes];
        sampleSizes[0]=minimumSampleSize;
        for (int i = 1; i < numberOfSampleSizes; i++) {
            sampleSizes[i]=10*sampleSizes[i-1];
        }

        final double link2VarsRatio = 2;

        System.out.println("\n\n\n");

        System.out.println("DISTRIBUTED IMPORTANCE SAMPLING PRECISION EXPERIMENTS");
        System.out.println("Parameters:");
        System.out.println("Bayesian Network with  " + nDiscreteVars + " discrete vars and " + nContVars + " continuous vars");
        System.out.println("(BN generated with seed " + seedBN + " and number of links " + (int) (link2VarsRatio*(nDiscreteVars + nContVars)) + ")");
        System.out.println();
        System.out.println("Ratio of variables in the evidence: " + evidenceVarsRatio);
        System.out.println();
        System.out.println("Seed for ImportanceSampling: " + seedIS);
        System.out.println("Sample sizes for IS: " + Arrays.toString(sampleSizes));
        System.out.println();
        System.out.println("Number of samples for estimating likelihood and actual probability of interval: " + nSamplesForLikelihood);

        System.out.println("\n\n\n");


        double[][] likelihood_Gaussian = new double[numberOfSampleSizes][numberOfRepetitions];
        double[][] likelihood_GaussianMixture = new double[numberOfSampleSizes][numberOfRepetitions];

        double[][] probability_LargeSample = new double[numberOfSampleSizes][numberOfRepetitions];
        double[][] probability_Query = new double[numberOfSampleSizes][numberOfRepetitions];
        double[][] probability_Gaussian = new double[numberOfSampleSizes][numberOfRepetitions];
        double[][] probability_GaussianMixture = new double[numberOfSampleSizes][numberOfRepetitions];


        for (int rep = 0; rep < numberOfRepetitions; rep++) {

            /**********************************************
             *    INITIALIZATION
             *********************************************/

            /*
             *  RANDOM GENERATION OF A BAYESIAN NETWORK
             */
            BayesianNetworkGenerator.setSeed(seedBN);
            BayesianNetworkGenerator.setNumberOfMultinomialVars(nDiscreteVars, 2);
            BayesianNetworkGenerator.setNumberOfGaussianVars(nContVars);
            BayesianNetworkGenerator.setNumberOfLinks((int) (link2VarsRatio*(nDiscreteVars + nContVars)));
            BayesianNetwork bn = BayesianNetworkGenerator.generateBayesianNetwork();
            System.out.println(bn);


            /*
             *  RANDOM CHOICE OF A CONTINUOUS VARIABLE OF INTEREST
             */
            Random variablesChoiceRandom = new Random(seedBN + 1000);

            List<Variable> variableList = bn.getVariables().getListOfVariables();
            List<Variable> normalVariablesList = variableList.stream().filter(Variable::isNormal).collect(Collectors.toList());

            int indexVarOfInterest = variablesChoiceRandom.nextInt(normalVariablesList.size());
            Variable varOfInterest = normalVariablesList.get(indexVarOfInterest);

            System.out.println("VARIABLE OF INTEREST: " + varOfInterest.getName() + "\n");

            List<Variable> varsOfInterestList = new ArrayList<>();
            varsOfInterestList.add(varOfInterest);


            /*
             *  RANDOM GENERATION OF AN EVIDENCE (EXCLUDING VARS OF INTEREST)
             */
            int nVarsEvidence = (int) (evidenceVarsRatio * bn.getNumberOfVars());
            System.out.println("Number of variables in the evidence: " + nVarsEvidence);
            List<Variable> varsEvidence = new ArrayList<>();

            while (varsEvidence.size() < nVarsEvidence) {
                int varIndex = variablesChoiceRandom.nextInt(bn.getNumberOfVars());
                Variable variable = bn.getVariables().getVariableById(varIndex);
                if (!varsEvidence.contains(variable) && !variable.equals(varOfInterest)) {
                    varsEvidence.add(variable);
                }
            }

            varsEvidence.sort((variable1, variable2) -> (variable1.getVarID() > variable2.getVarID() ? 1 : -1));

//            System.out.println("\nVARIABLES IN THE EVIDENCE: ");
//            varsEvidence.forEach(variable -> System.out.println(variable.getName()));

            BayesianNetworkSampler bayesianNetworkSampler = new BayesianNetworkSampler(bn);
            bayesianNetworkSampler.setSeed(variablesChoiceRandom.nextInt());
            DataStream<DataInstance> fullSample = bayesianNetworkSampler.sampleToDataStream(1);

            HashMapAssignment evidence = new HashMapAssignment(nVarsEvidence);
            varsEvidence.forEach(variable -> evidence.setValue(variable, fullSample.stream().findFirst().get().getValue(variable)));

            System.out.println("EVIDENCE: ");
            System.out.println(evidence.outputString(varsEvidence));



             /*
             *  LARGE SAMPLE FOR ESTIMATING THE LIKELIHOOD OF EACH POSTERIOR
             */
            BayesianNetworkSampler bnSampler = new BayesianNetworkSampler(bn);
            bnSampler.setSeed(12552);
            Stream<Assignment> sample = bnSampler.sampleWithEvidence(nSamplesForLikelihood, evidence);
            double[] varOfInterestSample = sample.mapToDouble(assignment -> assignment.getValue(varOfInterest)).toArray();



            /**********************************************************************************
             *    EXPERIMENT 1: COMPARING LIKELIHOOD OF POSTERIOR DISTRIBUTIONS
             *********************************************************************************/

            for (int ss = 0; ss < numberOfSampleSizes; ss++) {
                int currentSampleSize = sampleSizes[ss];

                /*
                 *  OBTAINING POSTERIORS WITH DISTRIBUTED IMPORTANCE SAMPLING
                 */
                DistributedImportanceSamplingCLG distributedIS = new DistributedImportanceSamplingCLG();

                distributedIS.setSeed(seedIS+ss+rep);
                distributedIS.setModel(bn);
                distributedIS.setSampleSize(currentSampleSize);
                distributedIS.setVariablesOfInterest(varsOfInterestList);


                // OBTAIN THE POSTERIOR AS A SINGLE GAUSSIAN
                distributedIS.setGaussianMixturePosteriors(false);
                distributedIS.runInference();
                Normal varOfInterestGaussianDistribution = distributedIS.getPosterior(varOfInterest);


                // OBTAIN THE POSTERIOR AS A GAUSSIAN MIXTURE
                distributedIS.setGaussianMixturePosteriors(true);

                // AND ALSO QUERY THE PROBABILITY OF THE VARIABLE BEING IN A CERTAIN INTERVAL

                double a = -3; // Lower endpoint of the interval
                double b = +3; // Upper endpoint of the interval

                final double finalA = a;
                final double finalB = b;
                distributedIS.setQuery(varOfInterest, (Function<Double, Double> & Serializable) (v -> (finalA < v && v < finalB) ? 1.0 : 0.0));


                distributedIS.runInference();

                // GET THE POSTERIOR AS A GAUSSIANMIXTURE AND THE QUERY RESULT

                double probQuery = distributedIS.getQueryResult();
                GaussianMixture varOfInterestGaussianMixtureDistribution = distributedIS.getPosterior(varOfInterest);



                /**********************************************************************************
                 *    EXPERIMENT 1: COMPARING LIKELIHOOD OF POSTERIOR DISTRIBUTIONS
                 *********************************************************************************/

                /*
                 *  ESTIMATE LIKELIHOOD OF EACH POSTERIOR
                 */

                double averageLikelihoodGaussian = Arrays.stream(varOfInterestSample).map(sample1 -> Math.exp(varOfInterestGaussianDistribution.getLogProbability(sample1))).average().getAsDouble();
                double averageLikelihoodGaussianMixture = Arrays.stream(varOfInterestSample).map(sample1 -> Math.exp(varOfInterestGaussianMixtureDistribution.getLogProbability(sample1))).average().getAsDouble();

                //System.out.println("\n\nVar: " + varOfInterest.getName() + ", conditional=" + bn.getConditionalDistribution(varOfInterest));
                //System.out.println("Gaussian posterior=" + varOfInterestGaussianDistribution.toString());
                //System.out.println("Gaussian likelihood= " + averageLikelihoodGaussian);
                //System.out.println("GaussianMixture posterior=" + varOfInterestGaussianMixtureDistribution.toString());
                //System.out.println("GaussianMixture likelihood= " + averageLikelihoodGaussianMixture);


                System.out.println("\n\nVar: " + varOfInterest.getName() + ", conditional=" + bn.getConditionalDistribution(varOfInterest));

                System.out.println("Gaussian posterior=        " + varOfInterestGaussianDistribution.toString());
                System.out.println("GaussianMixture posterior= " + varOfInterestGaussianMixtureDistribution.toString());


                System.out.println("Gaussian likelihood=         " + averageLikelihoodGaussian);
                System.out.println("GaussianMixture likelihood=  " + averageLikelihoodGaussianMixture);





                /**********************************************************************************
                 *    EXPERIMENT 2: COMPARING PROBABILITIES OF QUERIES
                 *********************************************************************************/

                NormalDistribution auxNormal = new NormalDistributionImpl(varOfInterestGaussianDistribution.getMean(), varOfInterestGaussianDistribution.getSd());
                double probGaussian = auxNormal.cumulativeProbability(finalB) - auxNormal.cumulativeProbability(finalA);

                double[] posteriorGaussianMixtureParameters = varOfInterestGaussianMixtureDistribution.getParameters();
                double probGaussianMixture = 0;
                for (int i = 0; i < varOfInterestGaussianMixtureDistribution.getNumberOfComponents(); i++) {
                    NormalDistribution auxNormal1 = new NormalDistributionImpl(posteriorGaussianMixtureParameters[1 + i * 3], Math.sqrt(posteriorGaussianMixtureParameters[2 + i * 3]));
                    probGaussianMixture += posteriorGaussianMixtureParameters[0 + i * 3] * (auxNormal1.cumulativeProbability(finalB) - auxNormal1.cumulativeProbability(finalA));
                }

                System.out.println("Query: P(" + Double.toString(a) + " < " + varOfInterest.getName() + " < " + Double.toString(b) + ")");
                System.out.println("Probability estimate with query:            " + probQuery);

                System.out.println("Probability with posterior Gaussian:        " + probGaussian);
                System.out.println("Probability with posterior GaussianMixture: " + probGaussianMixture);

                double probLargeSample = Arrays.stream(varOfInterestSample).map(v -> (finalA < v && v < finalB) ? 1.0 : 0.0).sum() / varOfInterestSample.length;
                System.out.println("Probability estimate with large sample:     " + probLargeSample);


                /**********************************************************************************
                 *    STORE THE RESULTS
                 *********************************************************************************/

                likelihood_Gaussian[ss][rep] = averageLikelihoodGaussian;
                likelihood_GaussianMixture[ss][rep] = averageLikelihoodGaussianMixture;


                probability_Query[ss][rep] = probQuery;
                probability_Gaussian[ss][rep] = probGaussian;
                probability_GaussianMixture[ss][rep] = probGaussianMixture;
                probability_LargeSample[ss][rep] = probLargeSample;

//            distributedIS = new DistributedImportanceSamplingCLG();
//
//            distributedIS.setSeed(seedIS);
//            distributedIS.setModel(bn);
//            distributedIS.setSampleSize(currentSampleSize);
//            distributedIS.setVariablesOfInterest(varsOfInterestList);
//
//
//            distributedIS.setGaussianMixturePosteriors(true);
//            distributedIS.setQuery(varOfInterest, (Function<Double, Double> & Serializable) (v -> (finalA < v && v < finalB) ? 1.0 : 0.0));
//            double probQuery = distributedIS.getQueryResult();
//            distributedIS.runInference();
//
//
//            double probQuery = distributedIS.getQueryResult();

            }
        }

        /**********************************************************************************
         *    SUMMARIZE AND SHOW THE RESULTS
         *********************************************************************************/

        for (int ss = 0; ss < numberOfSampleSizes; ss++) {
            System.out.println("SAMPLE SIZE: " + sampleSizes[ss]);

            System.out.println("Likelihood Gaussians:        " + Arrays.toString(likelihood_Gaussian[ss]));
            System.out.println("Likelihood GaussianMixtures: " + Arrays.toString(likelihood_GaussianMixture[ss]));

            System.out.println("Prob Large sample:    " + Arrays.toString(probability_LargeSample[ss]));
            System.out.println("Prob Query:           " + Arrays.toString(probability_Query[ss]));
            System.out.println("Prob Gaussian:        " + Arrays.toString(probability_Gaussian[ss]));
            System.out.println("Prob GaussianMixture: " + Arrays.toString(probability_GaussianMixture[ss]));

        }


    }
}
