package eu.amidst.core.exponentialfamily;

import eu.amidst.core.header.Variable;

/**
 * Created by andresmasegosa on 13/11/14.
 */
public class EF_Normal extends EF_UnivariateDistribution {

    public static int EXPECTED_MEAN = 0;
    public static int EXPECTED_SQUARE = 1;

    public EF_Normal(Variable var_) {
        this.var=var_;
        this.naturalParameters = new NaturalParameters(2);
        this.momentParameters = new MomentParameters(2);

        this.momentParameters.set(EXPECTED_MEAN,0);
        this.momentParameters.set(EXPECTED_SQUARE,1);
        this.setMomentParameters(momentParameters);
    }

    @Override
    public double computeLogBaseMeasure(double val) {
        return -0.5*Math.log(2*Math.PI);
    }

    @Override
    public double computeLogNormalizer(NaturalParameters parameters) {
        double m_0=this.momentParameters.get(EXPECTED_MEAN);
        double m_1=this.momentParameters.get(EXPECTED_SQUARE);
        return m_0*m_0/(2*(m_1-m_0*m_0)) + 0.5*Math.log(m_1-m_0*m_0);
    }

    @Override
    public SufficientStatistics getSufficientStatistics(double val) {
        SufficientStatistics vec = new SufficientStatistics(2);
        vec.set(EXPECTED_MEAN,val);
        vec.set(EXPECTED_SQUARE,val*val);
        return vec;
    }

    @Override
    public NaturalParameters getNaturalFromMomentParameters(MomentParameters momentParameters) {
        double m_0=this.momentParameters.get(EXPECTED_MEAN);
        double m_1=this.momentParameters.get(EXPECTED_SQUARE);
        this.naturalParameters.set(0,m_0/(m_0-m_1*m_1));
        this.naturalParameters.set(1,-0.5/(m_0-m_1*m_1));
        return naturalParameters;
    }

    @Override
    public MomentParameters getMomentFromNaturalParameters(NaturalParameters naturalParameters) {
        double n_0 = this.naturalParameters.get(0);
        double n_1 = this.naturalParameters.get(1);
        this.momentParameters.set(EXPECTED_MEAN,-0.5*n_0/n_1);
        this.momentParameters.set(EXPECTED_SQUARE,-0.5*n_0/n_1 + 0.25*Math.pow(n_0/n_1,2));
        return momentParameters;
    }
}