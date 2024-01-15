package org.arnhold.evaluator.evaluationManager

import mu.KotlinLogging
import org.apache.jena.ontology.OntModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.Reasoner
import org.apache.jena.reasoner.ReasonerRegistry
import org.arnhold.evaluator.dataProvision.DataProviderService
import org.arnhold.evaluator.evaluationProvider.EvaluationProviderService
import org.arnhold.evaluator.metricProcessing.MetricProcessingService
import org.arnhold.sdk.common.dqv.Category
import org.arnhold.sdk.common.dqv.Dimension
import org.arnhold.sdk.common.dqv.Measurement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EvaluationManagerServiceImpl @Autowired constructor(
    val evaluationProviderService: EvaluationProviderService,
    val dataProviderService: DataProviderService,
    val metricProcessingService: MetricProcessingService
) : EvaluationManagerService {

    private val logger = KotlinLogging.logger {}
    override fun createEvaluation(parameters: EvaluationTaskParameters): EvaluationTaskResult {
        logger.info { "Create evaluation with parameters $parameters" }

        val contextDMPId = dataProviderService.loadContextualizedDMP(parameters.dmpLoaderParameters)
        val contextDMP = dataProviderService.getContextualizedDMP(contextDMPId)
        
        val measurements = metricProcessingService.produceAllMeasurements(contextDMP, parameters.dataLifecycle)
        val measurementsModel = measurementsToModel(dataProviderService.getDMPDQVOntology(), measurements)

        //After adding measurements to contextDMP save and run reasoner over dmpdqv to get correct rdf types from predicate relations
        logger.info { "Reason over measurements" }
        val reasoner: Reasoner = ReasonerRegistry.getOWLReasoner()
        reasoner.bindSchema(dataProviderService.getDMPDQVOntology())
        val reasonedMeasurementsModel = ModelFactory.createInfModel(reasoner, measurementsModel)
        dataProviderService.saveModel(reasonedMeasurementsModel)

        logger.info { "Return evaluation results" }
        return EvaluationTaskResult(
            success = true,
            message = "no messages yet",
            evaluationId = UUID.randomUUID().toString(),
            measurements = measurements
        )
    }

    override fun getEvaluatorInformation(): Map<Category, List<Dimension>> {
        return evaluationProviderService.getAllEvaluators().map { it.getPluginInformation() }.groupBy ( {it.belongsToCategory}, {it.applicableDimension} )
    }

    private fun measurementsToModel(dmpdqv: OntModel, measurements: List<Measurement>): Model {
        logger.info { "Integrate measurements into semantic DMP model" }
        val model = dmpdqv.baseModel
        measurements.mapIndexed {index, measurement -> measurement.toResource(dmpdqv, "Measurement_$index")}
        return model
    }
}