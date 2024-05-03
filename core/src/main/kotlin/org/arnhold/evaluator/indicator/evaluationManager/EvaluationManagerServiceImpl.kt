package org.arnhold.evaluator.indicator.evaluationManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.jena.ontology.OntModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.Reasoner
import org.apache.jena.reasoner.ReasonerRegistry
import org.arnhold.evaluator.harvester.dataProvider.DataProviderService
import org.arnhold.evaluator.indicator.evaluationProvider.EvaluationProviderService
import org.arnhold.sdk.vocab.dqv.Category
import org.arnhold.sdk.vocab.dqv.Dimension
import org.arnhold.sdk.vocab.dqv.Measurement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EvaluationManagerServiceImpl @Autowired constructor(
    val evaluationProviderService: EvaluationProviderService,
    val dataProviderService: DataProviderService
) : EvaluationManagerService {

    private val logger = KotlinLogging.logger {}
    override fun createEvaluation(parameters: EvaluationTaskParameters): EvaluationTaskResult {
        logger.info { "Create evaluation with parameters $parameters" }

        val dmpStoreId = dataProviderService.loadDMP(parameters.dmpLoaderParameters)
        val dmp = dataProviderService.getDMP(dmpStoreId)

        val context = runBlocking(Dispatchers.Default) {
            return@runBlocking dataProviderService.loadContext(dmp)
        }

        val measurements = runBlocking(Dispatchers.Default) {
            return@runBlocking evaluationProviderService.produceAllMeasurements(dmp, context, parameters.dataLifecycle)
        }

        logger.info { "Created ${measurements.size} measurements" }

        val result = EvaluationTaskResult(
            success = true,
            message = "",
            evaluationId = UUID.randomUUID().toString(),
            measurements = measurements
        )

        saveMeasurements(result)
        return result
    }

    private fun saveMeasurements(result: EvaluationTaskResult) {
        val measurementsModel = measurementsToModel(dataProviderService.getDMPDQVOntology(), result.measurements)
        logger.info { "Reason over measurements" }
        val reasoner: Reasoner = ReasonerRegistry.getOWLReasoner()
        reasoner.bindSchema(dataProviderService.getDMPDQVOntology())
        val reasonedMeasurementsModel = ModelFactory.createInfModel(reasoner, measurementsModel)
        val uuid = dataProviderService.saveModel(reasonedMeasurementsModel)
        dataProviderService.saveAsJson<EvaluationTaskResult>(result, uuid)
    }

    override fun getEvaluatorInformation(): Map<Category, List<Dimension>> {
        return evaluationProviderService.getAllEvaluators().map { it.getPluginInformation() }.groupBy ( {it.belongsToCategory}, {it.applicableDimension} )
    }

    private fun measurementsToModel(dmpdqv: OntModel, measurements: List<Measurement>): Model {
        logger.info { "Convert measurements to DMPQV" }
        val model = dmpdqv.baseModel
        measurements.mapIndexed {index, measurement -> measurement.toResource(dmpdqv, "Measurement_$index")}
        return model
    }
}