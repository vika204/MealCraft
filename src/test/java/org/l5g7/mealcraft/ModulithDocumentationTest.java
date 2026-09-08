package org.l5g7.mealcraft;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.docs.Documenter.DiagramOptions;
import org.springframework.modulith.docs.Documenter.DiagramOptions.ElementsWithoutRelationships;

class ModulithDocumentationTest {


    // тест генерує документацію для модульної архітектури.
    @Test
    void generateDocumentation() {

        ApplicationModules modules =
                ApplicationModules.of(MealCraftApplication.class);

        DiagramOptions diagramOptions = DiagramOptions.defaults()
                .withElementsWithoutRelationships(ElementsWithoutRelationships.VISIBLE);

        Documenter.Options documenterOptions =
                Documenter.Options.defaults()
                        .withOutputFolder("docs");

        new Documenter(modules, documenterOptions)
                .writeModulesAsPlantUml(diagramOptions)
                .writeIndividualModulesAsPlantUml(diagramOptions);
    }
}