package moze_intel.projecte.emc.arithmetic;

import org.apache.commons.math3.fraction.BigFraction;

public class HiddenBigFractionArithmetic extends FullBigFractionArithmetic {

	@Override
	public BigFraction div(BigFraction a, long b) {
		BigFraction result = super.div(a, b);
		//if result >= 0 && result < 1
		if (isGreaterThanEqualZero(result) && isLessThanOne(result)) {
			return result;
		}
		//result >= 1
		//Drop the fractional part of this value
		return new BigFraction(result.getNumerator().divide(result.getDenominator()));
	}

	private boolean isLessThanOne(BigFraction result) {
		return result.getNumerator().signum() != 1 || result.getNumerator().compareTo(result.getDenominator()) < 0;
	}
}