package com.anatawa12.autoVisitor.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class AutoVisitorGradlePlugin : KotlinCompilerPluginSupportPlugin {
    lateinit var providers: ProviderFactory

    override fun apply(target: Project) {
        providers = target.providers

        val extension = AutoVisitorExtension(target)
        target.extensions.add("autoVisitor", extension)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return providers.provider(::emptyList)
    }

    override fun getCompilerPluginId(): String = "autovisitor"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.anatawa12.autoVisitor",
        artifactId = "compiler-plugin",
        version = Constants.version,
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}
