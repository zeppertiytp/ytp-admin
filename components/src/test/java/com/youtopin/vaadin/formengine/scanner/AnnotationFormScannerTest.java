package com.youtopin.vaadin.formengine.scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.definition.ActionDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;

class AnnotationFormScannerTest {

    private final AnnotationFormScanner scanner = new AnnotationFormScanner();

    @Test
    void scanBuildsDefinition() {
        FormDefinition definition = scanner.scan(SampleForm.class);
        assertThat(definition.getId()).isEqualTo("sample-form");
        assertThat(definition.getSections()).hasSize(1);
        assertThat(definition.getSections().get(0).getGroups()).hasSize(1);
        assertThat(definition.getSections().get(0).getGroups().get(0).getFields()).hasSize(1);
        assertThat(definition.getActions()).hasSize(1);
        assertThat(definition.getActions().get(0).getId()).isEqualTo("submit");
        assertThat(definition.getActions().get(0).getPlacement()).isEqualTo(com.youtopin.vaadin.formengine.annotation.UiAction.Placement.FOOTER);
    }

    @Test
    void duplicatePathThrows() {
        assertThatThrownBy(() -> scanner.scan(InvalidDuplicateForm.class))
                .isInstanceOf(FormDefinitionException.class)
                .hasMessageContaining("Duplicate field path");
    }

    @Test
    void invalidPathThrows() {
        assertThatThrownBy(() -> scanner.scan(InvalidPathForm.class))
                .isInstanceOf(FormDefinitionException.class)
                .hasMessageContaining("Unable to resolve property");
    }

    @Test
    void customActionsAreCaptured() {
        FormDefinition definition = scanner.scan(ActionForm.class);
        assertThat(definition.getActions()).extracting(ActionDefinition::getId)
                .containsExactly("approve", "reject");
        assertThat(definition.getActions()).extracting(ActionDefinition::getPlacement)
                .containsExactly(com.youtopin.vaadin.formengine.annotation.UiAction.Placement.HEADER,
                        com.youtopin.vaadin.formengine.annotation.UiAction.Placement.FOOTER);
    }

    @UiForm(id = "sample-form", bean = SampleBean.class, sections = {SampleSection.class})
    static class SampleForm {
    }

    @UiSection(id = "s1", groups = {SampleGroup.class})
    static class SampleSection {
    }

    @UiGroup(id = "g1")
    static class SampleGroup {
        @UiField(path = "name", labelKey = "field.name")
        String name;
    }

    static class SampleBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @UiForm(id = "invalid-duplicate", bean = SampleBean.class, sections = {InvalidSection.class})
    static class InvalidDuplicateForm {
    }

    @UiSection(id = "is1", groups = {InvalidGroup.class})
    static class InvalidSection {
    }

    @UiGroup(id = "ig1")
    static class InvalidGroup {
        @UiField(path = "name", labelKey = "field.name")
        String name1;

        @UiField(path = "name", labelKey = "field.name")
        String name2;
    }

    @UiForm(id = "invalid-path", bean = SampleBean.class, sections = {InvalidPathSection.class})
    static class InvalidPathForm {
    }

    @UiSection(id = "ips1", groups = {InvalidPathGroup.class})
    static class InvalidPathSection {
    }

    @UiGroup(id = "ipg1")
    static class InvalidPathGroup {
        @UiField(path = "missing", labelKey = "field.missing")
        String missing;
    }

    @UiForm(id = "action-form", bean = SampleBean.class, sections = {SampleSection.class},
            actions = {
                    @com.youtopin.vaadin.formengine.annotation.UiAction(id = "approve", labelKey = "action.approve",
                            placement = com.youtopin.vaadin.formengine.annotation.UiAction.Placement.HEADER, order = 1),
                    @com.youtopin.vaadin.formengine.annotation.UiAction(id = "reject", labelKey = "action.reject",
                            placement = com.youtopin.vaadin.formengine.annotation.UiAction.Placement.FOOTER, order = 2)
            })
    static class ActionForm {
    }
}
