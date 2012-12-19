package org.broadinstitute.sting.gatk.walkers.annotator;

import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.walkers.annotator.interfaces.AnnotatorCompatible;
import org.broadinstitute.sting.gatk.walkers.annotator.interfaces.InfoFieldAnnotation;
import org.broadinstitute.sting.utils.genotyper.PerReadAlleleLikelihoodMap;
import org.broadinstitute.variant.utils.BaseUtils;
import org.broadinstitute.variant.vcf.VCFHeaderLineType;
import org.broadinstitute.variant.vcf.VCFInfoHeaderLine;
import org.broadinstitute.sting.utils.pileup.PileupElement;
import org.broadinstitute.variant.variantcontext.VariantContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The number of N bases, counting only SOLiD data
 */
public class NBaseCount extends InfoFieldAnnotation {
    public Map<String, Object> annotate(final RefMetaDataTracker tracker,
                                        final AnnotatorCompatible walker,
                                        final ReferenceContext ref,
                                        final Map<String, AlignmentContext> stratifiedContexts,
                                        final VariantContext vc,
                                        final Map<String, PerReadAlleleLikelihoodMap> stratifiedPerReadAlleleLikelihoodMap) {
        if( stratifiedContexts.size() == 0 )
            return null;

        int countNBaseSolid = 0;
        int countRegularBaseSolid = 0;

        for( final AlignmentContext context : stratifiedContexts.values() ) {
            for( final PileupElement p : context.getBasePileup()) {
                final String platform = p.getRead().getReadGroup().getPlatform();
                if( platform != null && platform.toUpperCase().contains("SOLID") ) {
                    if( BaseUtils.isNBase( p.getBase() ) ) {
                        countNBaseSolid++;
                    } else if( BaseUtils.isRegularBase( p.getBase() ) ) {
                        countRegularBaseSolid++;
                    }
                }
            }
        }
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(getKeyNames().get(0), String.format("%.4f", (double)countNBaseSolid / (double)(countNBaseSolid + countRegularBaseSolid + 1)));
        return map;
    }

    public List<String> getKeyNames() { return Arrays.asList("PercentNBaseSolid"); }

    public List<VCFInfoHeaderLine> getDescriptions() { return Arrays.asList(new VCFInfoHeaderLine("PercentNBaseSolid", 1, VCFHeaderLineType.Float, "Percentage of N bases in the pileup (counting only SOLiD reads)")); }
}
