package com.acuo.collateral.transform.services;

import com.acuo.collateral.transform.TransformerContext;
import com.acuo.collateral.transform.trace.services.FromCmeFileOutputWrapper;
import com.acuo.collateral.transform.trace.services.ToCmeFileOutputWrapper;
import com.acuo.collateral.transform.trace.services.Trace;
import com.acuo.collateral.transform.trace.utils.TraceUtils;
import com.acuo.common.util.ArgChecker;
import com.google.common.collect.ImmutableList;
import com.tracegroup.transformer.exposedservices.MomException;
import com.tracegroup.transformer.exposedservices.RuleException;
import com.tracegroup.transformer.exposedservices.StructureException;
import com.tracegroup.transformer.exposedservices.UnrecognizedMessageException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ClarusTransformer<T> extends BaseTransformer<T> {

    public ClarusTransformer(Trace mapper) {
        super(mapper);
    }

    @Override
    public String serialise(T value, TransformerContext context) {
        return serialise(ImmutableList.of(value), context);
    }

    @Override
    public String serialise(List<T> value, TransformerContext context) {
        try {
            ToCmeFileOutputWrapper outputWrapper = getMapper().toCmeFile(value.toArray(), context);
            return outputWrapper.getOutput();
        } catch (MomException | RuleException | UnrecognizedMessageException | StructureException e) {
            String msg = String.format("error occurred while mapping the data {} to a list of swaps", value);
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public T deserialise(String value) {
        return null;
    }

    @Override
    public List<T> deserialiseToList(String values) {
        ArgChecker.notNull(values, "values");
        values = TraceUtils.replaceNewLineToWindows(values);
        try {
            FromCmeFileOutputWrapper output = getMapper().fromCmeFile(values);
            return Arrays.stream(output.getSwap()).map(value -> (T)value).collect(Collectors.toList());
        } catch (MomException | RuleException | UnrecognizedMessageException | StructureException e) {
            String msg = String.format("error occurred while mapping the data {} to a list of swaps", values);
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
}
