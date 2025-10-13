package com.youtopin.vaadin.samples.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.youtopin.vaadin.samples")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_does_not_depend_on_other_layers =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..infrastructure..", "..ui..", "..config..");

    @ArchTest
    static final ArchRule application_isolated_from_infrastructure_and_ui =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..", "..ui..", "..config..", "..frontend..");

    @ArchTest
    static final ArchRule ui_does_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..ui..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..");

    @ArchTest
    static final ArchRule infrastructure_avoids_ui_dependencies =
            noClasses().that().resideInAPackage("..infrastructure..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..ui..", "..frontend..");
}
