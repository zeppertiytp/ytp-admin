package com.youtopin.vaadin.formengine.scanner;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.youtopin.vaadin.formengine.RepeatableTitleGenerator;
import com.youtopin.vaadin.formengine.binder.DynamicPropertyBag;
import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiCrossField;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.annotation.UiSecurity;
import com.youtopin.vaadin.formengine.annotation.UiSubform;
import com.youtopin.vaadin.formengine.annotation.UiValidation;
import com.youtopin.vaadin.formengine.definition.ActionDefinition;
import com.youtopin.vaadin.formengine.definition.CrossFieldValidationDefinition;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;
import com.youtopin.vaadin.formengine.definition.GroupDefinition;
import com.youtopin.vaadin.formengine.definition.LifecycleHookDefinition;
import com.youtopin.vaadin.formengine.definition.OptionsDefinition;
import com.youtopin.vaadin.formengine.definition.RepeatableDefinition;
import com.youtopin.vaadin.formengine.definition.SectionDefinition;
import com.youtopin.vaadin.formengine.definition.SecurityDefinition;
import com.youtopin.vaadin.formengine.definition.SubformDefinition;
import com.youtopin.vaadin.formengine.definition.ValidationDefinition;

/**
 * Converts annotated classes into {@link FormDefinition} instances.
 */
public final class AnnotationFormScanner {

    public FormDefinition scan(Class<?> formClass) {
        Objects.requireNonNull(formClass, "formClass");
        UiForm uiForm = formClass.getAnnotation(UiForm.class);
        if (uiForm == null) {
            throw new FormDefinitionException("Class " + formClass.getName() + " is not annotated with @UiForm");
        }
        List<SectionDefinition> sections = new ArrayList<>();
        Set<String> fieldPaths = new HashSet<>();
        for (Class<?> sectionClass : uiForm.sections()) {
            UiSection section = sectionClass.getAnnotation(UiSection.class);
            if (section == null) {
                throw new FormDefinitionException("Section class " + sectionClass.getName() + " is missing @UiSection");
            }
            List<GroupDefinition> groups = new ArrayList<>();
            for (Class<?> groupClass : section.groups()) {
                groups.add(scanGroup(groupClass, uiForm.bean(), fieldPaths, false));
            }
            sections.add(new SectionDefinition(section.id(), section.titleKey(), section.descriptionKey(),
                    section.visibleWhen(), section.readOnlyWhen(), section.securityGuard(), section.order(), groups));
        }
        List<ActionDefinition> actions = Arrays.stream(uiForm.actions())
                .map(action -> new ActionDefinition(action.id(), action.labelKey(), action.descriptionKey(),
                        action.visibleWhen(), action.enabledWhen(), action.placement(), action.sectionId(), action.type(),
                        action.order(), toSecurity(action.security())))
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .toList();
        Map<String, LifecycleHookDefinition> lifecycleHooks = Arrays.stream(uiForm.lifecycleHooks())
                .collect(Collectors.toUnmodifiableMap(hook -> hook, hook -> new LifecycleHookDefinition(hook, "", "")));
        return new FormDefinition(uiForm.id(), uiForm.titleKey(), uiForm.descriptionKey(), uiForm.bean(), sections, actions,
                lifecycleHooks);
    }

    private GroupDefinition scanGroup(Class<?> groupClass,
                                      Class<?> beanType,
                                      Set<String> fieldPaths,
                                      boolean entryGroup) {
        UiGroup group = groupClass.getAnnotation(UiGroup.class);
        if (group == null) {
            throw new FormDefinitionException("Group class " + groupClass.getName() + " is missing @UiGroup");
        }
        if (entryGroup && group.repeatable().enabled()) {
            throw new FormDefinitionException("Entry group class " + groupClass.getName()
                    + " cannot enable repeatable behaviour");
        }
        List<FieldDefinition> fields = scanFields(groupClass, beanType, fieldPaths);
        List<GroupDefinition> entryGroups = new ArrayList<>();
        for (Class<?> entryGroupClass : group.entryGroups()) {
            entryGroups.add(scanGroup(entryGroupClass, beanType, fieldPaths, true));
        }
        RepeatableDefinition repeatable = toRepeatable(group.repeatable());
        SubformDefinition subform = toSubform(group.subform());
        return new GroupDefinition(group.id(), group.titleKey(), group.columns(), repeatable, subform,
                group.readOnlyWhen(), fields, entryGroups);
    }

    private List<FieldDefinition> scanFields(Class<?> groupClass, Class<?> beanType, Set<String> fieldPaths) {
        List<FieldDefinition> fields = new ArrayList<>();
        for (Field field : groupClass.getDeclaredFields()) {
            UiField uiField = field.getAnnotation(UiField.class);
            if (uiField != null) {
                fields.add(buildFieldDefinition(uiField, beanType, fieldPaths));
            }
        }
        for (Method method : groupClass.getDeclaredMethods()) {
            UiField uiField = method.getAnnotation(UiField.class);
            if (uiField != null) {
                fields.add(buildFieldDefinition(uiField, beanType, fieldPaths));
            }
        }
        fields.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        return fields;
    }

