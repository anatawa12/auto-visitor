package com.anatawa12.autoVisitor.gradle

import org.gradle.api.Project
import org.gradle.util.GUtil

open class AutoVisitorExtension(val project: Project) {
    private fun get(map: Map<String, Any>, defaultConfiguration: String): String {
        val sourceSetName = map["sourceSet"] ?: ""
        val configurationName = map["configuration"] ?: defaultConfiguration

        return GUtil.toLowerCamelCase("$sourceSetName $configurationName")
    }

    fun addLib(sourceSet: String) =
        addLib(mapOf("sourceSet" to sourceSet))

    fun addLib(map: Map<String, Any>) {
        project.dependencies.add(get(map, "compileOnly"),
            "com.anatawa12.autoVisitor:lib:${Constants.version}")
    }

    fun addAnnotationProcessor(sourceSet: String) =
        addAnnotationProcessor(mapOf("sourceSet" to sourceSet))

    fun addAnnotationProcessor(map: Map<String, Any>) {
        project.dependencies.add(get(map, "annotationProcessor"),
            "com.anatawa12.autoVisitor:lib:${Constants.version}")
    }
}
