package org.arnhold.dmpeval.casestudy.evaluation.complianceCategoryEvaluators

import org.apache.jena.rdf.model.Model
import org.arnhold.dmpeval.casestudy.evaluation.CategoryDimmensionModels
import org.arnhold.dmpeval.casestudy.evaluation.EvaluationDimensionConstants
import org.arnhold.sdk.vocab.dqv.Measurement
import org.arnhold.sdk.evaluator.EvaluatorPlugin
import org.arnhold.sdk.evaluator.EvaluatorInformation
import org.arnhold.sdk.model.EvaluationTaskParameters
import org.arnhold.sdk.vocab.context.DMPContext
import org.springframework.stereotype.Component

@Component
class GuidelineComplianceEvaluator : EvaluatorPlugin {

    override fun getPluginIdentifier(): String {
        return EvaluationDimensionConstants.GUIDELINE_COMPLIANCE.toString()
    }

    override fun getPluginInformation(): EvaluatorInformation {
        return EvaluatorInformation(
            CategoryDimmensionModels.GUIDELINE_COMPLIANCE_DIMENSION,
            CategoryDimmensionModels.COMPLIANCE_CATEGORY,
            listOf()
        )
    }

    override fun getAllMeasurements(dmp: Model, context: List<DMPContext>, parameters: EvaluationTaskParameters): List<Measurement> {
        return listOf()
    }

    override fun supports(p0: String): Boolean {
        return true
    }
}