package com.acuo.collateral.trace.socs;

import com.opengamma.strata.basics.schedule.Frequency;
import com.tracegroup.transformer.externalobjects.socs.StringBOT;
import com.tracegroup.transformer.mom.DataException;

public class FrequencySoc extends StringBOT<Frequency> {
    @Override
    public Object transformerFromExternalObject(Frequency frequency) throws DataException {
        if (Frequency.TERM.equals(frequency)) return "1T";
        String toParse = frequency.getPeriod().toString();
        String unprefixed = toParse.startsWith("P") ? toParse.substring(1) : toParse;
        return unprefixed;
    }

    /**
     * When looking at trade register files or trade repositories, it is not uncommon to notice a value of 1T in a
     * column describing the payment frequency. One could be forgiven for thinking this is simply bad data, as values
     * such as 3M, 6M, 1Y might seem more natural. However, 1T is a convention used in FpML, meaning there is a single
     * payment at the maturity of the swap (T refers to ‘term’); it is typically used on zero coupon swap legs, in which
     * case, one should not be too surprised to find payment frequency of 1T on the shorter dated OIS swaps.
     * @see <a href="https://www.clarusft.com/ois-swap-nuances/">https://www.clarusft.com/ois-swap-nuances/</a>
     */
    @Override
    public Frequency externalObjectFromTransformer(String name) throws DataException {
        if ("1T".equals(name)) return Frequency.TERM;
        return Frequency.parse(name);
    }

    @Override
    public Class<Frequency> getExternalObjectClass() {
        return Frequency.class;
    }
}