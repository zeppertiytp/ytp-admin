package com.youtopin.vaadin.formengine;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.i18n.I18NProvider;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.binder.BinderOrchestrator;
import com.youtopin.vaadin.formengine.binder.DynamicPropertyBag;
import com.youtopin.vaadin.formengine.definition.ActionDefinition;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;
import com.youtopin.vaadin.formengine.definition.GroupDefinition;
import com.youtopin.vaadin.formengine.definition.RepeatableDefinition;
import com.youtopin.vaadin.formengine.definition.SectionDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalog;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.options.OptionItem;
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

    private static final Pattern SIMPLE_COMPARISON = Pattern.compile("^\\s*(.+?)\\s*(==|!=)\\s*(.+)\\s*$");

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
        return render(definition, provider, locale, rtl);
    }

    public <T> RenderedForm<T> render(FormDefinition definition,
                                      I18NProvider provider,
                                      Locale locale,
                                      boolean rtl) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(provider, "provider");
        @SuppressWarnings("unchecked")
        Class<T> beanType = (Class<T>) definition.getBeanType();
        BinderOrchestrator<T> orchestrator = new BinderOrchestrator<>(beanType,
                key -> key == null || key.isBlank() ? "" : provider.getTranslation(key, locale));
        FieldRegistry registry = new FieldRegistry(optionCatalogRegistry);
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.getStyle().set("gap", "var(--lumo-space-l)");
        Map<FieldDefinition, FieldInstance> instances = new HashMap<>();
        Map<String, Button> actionButtons = new java.util.LinkedHashMap<>();
        Map<String, RepeatableGroupState> repeatableGroups = new LinkedHashMap<>();
        FieldFactoryContext context = new FieldFactoryContext(definition, locale, rtl,
                (key, loc) -> provider.getTranslation(key, loc), component -> {
                    component.getElement().getThemeList().add("ytp-field");
                });
        List<SectionDefinition> orderedSections = new ArrayList<>(definition.getSections());
        orderedSections.sort(Comparator.comparingInt(SectionDefinition::getOrder));
        Map<SectionDefinition, VerticalLayout> sectionLayouts = new LinkedHashMap<>();

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
                Component renderedGroup = renderGroup(group, registry, context, instances, repeatableGroups, beanType);
                sectionLayout.add(renderedGroup);
            }
            sectionLayouts.put(section, sectionLayout);
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
                repeatableGroups, sectionLayouts, registry, context, optionCatalogRegistry, locale);
    }

    public final class RenderedForm<T> {
        private final FormDefinition definition;
        private final BinderOrchestrator<T> orchestrator;
        private final VerticalLayout layout;
        private final Map<FieldDefinition, FieldInstance> fields;
        private final Map<String, Button> actionButtons;
        private final Map<String, ActionDefinition> actionDefinitions;
        private final com.vaadin.flow.component.orderedlayout.HorizontalLayout headerActions;
        private final com.vaadin.flow.component.orderedlayout.HorizontalLayout footerActions;
        private final Map<String, RepeatableGroupState> repeatableGroups;
        private final Map<SectionDefinition, VerticalLayout> sections;
        private java.util.function.Supplier<T> actionBeanSupplier;
        private final List<ValidationFailureListener<T>> validationFailureListeners = new CopyOnWriteArrayList<>();
        private final List<ReadOnlyOverride<T>> readOnlyOverrides = new CopyOnWriteArrayList<>();
        private final FieldRegistry fieldRegistry;
        private final FieldFactoryContext fieldContext;
        private final OptionCatalogRegistry optionCatalogRegistry;
        private final Locale locale;
        private T currentBean;

        private RenderedForm(FormDefinition definition,
                             BinderOrchestrator<T> orchestrator,
                             VerticalLayout layout,
                             Map<FieldDefinition, FieldInstance> fields,
                             com.vaadin.flow.component.orderedlayout.HorizontalLayout headerActions,
                             com.vaadin.flow.component.orderedlayout.HorizontalLayout footerActions,
                             Map<String, Button> actionButtons,
                             Map<String, RepeatableGroupState> repeatableGroups,
                             Map<SectionDefinition, VerticalLayout> sections,
                             FieldRegistry fieldRegistry,
                             FieldFactoryContext fieldContext,
                             OptionCatalogRegistry optionCatalogRegistry,
                             Locale locale) {
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
            this.sections = sections;
            this.fieldRegistry = fieldRegistry;
            this.fieldContext = fieldContext;
            this.optionCatalogRegistry = optionCatalogRegistry;
            this.locale = locale;
            this.currentBean = null;
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
                            entry -> entry.getValue().getEntries().stream()
                                    .map(RepeatableEntry::fields)
                                    .map(Map::copyOf)
                                    .toList()));
        }

        public Map<SectionDefinition, VerticalLayout> getSections() {
            return sections.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        public void setActionBeanSupplier(java.util.function.Supplier<T> supplier) {
            this.actionBeanSupplier = supplier;
        }

        public void addReadOnlyOverride(ReadOnlyOverride<T> override) {
            readOnlyOverrides.add(Objects.requireNonNull(override, "override"));
            applyReadOnlyState(currentBean);
        }

        @Deprecated
        public void addReadOnlyOverride(BiPredicate<FieldDefinition, T> override) {
            Objects.requireNonNull(override, "override");
            addReadOnlyOverride((ReadOnlyOverride<T>) (definition, context) ->
                    override.test(definition, context.getBean()));
        }

        public void refreshReadOnlyState() {
            applyReadOnlyState(currentBean);
        }

        public void initializeWithBean(T bean) {
            this.currentBean = bean;
            if (bean == null) {
                applyReadOnlyState(null);
                return;
            }
            fields.forEach((definition, instance) -> {
                Object propertyValue = orchestrator.readProperty(definition.getPath(), bean);
                applyComponentValue(instance, definition, propertyValue);
            });
            repeatableGroups.values().forEach(state -> populateRepeatableState(state, bean));
            applyReadOnlyState(bean);
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
                    applyRepeatableValues(bean);
                    handler.onAction(new ActionExecutionContext<>(actionDefinitions.get(actionId), bean, orchestrator));
                } catch (ValidationException ex) {
                    boolean handled = notifyValidationFailure(actionDefinitions.get(actionId), ex);
                    if (!handled) {
                        throw new RuntimeException("Validation failed for action " + actionId, ex);
                    }
                }
            });
        }

        public void addValidationFailureListener(ValidationFailureListener<T> listener) {
            validationFailureListeners.add(Objects.requireNonNull(listener, "listener"));
        }

        public void setRepeatableEntryCount(String groupId, int desiredCount) {
            RepeatableGroupState state = repeatableGroups.get(groupId);
            if (state == null) {
                throw new IllegalArgumentException("Unknown repeatable group id " + groupId);
            }
            ensureRepeatableEntryCount(state, desiredCount);
            FormEngine.this.updateAddButtonState(state);
            FormEngine.this.updateRemoveButtons(state);
            FormEngine.this.updateRepeatableTitles(state, fieldContext);
            applyReadOnlyState(currentBean);
        }

        public void duplicateRepeatableEntry(String groupId, int sourceIndex) {
            RepeatableGroupState state = repeatableGroups.get(groupId);
            if (state == null) {
                throw new IllegalArgumentException("Unknown repeatable group id " + groupId);
            }
            if (!state.getRepeatable().isAllowDuplicate()) {
                return;
            }
            if (sourceIndex < 0 || sourceIndex >= state.getEntries().size()) {
                return;
            }
            RepeatableEntry source = state.getEntries().get(sourceIndex);
            if (source == null) {
                return;
            }
            RepeatableEntry duplicated = FormEngine.this.addRepeatableEntry(state, fieldRegistry, fieldContext);
            if (duplicated == null) {
                return;
            }
            copyRepeatableValues(source, duplicated);
            FormEngine.this.updateDuplicateButtonState(state);
            FormEngine.this.refreshDuplicateDialogOptions(state, fieldContext);
        }

        private boolean notifyValidationFailure(ActionDefinition actionDefinition, ValidationException exception) {
            if (validationFailureListeners.isEmpty()) {
                return false;
            }
            validationFailureListeners.forEach(listener -> listener.onValidationFailure(actionDefinition, exception));
            return true;
        }

        private void populateRepeatableState(RepeatableGroupState state, T bean) {
            if (state.getParentPath().isBlank()) {
                return;
            }
            Object rawCollection = orchestrator.readProperty(state.getParentPath(), bean);
            List<Map<String, Object>> values = extractCollectionValues(rawCollection, state);
            ensureRepeatableEntryCount(state, values.size());
            List<RepeatableEntry> entries = state.getEntries();
            for (int index = 0; index < entries.size(); index++) {
                Map<String, Object> valueMap = index < values.size() ? values.get(index) : Collections.emptyMap();
                Map<FieldDefinition, FieldInstance> fieldMap = entries.get(index).fields();
                for (Map.Entry<FieldDefinition, FieldInstance> fieldEntry : fieldMap.entrySet()) {
                    FieldDefinition definition = fieldEntry.getKey();
                    FieldInstance instance = fieldEntry.getValue();
                    String path = definition.getPath();
                    String relativePath = toRelativePath(path, state.getParentPath());
                    if (relativePath.isBlank()) {
                        continue;
                    }
                    Object propertyValue = valueMap.get(relativePath);
                    applyComponentValue(instance, definition, propertyValue);
                }
            }
            FormEngine.this.updateAddButtonState(state);
            FormEngine.this.updateRemoveButtons(state);
            FormEngine.this.updateRepeatableTitles(state, fieldContext);
        }

        private void ensureRepeatableEntryCount(RepeatableGroupState state, int desiredCount) {
            int target = Math.max(state.getRepeatable().getMin(), Math.min(desiredCount, state.getRepeatable().getMax()));
            while (state.getEntries().size() < target) {
                FormEngine.this.addRepeatableEntry(state, fieldRegistry, fieldContext);
            }
            while (state.getEntries().size() > target) {
                RepeatableEntry removed = state.getEntries().remove(state.getEntries().size() - 1);
                state.getContainer().remove(removed.wrapper());
            }
        }

        private void applyReadOnlyState(T bean) {
            T evaluationBean = bean != null ? bean : currentBean;
            StateEvaluationContext beanContext = new BeanEvaluationContext(evaluationBean);
            for (SectionDefinition section : definition.getSections()) {
                boolean sectionReadOnly = evaluateStateExpression(section.getReadOnlyWhen(), beanContext);
                for (GroupDefinition group : section.getGroups()) {
                    boolean groupReadOnly = sectionReadOnly || evaluateStateExpression(group.getReadOnlyWhen(), beanContext);
                    if (isRepeatableGroup(group)) {
                        RepeatableGroupState state = repeatableGroups.get(group.getId());
                        if (state != null) {
                            applyReadOnlyToRepeatableGroup(state, groupReadOnly, evaluationBean, beanContext);
                        }
                    } else {
                        for (FieldDefinition fieldDefinition : group.getFields()) {
                            FieldInstance instance = fields.get(fieldDefinition);
                            if (instance == null) {
                                continue;
                            }
                            boolean fieldReadOnly = groupReadOnly
                                    || evaluateStateExpression(fieldDefinition.getReadOnlyWhen(), beanContext)
                                    || shouldApplyOverride(fieldDefinition, ReadOnlyOverrideContext.forBean(evaluationBean));
                            applyReadOnlyToField(instance, fieldReadOnly);
                        }
                    }
                }
            }
        }

        private void applyReadOnlyToRepeatableGroup(RepeatableGroupState state,
                                                    boolean groupReadOnly,
                                                    T bean,
                                                    StateEvaluationContext beanContext) {
            if (groupReadOnly) {
                disableRepeatableControlsForReadOnly(state);
            } else {
                FormEngine.this.updateAddButtonState(state);
            }
            List<RepeatableEntry> entries = state.getEntries();
            for (int index = 0; index < entries.size(); index++) {
                RepeatableEntry entry = entries.get(index);
                RepeatableEntryEvaluationContext entryContext =
                        new RepeatableEntryEvaluationContext(state, entry, index, beanContext, bean);
                ReadOnlyOverrideContext<T> overrideContext = ReadOnlyOverrideContext.forRepeatableEntry(
                        bean, entryContext.toOverrideContext());
                for (Map.Entry<FieldDefinition, FieldInstance> entryField : entry.fields().entrySet()) {
                    FieldDefinition definition = entryField.getKey();
                    boolean fieldReadOnly = groupReadOnly
                            || evaluateStateExpression(definition.getReadOnlyWhen(), entryContext)
                            || shouldApplyOverride(definition, overrideContext);
                    applyReadOnlyToField(entryField.getValue(), fieldReadOnly);
                }
                if (groupReadOnly) {
                    entry.removeButton().setEnabled(false);
                    FormEngine.this.updateRepeatableRemoveButtonStyles(entry.removeButton(), false);
                }
            }
            if (!groupReadOnly) {
                FormEngine.this.updateRemoveButtons(state);
            }
        }

        private void disableRepeatableControlsForReadOnly(RepeatableGroupState state) {
            state.getAddButton().setEnabled(false);
            state.getAddButton().getElement().setAttribute("aria-disabled", "true");
            state.getEntries().forEach(entry -> {
                entry.removeButton().setEnabled(false);
                FormEngine.this.updateRepeatableRemoveButtonStyles(entry.removeButton(), false);
            });
        }

        private void applyReadOnlyToField(FieldInstance instance, boolean readOnly) {
            HasValue<?, ?> valueComponent = instance.getValueComponent();
            boolean readOnlyApplied = applyReadOnly(valueComponent, readOnly);
            if (!readOnlyApplied) {
                if (valueComponent instanceof Component component) {
                    setEnabledFallback(component, !readOnly);
                }
                setEnabledFallback(instance.getComponent(), !readOnly);
            }
            if (valueComponent instanceof Component component) {
                updateReadOnlyDecoration(component, readOnly);
            }
            updateReadOnlyDecoration(instance.getComponent(), readOnly);
        }

        private boolean shouldApplyOverride(FieldDefinition definition, ReadOnlyOverrideContext<T> context) {
            if (readOnlyOverrides.isEmpty()) {
                return false;
            }
            for (ReadOnlyOverride<T> override : readOnlyOverrides) {
                if (override.test(definition, context)) {
                    return true;
                }
            }
            return false;
        }

        private boolean evaluateStateExpression(String expression, StateEvaluationContext context) {
            if (expression == null || expression.isBlank() || context == null) {
                return false;
            }
            String trimmed = expression.trim();
            if ("true".equalsIgnoreCase(trimmed)) {
                return true;
            }
            if ("false".equalsIgnoreCase(trimmed)) {
                return false;
            }
            Matcher matcher = SIMPLE_COMPARISON.matcher(trimmed);
            if (matcher.matches()) {
                String leftOperand = matcher.group(1).trim();
                String operator = matcher.group(2);
                String rightOperand = matcher.group(3);
                Object leftValue = context.read(leftOperand);
                Object rightValue = parseLiteral(rightOperand);
                return compareValues(leftValue, operator, rightValue);
            }
            Object value = context.read(trimmed);
            return coerceToBoolean(value);
        }

        private Object safeReadFromBean(String path, T bean) {
            if (bean == null || path == null || path.isBlank()) {
                return null;
            }
            try {
                return orchestrator.readProperty(path, bean);
            } catch (RuntimeException ex) {
                return null;
            }
        }

        private Object readEntryProperty(Object target, String propertyPath) {
            if (target == null || propertyPath == null || propertyPath.isBlank()) {
                return null;
            }
            String[] segments = propertyPath.split("\\.");
            Object current = target;
            for (String segment : segments) {
                if (current == null) {
                    return null;
                }
                if (current instanceof Map<?, ?> map) {
                    current = map.get(segment);
                } else {
                    current = RenderedForm.this.readPropertyValue(current, segment);
                }
            }
            return current;
        }

        private String extractPropertyName(String path) {
            if (path == null || path.isBlank()) {
                return "";
            }
            int lastDot = path.lastIndexOf('.');
            return lastDot >= 0 ? path.substring(lastDot + 1) : path;
        }

        private interface StateEvaluationContext {
            Object read(String path);
        }

        private final class BeanEvaluationContext implements StateEvaluationContext {
            private final T bean;

            private BeanEvaluationContext(T bean) {
                this.bean = bean;
            }

            @Override
            public Object read(String path) {
                return safeReadFromBean(path, bean);
            }
        }

        private final class RepeatableEntryEvaluationContext implements StateEvaluationContext {
            private final RepeatableGroupState groupState;
            private final int index;
            private final StateEvaluationContext beanContext;
            private final Map<String, Object> values;
            private final Set<String> entryKeys;
            private final Object backingValue;
            private final ReadOnlyOverrideContext.RepeatableEntryOverrideContext overrideContext;

            private RepeatableEntryEvaluationContext(RepeatableGroupState groupState,
                                                     RepeatableEntry entry,
                                                     int index,
                                                     StateEvaluationContext beanContext,
                                                     T bean) {
                this.groupState = groupState;
                this.index = index;
                this.beanContext = beanContext;
                EntrySnapshot snapshot = captureEntrySnapshot(entry);
                this.values = Collections.unmodifiableMap(snapshot.values());
                this.entryKeys = Collections.unmodifiableSet(snapshot.keys());
                this.backingValue = resolveBackingValue(groupState, bean, index);
                this.overrideContext = new ReadOnlyOverrideContext.RepeatableEntryOverrideContext(
                        groupState.getGroup().getId(), index, this.values, this.backingValue);
            }

            @Override
            public Object read(String path) {
                if (path == null || path.isBlank()) {
                    return null;
                }
                String trimmed = path.trim();
                if (entryKeys.contains(path)) {
                    return values.get(path);
                }
                if (entryKeys.contains(trimmed)) {
                    return values.get(trimmed);
                }
                String parentPath = groupState.getParentPath();
                if (!parentPath.isBlank()) {
                    if (trimmed.equals(parentPath)) {
                        return backingValue;
                    }
                    if (trimmed.startsWith(parentPath + ".")) {
                        String relative = trimmed.substring(parentPath.length() + 1);
                        if (entryKeys.contains(relative)) {
                            return values.get(relative);
                        }
                        return readEntryProperty(backingValue, relative);
                    }
                }
                int lastDot = trimmed.lastIndexOf('.');
                if (lastDot >= 0) {
                    String tail = trimmed.substring(lastDot + 1);
                    if (entryKeys.contains(tail)) {
                        return values.get(tail);
                    }
                }
                Object candidate = readEntryProperty(backingValue, trimmed);
                if (candidate != null) {
                    return candidate;
                }
                return beanContext.read(path);
            }

            private EntrySnapshot captureEntrySnapshot(RepeatableEntry entry) {
                Map<String, Object> snapshotValues = new LinkedHashMap<>();
                Set<String> keys = new LinkedHashSet<>();
                String parentPath = groupState.getParentPath();
                for (Map.Entry<FieldDefinition, FieldInstance> fieldEntry : entry.fields().entrySet()) {
                    FieldDefinition definition = fieldEntry.getKey();
                    FieldInstance instance = fieldEntry.getValue();
                    HasValue<?, ?> component = instance.getValueComponent();
                    if (component == null) {
                        continue;
                    }
                    Object raw = component.getValue();
                    Object converted = orchestrator.convertRawValue(definition, raw);
                    String path = definition.getPath();
                    if (path == null || path.isBlank()) {
                        continue;
                    }
                    snapshotValues.put(path, converted);
                    keys.add(path);
                    String property = extractPropertyName(path);
                    if (!property.isBlank()) {
                        snapshotValues.putIfAbsent(property, converted);
                        keys.add(property);
                    }
                    if (!parentPath.isBlank() && path.startsWith(parentPath + ".")) {
                        String relative = path.substring(parentPath.length() + 1);
                        if (!relative.isBlank()) {
                            snapshotValues.putIfAbsent(relative, converted);
                            keys.add(relative);
                        }
                    }
                }
                return new EntrySnapshot(snapshotValues, keys);
            }

            private Object resolveBackingValue(RepeatableGroupState state, T bean, int index) {
                if (bean == null || state.getParentPath().isBlank()) {
                    return null;
                }
                Object container = safeReadFromBean(state.getParentPath(), bean);
                if (container instanceof List<?> list) {
                    return index >= 0 && index < list.size() ? list.get(index) : null;
                }
                if (container instanceof Collection<?> collection) {
                    if (index < 0 || index >= collection.size()) {
                        return null;
                    }
                    int counter = 0;
                    for (Object element : collection) {
                        if (counter == index) {
                            return element;
                        }
                        counter++;
                    }
                    return null;
                }
                if (container != null && container.getClass().isArray()) {
                    Object[] array = (Object[]) container;
                    return index >= 0 && index < array.length ? array[index] : null;
                }
                return null;
            }

            private ReadOnlyOverrideContext.RepeatableEntryOverrideContext toOverrideContext() {
                return overrideContext;
            }

            private final class EntrySnapshot {
                private final Map<String, Object> values;
                private final Set<String> keys;

                private EntrySnapshot(Map<String, Object> values, Set<String> keys) {
                    this.values = values;
                    this.keys = keys;
                }

                private Map<String, Object> values() {
                    return values;
                }

                private Set<String> keys() {
                    return keys;
                }
            }
        }

        @FunctionalInterface
        public interface ReadOnlyOverride<T> {
            boolean test(FieldDefinition definition, ReadOnlyOverrideContext<T> context);
        }

        public static final class ReadOnlyOverrideContext<T> {
            private final T bean;
            private final RepeatableEntryOverrideContext repeatableEntry;

            private ReadOnlyOverrideContext(T bean, RepeatableEntryOverrideContext repeatableEntry) {
                this.bean = bean;
                this.repeatableEntry = repeatableEntry;
            }

            public T getBean() {
                return bean;
            }

            public Optional<RepeatableEntryOverrideContext> getRepeatableEntry() {
                return Optional.ofNullable(repeatableEntry);
            }

            private static <T> ReadOnlyOverrideContext<T> forBean(T bean) {
                return new ReadOnlyOverrideContext<>(bean, null);
            }

            private static <T> ReadOnlyOverrideContext<T> forRepeatableEntry(T bean,
                                                                             RepeatableEntryOverrideContext repeatable) {
                return new ReadOnlyOverrideContext<>(bean, repeatable);
            }

            public static final class RepeatableEntryOverrideContext {
                private final String groupId;
                private final int index;
                private final Map<String, Object> values;
                private final Object backingValue;

                private RepeatableEntryOverrideContext(String groupId,
                                                       int index,
                                                       Map<String, Object> values,
                                                       Object backingValue) {
                    this.groupId = groupId;
                    this.index = index;
                    this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
                    this.backingValue = backingValue;
                }

                public String getGroupId() {
                    return groupId;
                }

                public int getIndex() {
                    return index;
                }

                public Map<String, Object> getValues() {
                    return values;
                }

                public Object getBackingValue() {
                    return backingValue;
                }
            }
        }

        private Object parseLiteral(String token) {
            if (token == null) {
                return null;
            }
            String trimmed = token.trim();
            if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                    || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
                trimmed = trimmed.substring(1, Math.max(1, trimmed.length() - 1));
            }
            if (trimmed.isEmpty()) {
                return "";
            }
            if ("null".equalsIgnoreCase(trimmed)) {
                return null;
            }
            if ("true".equalsIgnoreCase(trimmed)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(trimmed)) {
                return Boolean.FALSE;
            }
            try {
                if (trimmed.contains(".")) {
                    return Double.parseDouble(trimmed);
                }
                return Long.parseLong(trimmed);
            } catch (NumberFormatException ex) {
                return trimmed;
            }
        }

        private Object normalizeComparisonOperand(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Enum<?> enumValue) {
                return enumValue.name();
            }
            if (value instanceof Number number) {
                return Double.valueOf(number.doubleValue());
            }
            if (value instanceof Boolean bool) {
                return bool;
            }
            return value instanceof String ? value : String.valueOf(value);
        }

        private boolean compareValues(Object left, String operator, Object right) {
            if ("==".equals(operator)) {
                if (left == null || right == null) {
                    return left == null && right == null;
                }
                Object lhs = normalizeComparisonOperand(left);
                Object rhs = normalizeComparisonOperand(right);
                if (lhs instanceof Double ld && rhs instanceof Double rd) {
                    return Double.compare(ld, rd) == 0;
                }
                return Objects.equals(lhs, rhs);
            }
            if ("!=".equals(operator)) {
                return !compareValues(left, "==", right);
            }
            return false;
        }

        private boolean coerceToBoolean(Object value) {
            if (value == null) {
                return false;
            }
            if (value instanceof Boolean bool) {
                return bool;
            }
            if (value instanceof Number number) {
                return Math.abs(number.doubleValue()) > 0.0000001d;
            }
            if (value instanceof String str) {
                String trimmed = str.trim();
                if (trimmed.isEmpty()) {
                    return false;
                }
                return !("false".equalsIgnoreCase(trimmed) || "0".equals(trimmed));
            }
            return true;
        }

        private boolean applyReadOnly(HasValue<?, ?> component, boolean readOnly) {
            if (component == null) {
                return false;
            }
            try {
                component.setReadOnly(readOnly);
                return true;
            } catch (UnsupportedOperationException ex) {
                return false;
            }
        }

        private void setEnabledFallback(Component component, boolean enabled) {
            if (component == null) {
                return;
            }
            if (component instanceof HasEnabled) {
                ((HasEnabled) component).setEnabled(enabled);
            } else if (!enabled) {
                component.getElement().setProperty("disabled", true);
            } else {
                component.getElement().removeProperty("disabled");
            }
            if (enabled) {
                component.getElement().removeAttribute("aria-disabled");
                component.getElement().getThemeList().remove("ytp-disabled");
            } else {
                component.getElement().setAttribute("aria-disabled", "true");
                component.getElement().getThemeList().add("ytp-disabled");
            }
        }

        private void updateReadOnlyDecoration(Component component, boolean readOnly) {
            if (component == null) {
                return;
            }
            if (readOnly) {
                component.getElement().setAttribute("aria-readonly", "true");
                component.getElement().getThemeList().add("ytp-readonly");
            } else {
                component.getElement().removeAttribute("aria-readonly");
                component.getElement().getThemeList().remove("ytp-readonly");
            }
        }

        private List<Map<String, Object>> extractCollectionValues(Object raw, RepeatableGroupState state) {
            if (raw == null) {
                return List.of();
            }
            Collection<?> collection;
            if (raw instanceof Collection<?> coll) {
                collection = coll;
            } else if (raw.getClass().isArray()) {
                collection = Arrays.asList((Object[]) raw);
            } else {
                collection = List.of(raw);
            }
            List<Map<String, Object>> values = new ArrayList<>();
            for (Object element : collection) {
                if (element == null) {
                    continue;
                }
                Map<String, Object> entryValues = new LinkedHashMap<>();
                for (FieldDefinition field : state.getGroup().getFields()) {
                    String path = field.getPath();
                    String property = toRelativePath(path, state.getParentPath());
                    if (property.isBlank()) {
                        continue;
                    }
                    Object propertyValue = readPropertyValue(element, property);
                    entryValues.put(property, propertyValue);
                }
                values.add(entryValues);
            }
            return values;
        }

        private Object readPropertyValue(Object target, String path) {
            if (target == null || path == null || path.isBlank()) {
                return null;
            }
            Object current = target;
            String[] segments = path.split("\\.");
            for (String segment : segments) {
                if (segment == null || segment.isBlank()) {
                    return null;
                }
                current = readSinglePropertyValue(current, segment);
                if (current == null) {
                    return null;
                }
            }
            return current;
        }

        private Object readSinglePropertyValue(Object target, String property) {
            if (target == null || property == null || property.isBlank()) {
                return null;
            }
            if (target instanceof DynamicPropertyBag bag) {
                return bag.readDynamicProperty(property);
            }
            if (target instanceof Map<?, ?> map) {
                return map.get(property);
            }
            try {
                Method getter = target.getClass().getMethod("get" + capitalize(property));
                return getter.invoke(target);
            } catch (NoSuchMethodException ex) {
                try {
                    Method booleanGetter = target.getClass().getMethod("is" + capitalize(property));
                    return booleanGetter.invoke(target);
                } catch (NoSuchMethodException ignored) {
                    return null;
                } catch (ReflectiveOperationException reflectionEx) {
                    throw new IllegalStateException("Unable to access property '" + property + "' on "
                            + target.getClass().getName(), reflectionEx);
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to access property '" + property + "' on "
                        + target.getClass().getName(), e);
            }
        }

        private void applyComponentValue(FieldInstance instance, FieldDefinition definition, Object value) {
            com.vaadin.flow.component.HasValue<?, ?> component = instance.getValueComponent();
            Object presentation = convertForComponent(definition, value);
            setPresentation(component, definition, presentation);
        }

        private Object convertForComponent(FieldDefinition definition, Object value) {
            if (value == null) {
                return null;
            }
            return switch (definition.getComponentType()) {
                case NUMBER -> value instanceof Number number ? number.doubleValue()
                        : Double.valueOf(String.valueOf(value));
                case INTEGER -> value instanceof Number number ? number.intValue()
                        : Integer.valueOf(String.valueOf(value));
                case MONEY -> value instanceof BigDecimal bigDecimal ? bigDecimal
                        : new BigDecimal(String.valueOf(value));
                case CHECKBOX, SWITCH -> value instanceof Boolean bool ? bool : Boolean.valueOf(String.valueOf(value));
                case DATE -> value instanceof LocalDate ? value : null;
                case DATETIME, JALALI_DATE_TIME -> value instanceof LocalDateTime ? value : null;
                case TIME -> value instanceof LocalTime ? value : null;
                case JALALI_DATE -> value instanceof LocalDate ? value : null;
                case MULTI_SELECT, TAGS -> resolveOptionItems(definition, value);
                case SELECT, AUTOCOMPLETE, ENUM, RADIO -> resolveOptionItem(definition, value);
                case MAP -> value instanceof Map<?, ?> map ? Map.copyOf(map) : null;
                default -> value;
            };
        }

        private OptionItem resolveOptionItem(FieldDefinition definition, Object candidate) {
            if (candidate == null) {
                return null;
            }
            if (candidate instanceof OptionItem option) {
                return option;
            }
            String id;
            if (candidate instanceof Enum<?> enumValue) {
                id = enumValue.name();
            } else {
                id = String.valueOf(candidate);
            }
            OptionCatalog catalog = optionCatalogRegistry.resolve(definition, locale);
            return catalog.byIds(List.of(id)).stream()
                    .findFirst()
                    .orElse(new OptionItem(id, id));
        }

        private Set<OptionItem> resolveOptionItems(FieldDefinition definition, Object value) {
            if (value == null) {
                return Collections.emptySet();
            }
            Collection<?> collection;
            if (value instanceof Collection<?> coll) {
                collection = coll;
            } else if (value.getClass().isArray()) {
                collection = Arrays.asList((Object[]) value);
            } else {
                collection = List.of(value);
            }
            Set<OptionItem> resolved = new LinkedHashSet<>();
            for (Object element : collection) {
                OptionItem option = resolveOptionItem(definition, element);
                if (option != null) {
                    resolved.add(option);
                }
            }
            return resolved;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void setPresentation(com.vaadin.flow.component.HasValue component,
                                     FieldDefinition definition,
                                     Object presentation) {
            if (presentation == null) {
                switch (definition.getComponentType()) {
                    case CHECKBOX, SWITCH -> component.setValue(Boolean.FALSE);
                    case MULTI_SELECT, TAGS -> component.setValue(Collections.emptySet());
                    default -> component.clear();
                }
                return;
            }
            component.setValue(presentation);
        }

        private String capitalize(String value) {
            if (value == null || value.isBlank()) {
                return "";
            }
            if (value.length() == 1) {
                return value.toUpperCase();
            }
            return Character.toUpperCase(value.charAt(0)) + value.substring(1);
        }

        private void applyRepeatableValues(T bean) {
            if (repeatableGroups.isEmpty()) {
                return;
            }
            Map<String, List<Map<String, Object>>> aggregated = new LinkedHashMap<>();
            repeatableGroups.values().forEach(state -> {
                if (state == null || state.getEntries().isEmpty()) {
                    return;
                }
                String parentPath = state.getParentPath();
                if (parentPath.isBlank()) {
                    return;
                }
                for (RepeatableEntry entry : state.getEntries()) {
                    Map<FieldDefinition, FieldInstance> fields = entry.fields();
                    if (fields == null || fields.isEmpty()) {
                        continue;
                    }
                    Map<String, Object> valuesByProperty = new LinkedHashMap<>();
                    for (Map.Entry<FieldDefinition, FieldInstance> fieldEntry : fields.entrySet()) {
                        FieldDefinition definition = fieldEntry.getKey();
                        FieldInstance instance = fieldEntry.getValue();
                        String path = definition.getPath();
                        String property = toRelativePath(path, state.getParentPath());
                        if (property.isBlank()) {
                            continue;
                        }
                        Object rawValue = ((com.vaadin.flow.component.HasValue<?, ?>) instance.getValueComponent()).getValue();
                        Object converted = orchestrator.convertRawValue(definition, rawValue);
                        valuesByProperty.put(property, converted);
                    }
                    if (isEmptyValueMap(valuesByProperty)) {
                        continue;
                    }
                    aggregated.computeIfAbsent(parentPath, key -> new ArrayList<>()).add(valuesByProperty);
                }
            });
            aggregated.forEach((path, values) -> {
                try {
                    applyCollectionValues(bean, path, values);
                } catch (IntrospectionException | ReflectiveOperationException ex) {
                    throw new IllegalStateException("Unable to apply repeatable values for path '" + path + "'", ex);
                }
            });
        }

        private boolean isEmptyValueMap(Map<String, Object> values) {
            if (values.isEmpty()) {
                return true;
            }
            for (Object value : values.values()) {
                if (!isEmptyValue(value)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isEmptyValue(Object value) {
            if (value == null) {
                return true;
            }
            if (value instanceof String str) {
                return str.isBlank();
            }
            if (value instanceof Collection<?> collection) {
                return collection.isEmpty();
            }
            if (value instanceof Map<?, ?> map) {
                return map.isEmpty();
            }
            return false;
        }

        private void applyCollectionValues(T bean, String parentPath, List<Map<String, Object>> values)
                throws IntrospectionException, ReflectiveOperationException {
            PathResolution resolution = resolvePath(bean, parentPath);
            PropertyDescriptor descriptor = resolution.descriptor();
            Collection<Object> targetCollection = instantiateCollection(descriptor.getPropertyType());
            if (targetCollection == null) {
                return;
            }
            Class<?> elementType = resolution.elementType();
            for (Map<String, Object> valueMap : values) {
                Object element = createElement(elementType, valueMap);
                if (element != null) {
                    targetCollection.add(element);
                }
            }
            Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod != null) {
                writeMethod.invoke(resolution.owner(), targetCollection);
            } else {
                Method readMethod = descriptor.getReadMethod();
                if (readMethod == null) {
                    return;
                }
                Object existing = readMethod.invoke(resolution.owner());
                if (existing instanceof Collection<?> existingCollection) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> mutable = (Collection<Object>) existingCollection;
                    mutable.clear();
                    mutable.addAll(targetCollection);
                }
            }
        }

        private Collection<Object> instantiateCollection(Class<?> propertyType) {
            if (propertyType == null) {
                return null;
            }
            if (List.class.isAssignableFrom(propertyType)) {
                return new ArrayList<>();
            }
            if (Set.class.isAssignableFrom(propertyType)) {
                return new LinkedHashSet<>();
            }
            if (Collection.class.isAssignableFrom(propertyType)) {
                return new ArrayList<>();
            }
            return null;
        }

        private Object createElement(Class<?> elementType, Map<String, Object> values)
                throws IntrospectionException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
            if (values.isEmpty()) {
                return null;
            }
            if (elementType == null || elementType == Object.class || Map.class.isAssignableFrom(elementType)) {
                return new LinkedHashMap<>(values);
            }
            if (CharSequence.class.isAssignableFrom(elementType) || elementType == String.class) {
                return values.values().stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .findFirst()
                        .orElse("");
            }
            Object element = elementType.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String propertyPath = entry.getKey();
                if (propertyPath == null || propertyPath.isBlank()) {
                    continue;
                }
                writePropertyPath(element, propertyPath, entry.getValue());
            }
            return element;
        }

        private void writePropertyPath(Object target, String path, Object value)
                throws IntrospectionException, InvocationTargetException, InstantiationException, IllegalAccessException,
                NoSuchMethodException {
            if (target == null || path == null || path.isBlank()) {
                return;
            }
            Object current = target;
            String[] segments = path.split("\\.");
            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                if (segment == null || segment.isBlank()) {
                    return;
                }
                boolean last = i == segments.length - 1;
                if (last) {
                    assignPropertyValue(current, segment, value);
                } else {
                    Object next = readSinglePropertyValue(current, segment);
                    if (next == null) {
                        next = ensureIntermediateValue(current, segment);
                    }
                    if (next == null) {
                        return;
                    }
                    current = next;
                }
            }
        }

        private Object ensureIntermediateValue(Object target, String property)
                throws IntrospectionException, InvocationTargetException, InstantiationException, IllegalAccessException,
                NoSuchMethodException {
            if (target instanceof DynamicPropertyBag) {
                return null;
            }
            if (target instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> mutable = (Map<Object, Object>) map;
                Map<String, Object> nested = new LinkedHashMap<>();
                mutable.put(property, nested);
                return nested;
            }
            PropertyDescriptor descriptor = findDescriptor(target.getClass(), property);
            if (descriptor == null) {
                return null;
            }
            Method read = descriptor.getReadMethod();
            if (read != null) {
                Object existing = read.invoke(target);
                if (existing != null) {
                    return existing;
                }
            }
            Class<?> propertyType = descriptor.getPropertyType();
            if (propertyType == null) {
                return null;
            }
            Object instance = instantiateIntermediate(propertyType);
            if (instance == null) {
                return null;
            }
            Method write = descriptor.getWriteMethod();
            if (write != null) {
                write.invoke(target, instance);
                return instance;
            }
            return null;
        }

        private void assignPropertyValue(Object target, String property, Object value)
                throws IntrospectionException, InvocationTargetException, IllegalAccessException {
            if (target == null || property == null || property.isBlank()) {
                return;
            }
            if (target instanceof DynamicPropertyBag bag && bag.writeDynamicProperty(property, value)) {
                return;
            }
            if (target instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> mutable = (Map<Object, Object>) map;
                mutable.put(property, value);
                return;
            }
            PropertyDescriptor descriptor = findDescriptor(target.getClass(), property);
            if (descriptor == null) {
                return;
            }
            Method write = descriptor.getWriteMethod();
            if (write == null) {
                return;
            }
            Object coerced = orchestrator.coerceValueForType(value, write.getParameterTypes()[0]);
            write.invoke(target, coerced);
        }

        private PathResolution resolvePath(Object root, String path)
                throws IntrospectionException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
            String[] segments = path.split("\\.");
            Object current = root;
            Class<?> currentType = root.getClass();
            PropertyDescriptor descriptor = null;
            for (int index = 0; index < segments.length; index++) {
                descriptor = findDescriptor(currentType, segments[index]);
                if (descriptor == null) {
                    throw new IllegalStateException("Unable to resolve property '" + segments[index] + "' on path '" + path + "'");
                }
                if (index == segments.length - 1) {
                    Class<?> elementType = resolveElementType(descriptor);
                    return new PathResolution(current, descriptor, elementType);
                }
                Method read = descriptor.getReadMethod();
                if (read == null) {
                    throw new IllegalStateException("Property '" + segments[index] + "' on path '" + path + "' has no getter");
                }
                Object next = read.invoke(current);
                if (next == null) {
                    next = instantiateIntermediate(descriptor.getPropertyType());
                    Method write = descriptor.getWriteMethod();
                    if (write != null) {
                        write.invoke(current, next);
                    } else {
                        throw new IllegalStateException("Property '" + segments[index] + "' on path '" + path + "' is null and cannot be initialised");
                    }
                }
                current = next;
                currentType = next.getClass();
            }
            throw new IllegalStateException("Unable to resolve path '" + path + "'");
        }

        private Object instantiateIntermediate(Class<?> type)
                throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            if (type == null) {
                return null;
            }
            if (type.isInterface()) {
                if (List.class.isAssignableFrom(type)) {
                    return new ArrayList<>();
                }
                if (Set.class.isAssignableFrom(type)) {
                    return new LinkedHashSet<>();
                }
                if (Map.class.isAssignableFrom(type)) {
                    return new LinkedHashMap<>();
                }
                if (Collection.class.isAssignableFrom(type)) {
                    return new ArrayList<>();
                }
                throw new IllegalStateException("Cannot instantiate interface type " + type.getName());
            }
            return type.getDeclaredConstructor().newInstance();
        }

        private PropertyDescriptor findDescriptor(Class<?> type, String name) throws IntrospectionException {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if (descriptor.getName().equals(name)) {
                    return descriptor;
                }
            }
            return null;
        }

        private Class<?> resolveElementType(PropertyDescriptor descriptor) {
            Class<?> propertyType = descriptor.getPropertyType();
            if (propertyType == null) {
                return null;
            }
            if (propertyType.isArray()) {
                return propertyType.getComponentType();
            }
            if (Collection.class.isAssignableFrom(propertyType)) {
                Class<?> elementType = resolveTypeArgument(descriptor, 0);
                return elementType == null ? Object.class : elementType;
            }
            if (Map.class.isAssignableFrom(propertyType)) {
                Class<?> valueType = resolveTypeArgument(descriptor, 1);
                return valueType == null ? Object.class : valueType;
            }
            return propertyType;
        }

        private Class<?> resolveTypeArgument(PropertyDescriptor descriptor, int index) {
            Method read = descriptor.getReadMethod();
            if (read != null) {
                Class<?> resolved = resolveTypeArgument(read.getGenericReturnType(), index);
                if (resolved != null) {
                    return resolved;
                }
            }
            Method write = descriptor.getWriteMethod();
            if (write != null && write.getGenericParameterTypes().length == 1) {
                Class<?> resolved = resolveTypeArgument(write.getGenericParameterTypes()[0], index);
                if (resolved != null) {
                    return resolved;
                }
            }
            return null;
        }

        private Class<?> resolveTypeArgument(Type type, int index) {
            if (type instanceof ParameterizedType parameterized) {
                Type[] arguments = parameterized.getActualTypeArguments();
                if (index >= 0 && index < arguments.length) {
                    Type argument = arguments[index];
                    if (argument instanceof Class<?> clazz) {
                        return clazz;
                    }
                }
            }
            return null;
        }

        private record PathResolution(Object owner, PropertyDescriptor descriptor, Class<?> elementType) {
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
                                  Map<String, RepeatableGroupState> repeatableGroups,
                                  Class<?> beanType) {
        if (isRepeatableGroup(group)) {
            return createRepeatableGroup(group, registry, context, repeatableGroups, beanType);
        }
        com.vaadin.flow.component.formlayout.FormLayout formLayout = new com.vaadin.flow.component.formlayout.FormLayout();
        configureResponsiveSteps(formLayout, group.getColumns());
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
                                            Map<String, RepeatableGroupState> repeatableGroups,
                                            Class<?> beanType) {
        com.vaadin.flow.component.orderedlayout.VerticalLayout wrapper = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.addClassName("form-engine-repeatable-group");
        RepeatableDefinition repeatable = group.getRepeatableDefinition();
        com.vaadin.flow.component.orderedlayout.VerticalLayout entriesContainer = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        entriesContainer.setPadding(false);
        entriesContainer.setSpacing(true);
        entriesContainer.getElement().setAttribute("data-repeatable-container", group.getId());
        wrapper.add(entriesContainer);
        Button addEntry = new Button(context.translate("form.repeatable.addGroup"));
        addEntry.getElement().setAttribute("aria-label", context.translate("form.repeatable.addGroup"));
        addEntry.getElement().setAttribute("data-repeatable-add", group.getId());
        context.applyTheme(addEntry);
        Button duplicateEntry = null;
        com.vaadin.flow.component.dialog.Dialog duplicateDialog = null;
        com.vaadin.flow.component.combobox.ComboBox<RepeatableEntry> duplicateSelector = null;
        Button confirmDuplicate = null;
        Button cancelDuplicate = null;
        if (repeatable.isAllowDuplicate()) {
            duplicateEntry = new Button(context.translate("form.repeatable.duplicateGroup"));
            duplicateEntry.getElement().setAttribute("aria-label", context.translate("form.repeatable.duplicateGroup"));
            duplicateEntry.getElement().setAttribute("data-repeatable-duplicate", group.getId());
            context.applyTheme(duplicateEntry);
            duplicateDialog = new com.vaadin.flow.component.dialog.Dialog();
            final com.vaadin.flow.component.dialog.Dialog dialog = duplicateDialog;
            context.applyTheme(dialog);
            dialog.setModal(true);
            dialog.setDraggable(false);
            dialog.setResizable(false);
            dialog.setHeaderTitle(context.translate("form.repeatable.duplicateGroup"));
            duplicateSelector = new com.vaadin.flow.component.combobox.ComboBox<>();
            final com.vaadin.flow.component.combobox.ComboBox<RepeatableEntry> selector = duplicateSelector;
            selector.setWidthFull();
            selector.setPlaceholder(context.translate("form.repeatable.chooseDuplicate"));
            selector.setClearButtonVisible(false);
            selector.getElement().setAttribute("data-repeatable-duplicate-selector", group.getId());
            selector.getElement().setAttribute("aria-label", context.translate("form.repeatable.chooseDuplicate"));
            selector.addValueChangeListener(event -> {
                if (event.isFromClient()) {
                    selector.setInvalid(false);
                }
            });
            confirmDuplicate = new Button(context.translate("form.repeatable.confirmDuplicate"));
            final Button confirmButton = confirmDuplicate;
            confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            confirmButton.getElement().setAttribute("data-repeatable-duplicate-confirm", group.getId());
            confirmButton.getElement().setAttribute("aria-label", context.translate("form.repeatable.confirmDuplicate"));
            context.applyTheme(confirmButton);
            cancelDuplicate = new Button(context.translate("form.repeatable.cancelDuplicate"));
            final Button cancelButton = cancelDuplicate;
            cancelButton.getElement().setAttribute("data-repeatable-duplicate-cancel", group.getId());
            cancelButton.getElement().setAttribute("aria-label", context.translate("form.repeatable.cancelDuplicate"));
            context.applyTheme(cancelButton);
            cancelButton.addClickListener(event -> dialog.close());
            com.vaadin.flow.component.orderedlayout.VerticalLayout dialogLayout = new com.vaadin.flow.component.orderedlayout.VerticalLayout(selector);
            dialogLayout.setPadding(false);
            dialogLayout.setSpacing(false);
            dialogLayout.addClassName("stack-m");
            dialog.add(dialogLayout);
            com.vaadin.flow.component.orderedlayout.HorizontalLayout footer = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(cancelButton, confirmButton);
            footer.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
            footer.setWidthFull();
            dialog.getFooter().add(footer);
        }
        Button duplicateButtonRef = duplicateEntry;
        com.vaadin.flow.component.dialog.Dialog duplicateDialogRef = duplicateDialog;
        com.vaadin.flow.component.combobox.ComboBox<RepeatableEntry> duplicateSelectorRef = duplicateSelector;
        Button confirmDuplicateRef = confirmDuplicate;
        Button cancelDuplicateRef = cancelDuplicate;
        RepeatableGroupState state = repeatableGroups.computeIfAbsent(group.getId(), key ->
                new RepeatableGroupState(group, repeatable, entriesContainer, addEntry, duplicateButtonRef, duplicateDialogRef,
                        duplicateSelectorRef, confirmDuplicateRef, cancelDuplicateRef, new ArrayList<>(),
                        deriveParentPath(group, beanType)));
        addEntry.addClickListener(event -> {
            if (!state.getRepeatable().isAllowManualAdd()) {
                return;
            }
            addRepeatableEntry(state, registry, context);
        });
        if (state.getDuplicateButton() != null && state.getDuplicateDialog() != null
                && state.getDuplicateSelector() != null && state.getConfirmDuplicateButton() != null) {
            final RepeatableGroupState currentState = state;
            final FieldRegistry registryRef = registry;
            final FieldFactoryContext contextRef = context;
            currentState.getDuplicateSelector().setItemLabelGenerator(entry ->
                    generateRepeatableTitle(currentState, entry, contextRef));
            currentState.getDuplicateSelector().addValueChangeListener(event ->
                    currentState.getConfirmDuplicateButton().setEnabled(event.getValue() != null));
            currentState.getConfirmDuplicateButton().addClickListener(event -> {
                RepeatableEntry selected = currentState.getDuplicateSelector().getValue();
                if (selected == null) {
                    currentState.getDuplicateSelector().setInvalid(true);
                    currentState.getDuplicateSelector().setErrorMessage(contextRef.translate("form.repeatable.selectDuplicateError"));
                    return;
                }
                RepeatableEntry newEntry = addRepeatableEntry(currentState, registryRef, contextRef);
                if (newEntry != null) {
                    copyRepeatableValues(selected, newEntry);
                    updateDuplicateButtonState(currentState);
                    refreshDuplicateDialogOptions(currentState, contextRef);
                    currentState.getDuplicateDialog().close();
                }
            });
            currentState.getDuplicateButton().addClickListener(event -> {
                refreshDuplicateDialogOptions(currentState, contextRef);
                currentState.getDuplicateSelector().setValue(null);
                currentState.getConfirmDuplicateButton().setEnabled(false);
                if (currentState.getDuplicateDialog().getUI().isPresent()) {
                    currentState.getDuplicateDialog().open();
                }
            });
        }
        int initial = repeatable.getMin();
        for (int i = 0; i < initial; i++) {
            addRepeatableEntry(state, registry, context);
        }
        updateAddButtonState(state);
        updateRemoveButtons(state);
        updateRepeatableTitles(state, context);
        updateDuplicateButtonState(state);
        refreshDuplicateDialogOptions(state, context);
        com.vaadin.flow.component.orderedlayout.HorizontalLayout controls = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
        controls.setSpacing(true);
        controls.setPadding(false);
        controls.getStyle().set("margin-top", "var(--lumo-space-s)");
        controls.add(addEntry);
        if (state.getDuplicateButton() != null) {
            controls.add(state.getDuplicateButton());
        }
        wrapper.add(controls);
        if (state.getDuplicateDialog() != null) {
            wrapper.add(state.getDuplicateDialog());
        }
        return wrapper;
    }

    private boolean isRepeatableGroup(GroupDefinition group) {
        RepeatableDefinition repeatable = group.getRepeatableDefinition();
        return repeatable != null && repeatable.isEnabled();
    }

    private RepeatableEntry addRepeatableEntry(RepeatableGroupState state,
                                               FieldRegistry registry,
                                               FieldFactoryContext context) {
        if (state.getEntries().size() >= state.getRepeatable().getMax()) {
            updateAddButtonState(state);
            updateDuplicateButtonState(state);
            return null;
        }
        com.vaadin.flow.component.orderedlayout.VerticalLayout entryWrapper = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        entryWrapper.setPadding(false);
        entryWrapper.setSpacing(false);
        entryWrapper.getElement().setAttribute("data-repeatable-entry", state.getGroup().getId());
        Button removeButton = new Button(context.translate("form.repeatable.removeGroup"));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        removeButton.getStyle().set("color", "var(--color-danger-500)");
        removeButton.getStyle().set("--lumo-button-color", "var(--color-danger-500)");
        removeButton.getStyle().set("--lumo-error-color", "var(--color-danger-500)");
        removeButton.getElement().setAttribute("data-repeatable-remove", "true");
        removeButton.getElement().setAttribute("aria-label", context.translate("form.repeatable.removeGroup"));
        context.applyTheme(removeButton);
        removeButton.setEnabled(state.getRepeatable().isAllowManualRemove());
        updateRepeatableRemoveButtonStyles(removeButton, removeButton.isEnabled());
        com.vaadin.flow.component.orderedlayout.HorizontalLayout header = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        header.getStyle().set("gap", "var(--lumo-space-s)");
        header.getElement().setAttribute("data-repeatable-header", "true");
        com.vaadin.flow.component.html.Span title = new com.vaadin.flow.component.html.Span();
        title.getElement().setAttribute("data-repeatable-title", "true");
        title.addClassName("form-engine-repeatable-title");
        title.setText(state.getRepeatable().getTitleGenerator()
                .generate(state.getEntries().size(), context, state.getGroup(), state.getRepeatable()));
        header.add(title, removeButton);
        com.vaadin.flow.component.formlayout.FormLayout formLayout = new com.vaadin.flow.component.formlayout.FormLayout();
        configureResponsiveSteps(formLayout, state.getGroup().getColumns());
        Map<FieldDefinition, FieldInstance> entryInstances = new LinkedHashMap<>();
        for (FieldDefinition fieldDefinition : state.getGroup().getFields()) {
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
        RepeatableEntry entry = new RepeatableEntry(entryWrapper, entryInstances, removeButton);
        removeButton.addClickListener(event -> {
            if (!state.getRepeatable().isAllowManualRemove()) {
                return;
            }
            if (state.getEntries().size() <= state.getRepeatable().getMin()) {
                return;
            }
            state.getContainer().remove(entryWrapper);
            state.getEntries().remove(entry);
            updateAddButtonState(state);
            updateRemoveButtons(state);
            updateRepeatableTitles(state, context);
            updateDuplicateButtonState(state);
            refreshDuplicateDialogOptions(state, context);
        });
        entryWrapper.add(header, formLayout);
        state.getContainer().add(entryWrapper);
        state.getEntries().add(entry);
        updateAddButtonState(state);
        updateRemoveButtons(state);
        updateRepeatableTitles(state, context);
        updateDuplicateButtonState(state);
        refreshDuplicateDialogOptions(state, context);
        return entry;
    }

    private void configureResponsiveSteps(com.vaadin.flow.component.formlayout.FormLayout formLayout, int configuredColumns) {
        int columns = Math.max(1, configuredColumns);
        java.util.List<com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep> steps = new java.util.ArrayList<>();
        steps.add(new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 1));
        if (columns > 1) {
            steps.add(new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("36em", Math.min(columns, 2)));
            if (columns > 2) {
                steps.add(new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("64em", columns));
            }
        }
        formLayout.setResponsiveSteps(steps.toArray(new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep[0]));
    }

    private void updateAddButtonState(RepeatableGroupState state) {
        boolean manualAdd = state.getRepeatable().isAllowManualAdd();
        boolean enabled = manualAdd && state.getEntries().size() < state.getRepeatable().getMax();
        state.getAddButton().setEnabled(enabled);
        state.getAddButton().getElement().setAttribute("aria-disabled", String.valueOf(!enabled));
    }

    private void updateRemoveButtons(RepeatableGroupState state) {
        boolean manualRemove = state.getRepeatable().isAllowManualRemove();
        boolean canRemove = manualRemove && state.getEntries().size() > state.getRepeatable().getMin();
        state.getEntries().forEach(entry -> {
            Button button = entry.removeButton();
            button.setEnabled(canRemove);
            updateRepeatableRemoveButtonStyles(button, canRemove);
        });
    }

    private void updateRepeatableTitles(RepeatableGroupState state, FieldFactoryContext context) {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        state.getEntries().forEach(entry -> {
            int index = counter.getAndIncrement();
            String title = state.getRepeatable().getTitleGenerator()
                    .generate(index, context, state.getGroup(), state.getRepeatable());
            entry.wrapper().getChildren()
                    .filter(child -> child instanceof com.vaadin.flow.component.orderedlayout.HorizontalLayout
                            && "true".equals(child.getElement().getAttribute("data-repeatable-header")))
                    .findFirst()
                    .ifPresent(header -> header.getChildren()
                            .filter(hasTextComponent -> hasTextComponent instanceof com.vaadin.flow.component.HasText)
                            .filter(hasTextComponent -> "true".equals(hasTextComponent.getElement()
                                    .getAttribute("data-repeatable-title")))
                            .map(hasTextComponent -> (com.vaadin.flow.component.HasText) hasTextComponent)
                            .forEach(hasText -> hasText.setText(title)));
        });
    }

    private String generateRepeatableTitle(RepeatableGroupState state,
                                           RepeatableEntry entry,
                                           FieldFactoryContext context) {
        if (entry == null) {
            return "";
        }
        int index = state.getEntries().indexOf(entry);
        if (index < 0) {
            return "";
        }
        return state.getRepeatable().getTitleGenerator()
                .generate(index, context, state.getGroup(), state.getRepeatable());
    }

    private void updateDuplicateButtonState(RepeatableGroupState state) {
        if (state.getDuplicateButton() == null) {
            return;
        }
        boolean enabled = state.getRepeatable().isAllowDuplicate()
                && !state.getEntries().isEmpty()
                && state.getEntries().size() < state.getRepeatable().getMax();
        state.getDuplicateButton().setEnabled(enabled);
        state.getDuplicateButton().getElement().setAttribute("aria-disabled", String.valueOf(!enabled));
    }

    private void refreshDuplicateDialogOptions(RepeatableGroupState state, FieldFactoryContext context) {
        if (state.getDuplicateSelector() == null) {
            return;
        }
        List<RepeatableEntry> entries = new ArrayList<>(state.getEntries());
        state.getDuplicateSelector().setItems(entries);
        state.getDuplicateSelector().setItemLabelGenerator(entry -> generateRepeatableTitle(state, entry, context));
        state.getDuplicateSelector().setInvalid(false);
        state.getDuplicateSelector().setErrorMessage("");
        if (state.getConfirmDuplicateButton() != null) {
            state.getConfirmDuplicateButton().setEnabled(false);
        }
    }

    private void copyRepeatableValues(RepeatableEntry source, RepeatableEntry target) {
        if (source == null || target == null) {
            return;
        }
        source.fields().forEach((definition, instance) -> {
            FieldInstance targetInstance = target.fields().get(definition);
            if (targetInstance == null) {
                return;
            }
            Object value = ((HasValue<?, ?>) instance.getValueComponent()).getValue();
            Object duplicate = duplicateValue(value);
            @SuppressWarnings({"rawtypes", "unchecked"})
            HasValue targetValueComponent = (HasValue) targetInstance.getValueComponent();
            targetValueComponent.setValue(duplicate);
        });
    }

    private Object duplicateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        if (value instanceof Set<?> set) {
            return new LinkedHashSet<>(set);
        }
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>(map);
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return value;
        }
        if (value instanceof Cloneable cloneable) {
            try {
                return value.getClass().getMethod("clone").invoke(cloneable);
            } catch (ReflectiveOperationException ex) {
                return value;
            }
        }
        return value;
    }

    private String deriveParentPath(GroupDefinition group, Class<?> beanType) {
        if (group == null || group.getFields().isEmpty()) {
            return "";
        }
        String samplePath = group.getFields().get(0).getPath();
        if (samplePath == null || samplePath.isBlank()) {
            return "";
        }
        if (beanType == null) {
            return extractParentPath(samplePath);
        }
        String[] segments = samplePath.split("\\.");
        StringBuilder traversed = new StringBuilder();
        String collectionPath = "";
        Class<?> currentType = beanType;
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                break;
            }
            if (traversed.length() > 0) {
                traversed.append('.');
            }
            traversed.append(segment);
            PropertyDescriptor descriptor;
            try {
                descriptor = findDescriptor(currentType, segment);
            } catch (IntrospectionException ex) {
                collectionPath = "";
                break;
            }
            if (descriptor == null) {
                break;
            }
            Class<?> propertyType = descriptor.getPropertyType();
            if (propertyType == null) {
                break;
            }
            if (propertyType.isArray()) {
                collectionPath = traversed.toString();
                currentType = propertyType.getComponentType();
                continue;
            }
            if (Collection.class.isAssignableFrom(propertyType)) {
                collectionPath = traversed.toString();
                Class<?> elementType = resolveTypeArgument(descriptor, 0);
                currentType = elementType != null ? elementType : Object.class;
                continue;
            }
            currentType = propertyType;
        }
        if (!collectionPath.isBlank()) {
            return collectionPath;
        }
        return extractParentPath(samplePath);
    }

    private String extractParentPath(String path) {
        if (path == null) {
            return "";
        }
        int lastDot = path.lastIndexOf('.');
        if (lastDot <= 0) {
            return "";
        }
        return path.substring(0, lastDot);
    }

    private String toRelativePath(String path, String parentPath) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (parentPath == null || parentPath.isBlank()) {
            int lastDot = path.lastIndexOf('.');
            return lastDot >= 0 ? path.substring(lastDot + 1) : path;
        }
        if (path.equals(parentPath)) {
            return "";
        }
        String prefix = parentPath + ".";
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }

    private PropertyDescriptor findDescriptor(Class<?> type, String name) throws IntrospectionException {
        for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
            if (descriptor.getName().equals(name)) {
                return descriptor;
            }
        }
        return null;
    }

    private Class<?> resolveTypeArgument(PropertyDescriptor descriptor, int index) {
        Method read = descriptor.getReadMethod();
        if (read != null) {
            Class<?> resolved = resolveTypeArgument(read.getGenericReturnType(), index);
            if (resolved != null) {
                return resolved;
            }
        }
        Method write = descriptor.getWriteMethod();
        if (write != null && write.getGenericParameterTypes().length == 1) {
            Class<?> resolved = resolveTypeArgument(write.getGenericParameterTypes()[0], index);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    private Class<?> resolveTypeArgument(Type type, int index) {
        if (type instanceof ParameterizedType parameterized) {
            Type[] arguments = parameterized.getActualTypeArguments();
            if (index >= 0 && index < arguments.length) {
                Type argument = arguments[index];
                if (argument instanceof Class<?> clazz) {
                    return clazz;
                }
            }
        }
        return null;
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

    private static final class RepeatableGroupState {
        private final GroupDefinition group;
        private final RepeatableDefinition repeatable;
        private final com.vaadin.flow.component.orderedlayout.VerticalLayout container;
        private final Button addButton;
        private final Button duplicateButton;
        private final com.vaadin.flow.component.dialog.Dialog duplicateDialog;
        private final com.vaadin.flow.component.combobox.ComboBox<RepeatableEntry> duplicateSelector;
        private final Button confirmDuplicateButton;
        private final Button cancelDuplicateButton;
        private final List<RepeatableEntry> entries;
        private final String parentPath;

        private RepeatableGroupState(GroupDefinition group,
                                     RepeatableDefinition repeatable,
                                     com.vaadin.flow.component.orderedlayout.VerticalLayout container,
                                     Button addButton,
                                     Button duplicateButton,
                                     com.vaadin.flow.component.dialog.Dialog duplicateDialog,
                                     com.vaadin.flow.component.combobox.ComboBox<RepeatableEntry> duplicateSelector,
                                     Button confirmDuplicateButton,
                                     Button cancelDuplicateButton,
                                     List<RepeatableEntry> entries,
                                     String parentPath) {
            this.group = group;
            this.repeatable = repeatable;
            this.container = container;
            this.addButton = addButton;
            this.duplicateButton = duplicateButton;
            this.duplicateDialog = duplicateDialog;
            this.duplicateSelector = duplicateSelector;
            this.confirmDuplicateButton = confirmDuplicateButton;
            this.cancelDuplicateButton = cancelDuplicateButton;
            this.entries = entries;
            this.parentPath = parentPath == null ? "" : parentPath;
        }

        private GroupDefinition getGroup() {
            return group;
        }

        private RepeatableDefinition getRepeatable() {
            return repeatable;
        }

        private com.vaadin.flow.component.orderedlayout.VerticalLayout getContainer() {
            return container;
        }

        private Button getAddButton() {
            return addButton;
        }

        private Button getDuplicateButton() {
            return duplicateButton;
        }

        private com.vaadin.flow.component.dialog.Dialog getDuplicateDialog() {
            return duplicateDialog;
        }

        private com.vaadin.flow.component.combobox.ComboBox<RepeatableEntry> getDuplicateSelector() {
            return duplicateSelector;
        }

        private Button getConfirmDuplicateButton() {
            return confirmDuplicateButton;
        }

        private Button getCancelDuplicateButton() {
            return cancelDuplicateButton;
        }

        private List<RepeatableEntry> getEntries() {
            return entries;
        }

        private String getParentPath() {
            return parentPath;
        }
    }

    private void updateRepeatableRemoveButtonStyles(Button button, boolean enabled) {
        if (enabled) {
            button.getStyle().set("color", "var(--color-danger-500)");
            button.getStyle().set("--lumo-button-color", "var(--color-danger-500)");
            button.getStyle().set("--lumo-error-color", "var(--color-danger-500)");
            button.getStyle().remove("opacity");
        } else {
            button.getStyle().set("color", "var(--lumo-disabled-text-color)");
            button.getStyle().set("--lumo-button-color", "var(--lumo-disabled-text-color)");
            button.getStyle().set("--lumo-error-color", "var(--lumo-disabled-text-color)");
            button.getStyle().set("opacity", "0.6");
        }
        button.getElement().setAttribute("aria-disabled", String.valueOf(!enabled));
    }

    private static final class RepeatableEntry {
        private final com.vaadin.flow.component.orderedlayout.VerticalLayout wrapper;
        private final Map<FieldDefinition, FieldInstance> fields;
        private final Button removeButton;

        private RepeatableEntry(com.vaadin.flow.component.orderedlayout.VerticalLayout wrapper,
                                Map<FieldDefinition, FieldInstance> fields,
                                Button removeButton) {
            this.wrapper = wrapper;
            this.fields = fields;
            this.removeButton = removeButton;
        }

        private com.vaadin.flow.component.orderedlayout.VerticalLayout wrapper() {
            return wrapper;
        }

        private Map<FieldDefinition, FieldInstance> fields() {
            return fields;
        }

        private Button removeButton() {
            return removeButton;
        }
    }

    public interface ActionHandler<T> {
        void onAction(ActionExecutionContext<T> context) throws com.vaadin.flow.data.binder.ValidationException;
    }

    @FunctionalInterface
    public interface ValidationFailureListener<T> {
        void onValidationFailure(ActionDefinition actionDefinition, ValidationException exception);
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
