package com.youtopin.vaadin.formengine;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
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
        BinderOrchestrator<T> orchestrator = new BinderOrchestrator<>(beanType,
                key -> key == null || key.isBlank() ? "" : provider.getTranslation(key, locale));
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
                Component renderedGroup = renderGroup(group, registry, context, instances, repeatableGroups);
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
                repeatableGroups, sectionLayouts);
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
        private final Map<SectionDefinition, VerticalLayout> sections;
        private java.util.function.Supplier<T> actionBeanSupplier;
        private final List<ValidationFailureListener<T>> validationFailureListeners = new CopyOnWriteArrayList<>();

        private RenderedForm(FormDefinition definition,
                             BinderOrchestrator<T> orchestrator,
                             VerticalLayout layout,
                             Map<FieldDefinition, FieldInstance> fields,
                             com.vaadin.flow.component.orderedlayout.HorizontalLayout headerActions,
                             com.vaadin.flow.component.orderedlayout.HorizontalLayout footerActions,
                             Map<String, Button> actionButtons,
                             Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatableGroups,
                             Map<SectionDefinition, VerticalLayout> sections) {
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

        public Map<SectionDefinition, VerticalLayout> getSections() {
            return sections.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
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

        private boolean notifyValidationFailure(ActionDefinition actionDefinition, ValidationException exception) {
            if (validationFailureListeners.isEmpty()) {
                return false;
            }
            validationFailureListeners.forEach(listener -> listener.onValidationFailure(actionDefinition, exception));
            return true;
        }

        private void applyRepeatableValues(T bean) {
            if (repeatableGroups.isEmpty()) {
                return;
            }
            Map<String, List<Map<String, Object>>> aggregated = new LinkedHashMap<>();
            repeatableGroups.values().forEach(entries -> {
                if (entries == null) {
                    return;
                }
                for (Map<FieldDefinition, FieldInstance> entry : entries) {
                    if (entry == null || entry.isEmpty()) {
                        continue;
                    }
                    Map<String, Object> valuesByProperty = new LinkedHashMap<>();
                    String parentPath = null;
                    for (Map.Entry<FieldDefinition, FieldInstance> fieldEntry : entry.entrySet()) {
                        FieldDefinition definition = fieldEntry.getKey();
                        FieldInstance instance = fieldEntry.getValue();
                        String path = definition.getPath();
                        int lastDot = path.lastIndexOf('.');
                        if (lastDot <= 0) {
                            continue;
                        }
                        String entryParentPath = path.substring(0, lastDot);
                        if (parentPath == null) {
                            parentPath = entryParentPath;
                        } else if (!parentPath.equals(entryParentPath)) {
                            continue;
                        }
                        String property = path.substring(lastDot + 1);
                        Object rawValue = ((com.vaadin.flow.component.HasValue<?, ?>) instance.getValueComponent()).getValue();
                        Object converted = orchestrator.convertRawValue(definition, rawValue);
                        valuesByProperty.put(property, converted);
                    }
                    if (parentPath == null || isEmptyValueMap(valuesByProperty)) {
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
                PropertyDescriptor descriptor = findDescriptor(elementType, entry.getKey());
                if (descriptor == null) {
                    continue;
                }
                Method write = descriptor.getWriteMethod();
                if (write == null) {
                    continue;
                }
                Object coerced = orchestrator.coerceValueForType(entry.getValue(), write.getParameterTypes()[0]);
                write.invoke(element, coerced);
            }
            return element;
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
        entriesContainer.getElement().setAttribute("data-repeatable-container", group.getId());
        wrapper.add(entriesContainer);
        Button addEntry = new Button(context.translate("form.repeatable.addGroup"));
        addEntry.getElement().setAttribute("aria-label", context.translate("form.repeatable.addGroup"));
        addEntry.getElement().setAttribute("data-repeatable-add", group.getId());
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
        updateRepeatableTitles(entriesContainer, repeatable, context, group);
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
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        removeButton.getStyle().set("color", "var(--color-danger-500)");
        removeButton.getStyle().set("--lumo-button-color", "var(--color-danger-500)");
        removeButton.getStyle().set("--lumo-error-color", "var(--color-danger-500)");
        removeButton.getElement().setAttribute("data-repeatable-remove", "true");
        removeButton.getElement().setAttribute("aria-label", context.translate("form.repeatable.removeGroup"));
        context.applyTheme(removeButton);
        com.vaadin.flow.component.orderedlayout.HorizontalLayout header = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        header.getStyle().set("gap", "var(--lumo-space-s)");
        header.getElement().setAttribute("data-repeatable-header", "true");
        com.vaadin.flow.component.html.Span title = new com.vaadin.flow.component.html.Span();
        title.getElement().setAttribute("data-repeatable-title", "true");
        title.addClassName("form-engine-repeatable-title");
        title.setText(repeatable.getTitleGenerator().generate(entries.size(), context, group, repeatable));
        header.add(title, removeButton);
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
            updateRepeatableTitles(entriesContainer, repeatable, context, group);
        });
        entryWrapper.add(header, formLayout);
        entriesContainer.add(entryWrapper);
        entries.add(entryInstances);
        updateAddButtonState(addEntryButton, repeatable, entries);
        updateRemoveButtons(entriesContainer, repeatable, entries.size());
        updateRepeatableTitles(entriesContainer, repeatable, context, group);
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

    private void updateRepeatableTitles(com.vaadin.flow.component.orderedlayout.VerticalLayout entriesContainer,
                                        RepeatableDefinition repeatable,
                                        FieldFactoryContext context,
                                        GroupDefinition group) {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        entriesContainer.getChildren()
                .filter(component -> component instanceof com.vaadin.flow.component.orderedlayout.VerticalLayout)
                .map(component -> (com.vaadin.flow.component.orderedlayout.VerticalLayout) component)
                .forEach(wrapper -> {
                    int index = counter.getAndIncrement();
                    String title = repeatable.getTitleGenerator().generate(index, context, group, repeatable);
                    wrapper.getChildren()
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
