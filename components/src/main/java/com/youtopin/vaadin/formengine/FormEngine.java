package com.youtopin.vaadin.formengine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.I18NProvider;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.binder.BinderOrchestrator;
import com.youtopin.vaadin.formengine.definition.ActionDefinition;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;
import com.youtopin.vaadin.formengine.definition.GroupDefinition;
import com.youtopin.vaadin.formengine.definition.RepeatableDefinition;
import com.youtopin.vaadin.formengine.definition.SectionDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.registry.FieldFactoryContext;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.formengine.registry.FieldRegistry;
import com.youtopin.vaadin.formengine.scanner.AnnotationFormScanner;

/**
 * Entry point for rendering annotated forms.
 */
public final class FormEngine {

    private final AnnotationFormScanner scanner = new AnnotationFormScanner();
    private final OptionCatalogRegistry optionCatalogRegistry;

    public FormEngine(OptionCatalogRegistry optionCatalogRegistry) {
        this.optionCatalogRegistry = Objects.requireNonNull(optionCatalogRegistry, "optionCatalogRegistry");
    }

    public <T> RenderedForm<T> render(Class<?> definitionClass,
                                      I18NProvider provider,
                                      Locale locale,
                                      boolean rtl) {
        UiForm formAnnotation = definitionClass.getAnnotation(UiForm.class);
        if (formAnnotation == null) {
            throw new IllegalArgumentException("Class " + definitionClass + " is not annotated with @UiForm");
        }
        FormDefinition definition = scanner.scan(definitionClass);
        @SuppressWarnings("unchecked")
        Class<T> beanType = (Class<T>) definition.getBeanType();
        BinderOrchestrator<T> orchestrator = new BinderOrchestrator<>(beanType, locale);
        FieldRegistry registry = new FieldRegistry(optionCatalogRegistry);
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.getStyle().set("gap", "var(--lumo-space-l)");
        Map<FieldDefinition, FieldInstance> instances = new HashMap<>();
        Map<String, Button> actionButtons = new java.util.LinkedHashMap<>();
        Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatableGroups = new LinkedHashMap<>();
        FieldFactoryContext context = new FieldFactoryContext(definition, locale, rtl,
                (key, loc) -> provider.getTranslation(key, loc), component -> {
                    component.getElement().getThemeList().add("ytp-field");
                });
        List<SectionDefinition> orderedSections = new ArrayList<>(definition.getSections());
        orderedSections.sort(Comparator.comparingInt(SectionDefinition::getOrder));

        Map<String, List<ActionDefinition>> sectionActions = definition.getActions().stream()
                .filter(action -> action.getPlacement() == com.youtopin.vaadin.formengine.annotation.UiAction.Placement.SECTION_FOOTER)
                .filter(action -> !action.getSectionId().isBlank())
                .collect(java.util.stream.Collectors.groupingBy(ActionDefinition::getSectionId, LinkedHashMap::new, java.util.stream.Collectors.toList()));

        for (SectionDefinition section : orderedSections) {
            VerticalLayout sectionLayout = new VerticalLayout();
            sectionLayout.addClassName("form-engine-section");
            sectionLayout.getStyle().set("background-color", "var(--lumo-base-color)");
            sectionLayout.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
            if (!section.getTitleKey().isBlank()) {
                sectionLayout.add(new com.vaadin.flow.component.html.H3(context.translate(section.getTitleKey())));
            }
            for (GroupDefinition group : section.getGroups()) {
                Component renderedGroup = renderGroup(group, registry, context, instances, repeatableGroups);
                sectionLayout.add(renderedGroup);
            }
            List<ActionDefinition> actionsForSection = sectionActions.getOrDefault(section.getId(), List.of());
            if (!actionsForSection.isEmpty()) {
                com.vaadin.flow.component.orderedlayout.HorizontalLayout sectionActionRow = createActionRow();
                layoutSectionActions(actionsForSection, sectionActionRow, context, actionButtons);
                sectionLayout.add(sectionActionRow);
            }
            layout.add(sectionLayout);
        }

        com.vaadin.flow.component.orderedlayout.HorizontalLayout headerActions = null;
        List<ActionDefinition> headerDefinitions = definition.getActions().stream()
                .filter(action -> action.getPlacement() == com.youtopin.vaadin.formengine.annotation.UiAction.Placement.HEADER)
                .toList();
        if (!headerDefinitions.isEmpty()) {
            headerActions = createActionRow();
            layoutSectionActions(headerDefinitions, headerActions, context, actionButtons);
            layout.addComponentAtIndex(0, headerActions);
        }

        com.vaadin.flow.component.orderedlayout.HorizontalLayout footerActions = null;
        List<ActionDefinition> footerDefinitions = definition.getActions().stream()
                .filter(action -> action.getPlacement() == com.youtopin.vaadin.formengine.annotation.UiAction.Placement.FOOTER)
                .toList();
        if (!footerDefinitions.isEmpty()) {
            footerActions = createActionRow();
            layoutSectionActions(footerDefinitions, footerActions, context, actionButtons);
            layout.add(footerActions);
        }

        return new RenderedForm<>(definition, orchestrator, layout, instances, headerActions, footerActions, actionButtons,
                repeatableGroups);
    }

