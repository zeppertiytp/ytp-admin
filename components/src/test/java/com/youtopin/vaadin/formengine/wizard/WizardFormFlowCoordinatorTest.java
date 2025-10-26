package com.youtopin.vaadin.formengine.wizard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.I18NProvider;
import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class WizardFormFlowCoordinatorTest {

    private static final String STEP_ONE = "step-one";
    private static final String STEP_TWO = "step-two";

    private FormEngine formEngine;
    private HorizontalWizard wizard;
    private TestContext context;
    private WizardFormFlowCoordinator<TestContext> coordinator;

    @BeforeEach
    void setUp() {
        formEngine = new FormEngine(new OptionCatalogRegistry());
        wizard = new HorizontalWizard();
        context = new TestContext();
        coordinator = new WizardFormFlowCoordinator<>(wizard, List.of(STEP_ONE, STEP_TWO), context);
    }

    @Test
    void propagatesContextMutationsFromFormActions() {
        RenderedForm<StepOneBean> stepOneForm = renderForm(StepOneDefinition.class, context::getStepOne);
        RenderedForm<StepTwoBean> stepTwoForm = renderForm(StepTwoDefinition.class, context::getStepTwo);

        coordinator.registerStepForm(STEP_ONE, stepOneForm, TestContext::getStepOne);
        coordinator.registerStepForm(STEP_TWO, stepTwoForm, TestContext::getStepTwo);
        coordinator.configureWizard(this::buildStep);

        TextField field = (TextField) findField(stepOneForm, "value").getValueComponent();
        field.setValue("Updated");

        stepOneForm.addActionHandler("step-one-next", action -> coordinator.markStepCompleted(STEP_ONE));
        Button nextButton = stepOneForm.getActionButtons().get("step-one-next");
        nextButton.click();

        assertThat(context.getStepOne().getValue()).isEqualTo("Updated");
    }

    @Test
    void honoursCompletionRulesAndAccessibility() {
        RenderedForm<StepOneBean> stepOneForm = renderForm(StepOneDefinition.class, context::getStepOne);
        RenderedForm<StepTwoBean> stepTwoForm = renderForm(StepTwoDefinition.class, context::getStepTwo);
        coordinator.registerStepForm(STEP_ONE, stepOneForm, TestContext::getStepOne);
        coordinator.registerStepForm(STEP_TWO, stepTwoForm, TestContext::getStepTwo);
        coordinator.configureWizard(this::buildStep);

        assertThat(coordinator.isStepAccessible(STEP_ONE)).isTrue();
        assertThat(coordinator.isStepAccessible(STEP_TWO)).isFalse();

        coordinator.markStepCompleted(STEP_ONE);
        assertThat(coordinator.isStepAccessible(STEP_TWO)).isTrue();
        assertThat(coordinator.computeResumeIndex()).isEqualTo(1);
    }

    @Test
    void updatesWizardSelection() {
        RenderedForm<StepOneBean> stepOneForm = renderForm(StepOneDefinition.class, context::getStepOne);
        RenderedForm<StepTwoBean> stepTwoForm = renderForm(StepTwoDefinition.class, context::getStepTwo);
        coordinator.registerStepForm(STEP_ONE, stepOneForm, TestContext::getStepOne);
        coordinator.registerStepForm(STEP_TWO, stepTwoForm, TestContext::getStepTwo);

        AtomicReference<String> selected = new AtomicReference<>();
        coordinator.onStepSelection((stepId, ctx) -> selected.set(stepId));
        coordinator.configureWizard(this::buildStep);

        coordinator.setCurrentStepId(STEP_TWO);
        assertThat(selected.get()).isEqualTo(STEP_TWO);
        assertThat(wizard.getCurrentStepId()).contains(STEP_TWO);

        wizard.setCurrentStepId(STEP_ONE);
        assertThat(coordinator.getCurrentStepId()).isEqualTo(STEP_ONE);
    }

    @Test
    void executesFormSubmissionEndToEnd() {
        RenderedForm<StepOneBean> stepOneForm = renderForm(StepOneDefinition.class, context::getStepOne);
        coordinator.registerStepForm(STEP_ONE, stepOneForm, TestContext::getStepOne);
        coordinator.registerStepForm(STEP_TWO, renderForm(StepTwoDefinition.class, context::getStepTwo), TestContext::getStepTwo);

        List<String> completed = new ArrayList<>();
        AtomicInteger selections = new AtomicInteger();
        coordinator.onStepCompletion((stepId, ctx) -> completed.add(stepId));
        coordinator.onStepSelection((stepId, ctx) -> selections.incrementAndGet());
        coordinator.configureWizard(this::buildStep);

        stepOneForm.addActionHandler("step-one-next", action -> {
            coordinator.markStepCompleted(STEP_ONE);
            coordinator.nextStep(STEP_ONE).ifPresent(coordinator::setCurrentStepId);
        });

        TextField field = (TextField) findField(stepOneForm, "value").getValueComponent();
        field.setValue("coordinated");
        Button button = stepOneForm.getActionButtons().get("step-one-next");
        button.click();

        assertThat(completed).containsExactly(STEP_ONE);
        assertThat(selections.get()).isGreaterThanOrEqualTo(1);
        assertThat(context.getStepOne().getValue()).isEqualTo("coordinated");
        assertThat(coordinator.getCurrentStepId()).isEqualTo(STEP_TWO);
    }

    private <T> RenderedForm<T> renderForm(Class<?> definitionClass, java.util.function.Supplier<T> supplier) {
        RenderedForm<T> rendered = formEngine.render(definitionClass, new StubI18NProvider(), Locale.ENGLISH, false);
        rendered.getFields().forEach((FieldDefinition definition, FieldInstance instance) ->
                rendered.getOrchestrator().bindField(instance, definition));
        rendered.initializeWithBean(supplier.get());
        return rendered;
    }

    private WizardStep buildStep(String stepId) {
        return WizardStep.of(stepId, stepId);
    }

    private FieldInstance findField(RenderedForm<?> rendered, String path) {
        return rendered.getFields().entrySet().stream()
                .filter(entry -> path.equals(entry.getKey().getPath()))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    private static final class StubI18NProvider implements I18NProvider {

        @Override
        public List<Locale> getProvidedLocales() {
            return List.of(Locale.ENGLISH);
        }

        @Override
        public String getTranslation(String key, Locale locale, Object... params) {
            if (key == null) {
                return "";
            }
            return params == null || params.length == 0 ? key : key + java.util.Arrays.toString(params);
        }
    }

    public static class TestContext implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final StepOneBean stepOne = new StepOneBean();
        private final StepTwoBean stepTwo = new StepTwoBean();

        public StepOneBean getStepOne() {
            return stepOne;
        }

        public StepTwoBean getStepTwo() {
            return stepTwo;
        }
    }

    public static class StepOneBean implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String value = "";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value == null ? "" : value;
        }
    }

    public static class StepTwoBean implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String confirmation = "";

        public String getConfirmation() {
            return confirmation;
        }

        public void setConfirmation(String confirmation) {
            this.confirmation = confirmation == null ? "" : confirmation;
        }
    }

    @UiForm(id = STEP_ONE,
            titleKey = "",
            descriptionKey = "",
            bean = StepOneBean.class,
            sections = StepOneSection.class,
            actions = {
                    @UiAction(id = "step-one-next", labelKey = "next", placement = UiAction.Placement.FOOTER)
            })
    public static class StepOneDefinition {
    }

    @UiSection(id = "step-one-section", titleKey = "", groups = StepOneGroup.class)
    public static class StepOneSection {
    }

    @UiGroup(id = "step-one-group", columns = 1)
    public static class StepOneGroup {

        @UiField(path = "value", component = UiField.ComponentType.TEXT, labelKey = "value")
        public void value() {
        }
    }

    @UiForm(id = STEP_TWO,
            titleKey = "",
            descriptionKey = "",
            bean = StepTwoBean.class,
            sections = StepTwoSection.class,
            actions = {
                    @UiAction(id = "step-two-finish", labelKey = "finish", placement = UiAction.Placement.FOOTER)
            })
    public static class StepTwoDefinition {
    }

    @UiSection(id = "step-two-section", titleKey = "", groups = StepTwoGroup.class)
    public static class StepTwoSection {
    }

    @UiGroup(id = "step-two-group", columns = 1)
    public static class StepTwoGroup {

        @UiField(path = "confirmation", component = UiField.ComponentType.TEXT, labelKey = "confirmation")
        public void confirmation() {
        }
    }
}
