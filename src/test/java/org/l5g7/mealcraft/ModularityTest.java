package org.l5g7.mealcraft;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    // тест перевіряє правильність модульної архітектури,
    // дотримання меж модулів та відсутність циклічних залежностей.
    @Test
    void verifyModularStructure() {
        ApplicationModules.of(MealCraftApplication.class).verify();
    }
}