    public static final class RenderedForm<T> {
        private final FormDefinition definition;
        private final BinderOrchestrator<T> orchestrator;
        private final VerticalLayout layout;
        private final Map<FieldDefinition, FieldInstance> fields;
        private final Map<String, Button> actionButtons;
        private final Map<String, ActionDefinition> actionDefinitions;
        private final com.vaadin.flow.component.orderedlayout.HorizontalLayout headerActions;
        private final com.vaadin.flow.component.orderedlayout.HorizontalLayout footerActions;
        private final Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatableGroups;
        private java.util.function.Supplier<T> actionBeanSupplier;

        private RenderedForm(FormDefinition definition,
                             BinderOrchestrator<T> orchestrator,
                             VerticalLayout layout,
                             Map<FieldDefinition, FieldInstance> fields,
                             com.vaadin.flow.component.orderedlayout.HorizontalLayout headerActions,
                             com.vaadin.flow.component.orderedlayout.HorizontalLayout footerActions,
                             Map<String, Button> actionButtons,
                             Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatableGroups) {
            this.definition = definition;
            this.orchestrator = orchestrator;
            this.layout = layout;
            this.fields = fields;
            this.headerActions = headerActions;
            this.footerActions = footerActions;
            this.actionButtons = new LinkedHashMap<>(actionButtons);
            this.actionDefinitions = definition.getActions().stream()
                    .collect(java.util.stream.Collectors.toMap(ActionDefinition::getId, def -> def, (a, b) -> a,
                            LinkedHashMap::new));
            this.repeatableGroups = repeatableGroups;
        }

        public FormDefinition getDefinition() {
            return definition;
        }

        public BinderOrchestrator<T> getOrchestrator() {
            return orchestrator;
        }

        public VerticalLayout getLayout() {
            return layout;
        }

        public Map<FieldDefinition, FieldInstance> getFields() {
            return fields;
        }

        public Component getActionBar() {
            return footerActions == null ? new com.vaadin.flow.component.orderedlayout.HorizontalLayout() : footerActions;
        }

        public Map<String, Button> getActionButtons() {
            return java.util.Collections.unmodifiableMap(actionButtons);
        }