    private FieldDefinition buildFieldDefinition(UiField uiField, Class<?> beanType, Set<String> fieldPaths) {
        validatePath(beanType, uiField.path());
        if (!fieldPaths.add(uiField.path())) {
            throw new FormDefinitionException("Duplicate field path detected: " + uiField.path());
        }
        OptionsDefinition optionsDefinition = toOptions(uiField.options());
        List<ValidationDefinition> validations = Arrays.stream(uiField.validations())
                .map(this::toValidation)
                .toList();
        List<CrossFieldValidationDefinition> crossFieldValidations = Arrays.stream(uiField.crossField())
                .map(this::toCrossField)
                .toList();
        SecurityDefinition securityDefinition = toSecurity(uiField.security());
        return new FieldDefinition(uiField.path(), uiField.component(), uiField.labelKey(), uiField.helperKey(),
                uiField.placeholderKey(), uiField.requiredWhen(), uiField.requiredMessageKey(), uiField.visibleWhen(),
                uiField.enabledWhen(), uiField.readOnlyWhen(), uiField.defaultValue(), optionsDefinition, validations,
                crossFieldValidations, securityDefinition, uiField.order(), uiField.colSpan(), uiField.rowSpan());
    }

    private RepeatableDefinition toRepeatable(UiRepeatable repeatable) {
        RepeatableTitleGenerator generator = instantiateRepeatableTitleGenerator(repeatable.titleGenerator());
        return new RepeatableDefinition(repeatable.enabled(), repeatable.mode(), repeatable.min(), repeatable.max(),
                repeatable.uniqueBy(), repeatable.summaryTemplate(), repeatable.itemTitleKey(),
                repeatable.itemTitleOffset(), generator, repeatable.allowReorder(), repeatable.allowDuplicate(),
                repeatable.allowManualAdd(), repeatable.allowManualRemove());
    }

    private RepeatableTitleGenerator instantiateRepeatableTitleGenerator(
            Class<? extends RepeatableTitleGenerator> generatorType) {
        try {
            return generatorType.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new FormDefinitionException(
                    "Unable to instantiate repeatable title generator '" + generatorType.getName() + "'", ex);
        }
    }

    private SubformDefinition toSubform(UiSubform subform) {
        return new SubformDefinition(subform.enabled(), subform.formId(), subform.mode(), subform.autoOpen());
    }

    private OptionsDefinition toOptions(UiOptions options) {
        return new OptionsDefinition(options.enabled(), options.type(), List.of(options.entries()), options.enumType(),
                options.callbackRef(), options.remoteRef(), options.cascadeFrom(), options.allowCreate(),
                options.allowCreateFormId(), options.clientFilter());
    }

    private ValidationDefinition toValidation(UiValidation validation) {
        return new ValidationDefinition(validation.messageKey(), validation.expression(),
                List.of(validation.groups()), validation.asyncValidatorBean(), validation.validatorBean());
    }

    private CrossFieldValidationDefinition toCrossField(UiCrossField crossField) {
        return new CrossFieldValidationDefinition(crossField.expression(), crossField.messageKey(),
                List.of(crossField.groups()), List.of(crossField.targetPaths()));
    }

    private SecurityDefinition toSecurity(UiSecurity security) {
        if (security == null) {
            return new SecurityDefinition("", "", List.of(), false);
        }
        return new SecurityDefinition(security.guardId(), security.expression(), List.of(security.requiredAuthorities()),
                security.showWhenDenied());
    }

    private void validatePath(Class<?> beanType, String path) {
        try {
            String[] segments = path.split("\\.");
            Class<?> currentType = beanType;
            for (int index = 0; index < segments.length; index++) {
                String segment = segments[index];
                if (segment.isBlank()) {
                    throw new FormDefinitionException("Invalid property path '" + path + "'");
                }
                PropertyDescriptor descriptor = findDescriptor(currentType, segment);
                if (descriptor == null) {
                    if (DynamicPropertyBag.class.isAssignableFrom(currentType)) {
                        return;
                    }
                    throw new FormDefinitionException("Unable to resolve property '" + segment + "' on "
                            + currentType.getName() + " for path '" + path + "'");
                }
                currentType = resolvePropertyType(descriptor);
                if (currentType == null) {
                    throw new FormDefinitionException("Property '" + segment + "' has no accessible type on path '" + path
                            + "'");
                }
                if (currentType == Object.class && index < segments.length - 1) {
                    // Generic type information is erased; stop validation to avoid false positives.
                    return;
                }
            }
        } catch (IntrospectionException e) {
            throw new FormDefinitionException("Failed to introspect bean type " + beanType.getName(), e);
        }
    }

    private PropertyDescriptor findDescriptor(Class<?> type, String name) throws IntrospectionException {
        for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
            if (descriptor.getName().equals(name)) {
                return descriptor;
            }
        }
        return null;
    }

    private Class<?> resolvePropertyType(PropertyDescriptor descriptor) {
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
        Method readMethod = descriptor.getReadMethod();
        if (readMethod != null) {
            Class<?> resolved = resolveTypeArgument(readMethod.getGenericReturnType(), index);
            if (resolved != null) {
                return resolved;
            }
        }
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod != null && writeMethod.getGenericParameterTypes().length == 1) {
            Class<?> resolved = resolveTypeArgument(writeMethod.getGenericParameterTypes()[0], index);
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
}
