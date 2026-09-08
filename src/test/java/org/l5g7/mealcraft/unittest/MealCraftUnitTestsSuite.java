package org.l5g7.mealcraft.unittest;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
     //   MealPlanServiceImplTest.class,
        EventCellTest.class,
        ProductServiceUnitTests.class
})
public class MealCraftUnitTestsSuite {
}