        public Map<String, List<Map<FieldDefinition, FieldInstance>>> getRepeatableGroups() {
            return repeatableGroups.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey,
                            entry -> entry.getValue().stream()
                                    .map(Map::copyOf)
                                    .toList()));
        }

        public void setActionBeanSupplier(java.util.function.Supplier<T> supplier) {
            this.actionBeanSupplier = supplier;
        }

        public void addActionHandler(String actionId, ActionHandler<T> handler) {
            Objects.requireNonNull(handler, "handler");
            Button button = actionButtons.get(actionId);
            if (button == null) {
                throw new IllegalArgumentException("Unknown action id " + actionId);
            }
            button.addClickListener(event -> {
                if (actionBeanSupplier == null) {
                    throw new IllegalStateException("Action bean supplier not configured");
                }
                T bean = actionBeanSupplier.get();
                if (bean == null) {
                    throw new IllegalStateException("Action bean supplier returned null");
                }
                try {
                    orchestrator.writeBean(bean);
                    handler.onAction(new ActionExecutionContext<>(actionDefinitions.get(actionId), bean, orchestrator));
                } catch (com.vaadin.flow.data.binder.ValidationException ex) {
                    throw new RuntimeException("Validation failed for action " + actionId, ex);
                }
            });
        }

    }

    private void layoutSectionActions(List<ActionDefinition> definitions,
                                      com.vaadin.flow.component.orderedlayout.HorizontalLayout target,
                                      FieldFactoryContext context,
                                      Map<String, Button> actionButtons) {
        definitions.stream()
                .sorted(Comparator.comparingInt(ActionDefinition::getOrder))
                .forEach(action -> {
                    Button button = createActionButton(action, context);
                    target.add(button);
                    actionButtons.put(action.getId(), button);
                });
    }

    private Component renderGroup(GroupDefinition group,
                                  FieldRegistry registry,
                                  FieldFactoryContext context,
                                  Map<FieldDefinition, FieldInstance> instances,
                                  Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatableGroups) {
        if (group.getRepeatableDefinition().isEnabled()) {
            return createRepeatableGroup(group, registry, context, repeatableGroups);
        }
        com.vaadin.flow.component.formlayout.FormLayout formLayout = new com.vaadin.flow.component.formlayout.FormLayout();
        formLayout.setResponsiveSteps(new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", group.getColumns()));
        for (FieldDefinition fieldDefinition : group.getFields()) {
            FieldInstance instance = registry.create(fieldDefinition, context);
            instances.put(fieldDefinition, instance);
            Component component = instance.getComponent();
            formLayout.add(component);
            if (fieldDefinition.getColSpan() > 1) {
                formLayout.setColspan(component, fieldDefinition.getColSpan());
            }
            if (fieldDefinition.getRowSpan() > 1) {
                component.getElement().getStyle().set("grid-row-end", "span " + fieldDefinition.getRowSpan());
            }
        }
        return formLayout;
    }

    private Component createRepeatableGroup(GroupDefinition group,
                                            FieldRegistry registry,
                                            FieldFactoryContext context,
                                            Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatableGroups) {
        com.vaadin.flow.component.orderedlayout.VerticalLayout wrapper = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.addClassName("form-engine-repeatable-group");
        RepeatableDefinition repeatable = group.getRepeatableDefinition();
        List<Map<FieldDefinition, FieldInstance>> entries = repeatableGroups.computeIfAbsent(group.getId(), key -> new ArrayList<>());
        com.vaadin.flow.component.orderedlayout.VerticalLayout entriesContainer = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        entriesContainer.setPadding(false);
        entriesContainer.setSpacing(true);
        wrapper.add(entriesContainer);
        Button addEntry = new Button(context.translate("form.repeatable.addGroup"));
        addEntry.getElement().setAttribute("aria-label", context.translate("form.repeatable.addGroup"));
        context.applyTheme(addEntry);
        addEntry.addClickListener(event -> {
            if (entries.size() >= repeatable.getMax()) {
                addEntry.setEnabled(false);
                return;
            }
            addRepeatableEntry(group, registry, context, entriesContainer, entries, repeatable, addEntry);
        });
        int initial = repeatable.getMin();
        for (int i = 0; i < initial; i++) {
            addRepeatableEntry(group, registry, context, entriesContainer, entries, repeatable, addEntry);
        }
        updateAddButtonState(addEntry, repeatable, entries);
        updateRemoveButtons(entriesContainer, repeatable, entries.size());
        wrapper.add(addEntry);
        return wrapper;
    }

    private void addRepeatableEntry(GroupDefinition group,
                                    FieldRegistry registry,
                                    FieldFactoryContext context,
                                    com.vaadin.flow.component.orderedlayout.VerticalLayout entriesContainer,
                                    List<Map<FieldDefinition, FieldInstance>> entries,
                                    RepeatableDefinition repeatable,
                                    Button addEntryButton) {
        if (entries.size() >= repeatable.getMax()) {
            updateAddButtonState(addEntryButton, repeatable, entries);
            return;
        }
        com.vaadin.flow.component.orderedlayout.VerticalLayout entryWrapper = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        entryWrapper.setPadding(false);
        entryWrapper.setSpacing(false);
        entryWrapper.getElement().setAttribute("data-repeatable-entry", group.getId());
        Button removeButton = new Button(context.translate("form.repeatable.removeGroup"));
        removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        removeButton.getElement().setAttribute("data-repeatable-remove", "true");
        removeButton.getElement().setAttribute("aria-label", context.translate("form.repeatable.removeGroup"));
        context.applyTheme(removeButton);
        com.vaadin.flow.component.formlayout.FormLayout formLayout = new com.vaadin.flow.component.formlayout.FormLayout();
        formLayout.setResponsiveSteps(new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", group.getColumns()));
        Map<FieldDefinition, FieldInstance> entryInstances = new LinkedHashMap<>();
        for (FieldDefinition fieldDefinition : group.getFields()) {
            FieldInstance instance = registry.create(fieldDefinition, context);
            entryInstances.put(fieldDefinition, instance);
            Component component = instance.getComponent();
            formLayout.add(component);
            if (fieldDefinition.getColSpan() > 1) {
                formLayout.setColspan(component, fieldDefinition.getColSpan());
            }
            if (fieldDefinition.getRowSpan() > 1) {
                component.getElement().getStyle().set("grid-row-end", "span " + fieldDefinition.getRowSpan());
            }
        }
        removeButton.addClickListener(event -> {
            if (entries.size() <= repeatable.getMin()) {
                return;
            }
            entriesContainer.remove(entryWrapper);
            entries.remove(entryInstances);
            updateAddButtonState(addEntryButton, repeatable, entries);
            updateRemoveButtons(entriesContainer, repeatable, entries.size());
        });
        entryWrapper.add(removeButton, formLayout);
        entriesContainer.add(entryWrapper);
        entries.add(entryInstances);
        updateAddButtonState(addEntryButton, repeatable, entries);
        updateRemoveButtons(entriesContainer, repeatable, entries.size());
    }

    private void updateAddButtonState(Button addEntryButton,
                                      RepeatableDefinition repeatable,
                                      List<Map<FieldDefinition, FieldInstance>> entries) {
        addEntryButton.setEnabled(entries.size() < repeatable.getMax());
    }

    private void updateRemoveButtons(com.vaadin.flow.component.orderedlayout.VerticalLayout entriesContainer,
                                     RepeatableDefinition repeatable,
                                     int entryCount) {
        boolean canRemove = entryCount > repeatable.getMin();
        entriesContainer.getChildren()
                .filter(component -> component instanceof com.vaadin.flow.component.orderedlayout.VerticalLayout)
                .map(component -> (com.vaadin.flow.component.orderedlayout.VerticalLayout) component)
                .forEach(wrapper -> wrapper.getChildren()
                        .filter(child -> child instanceof Button)
                        .map(child -> (Button) child)
                        .filter(button -> "true".equals(button.getElement().getAttribute("data-repeatable-remove")))
                        .forEach(button -> button.setEnabled(canRemove)));
    }

    private com.vaadin.flow.component.orderedlayout.HorizontalLayout createActionRow() {
        com.vaadin.flow.component.orderedlayout.HorizontalLayout row = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
        row.getStyle().set("gap", "var(--lumo-space-m)");
        return row;
    }

    private Button createActionButton(ActionDefinition definition, FieldFactoryContext context) {
        String caption = context.translate(definition.getLabelKey());
        Button button = new Button(caption.isBlank() ? definition.getId() : caption);
        if (!definition.getDescriptionKey().isBlank()) {
            String description = context.translate(definition.getDescriptionKey());
            button.getElement().setProperty("title", description);
            button.getElement().setAttribute("aria-label", description.isBlank() ? caption : description);
        } else if (!caption.isBlank()) {
            button.getElement().setAttribute("aria-label", caption);
        }
        if (definition.getType() == com.youtopin.vaadin.formengine.annotation.UiAction.ActionType.SUBMIT) {
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
        button.setId(definition.getId());
        context.applyTheme(button);
        return button;
    }

    public interface ActionHandler<T> {
        void onAction(ActionExecutionContext<T> context) throws com.vaadin.flow.data.binder.ValidationException;
    }

    public static final class ActionExecutionContext<T> {
        private final ActionDefinition actionDefinition;
        private final T bean;
        private final BinderOrchestrator<T> orchestrator;

        private ActionExecutionContext(ActionDefinition actionDefinition, T bean, BinderOrchestrator<T> orchestrator) {
            this.actionDefinition = actionDefinition;
            this.bean = bean;
            this.orchestrator = orchestrator;
        }

        public ActionDefinition getActionDefinition() {
            return actionDefinition;
        }

        public T getBean() {
            return bean;
        }

        public BinderOrchestrator<T> getOrchestrator() {
            return orchestrator;
        }
    }
}
