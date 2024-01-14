package org.arnhold.sdk.evaluator

import org.apache.jena.rdf.model.Model
import org.arnhold.sdk.common.constants.DataLifecycle
import org.arnhold.sdk.common.dqv.Measurement
import org.arnhold.sdk.plugin.ConfigurablePlugin

interface DimensionEvaluatorPlugin: ConfigurablePlugin<String, EvaluatorInformation> {
    fun getAllMeasurements(dmp: Model, lifecycle: DataLifecycle): List<Measurement>
}