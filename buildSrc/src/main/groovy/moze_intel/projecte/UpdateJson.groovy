package moze_intel.projecte

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

abstract class UpdateJson extends DefaultTask {

    @Input
    final Provider<String> minecraftVersion
    @Input
    final Provider<String> modVersion
    @OutputFile
    final RegularFileProperty outputFile

    UpdateJson() {
        minecraftVersion = providerFactory.gradleProperty('minecraft_version')
        modVersion = providerFactory.gradleProperty('projecte_version')
        outputFile = objectFactory.fileProperty().convention(projectLayout.projectDirectory.file('update.json'))
    }

    @Inject
    protected abstract ProviderFactory getProviderFactory()

    @Inject
    protected abstract ObjectFactory getObjectFactory()

    @Inject
    protected abstract ProjectLayout getProjectLayout()

    @TaskAction
    void execute() {
        def updateJsonFile = outputFile.get().asFile
        def updateJson = new JsonSlurper().parse(updateJsonFile) as Map

        String minecraftVersion = this.minecraftVersion.get()
        String modVersion = this.modVersion.get()

        def versionData = updateJson[minecraftVersion]
        if (versionData == null) {
            versionData = [:] as Map
            updateJson.put(minecraftVersion, versionData)
        }

        versionData[modVersion] = "See https://www.curseforge.com/minecraft/mc-mods/projecte/files for detailed information."

        // Update promos
        updateJson.promos."${minecraftVersion}-latest" = modVersion
        updateJson.promos."${minecraftVersion}-recommended" = modVersion

        updateJsonFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(updateJson))
    }
}
