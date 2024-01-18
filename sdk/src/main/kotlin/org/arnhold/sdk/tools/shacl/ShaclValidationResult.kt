package org.arnhold.sdk.tools.shacl

data class ShaclValidationResult(
        var maDMP: String,
        var shape: String,
        var conforms: Boolean,
        var messages: List<String>
)