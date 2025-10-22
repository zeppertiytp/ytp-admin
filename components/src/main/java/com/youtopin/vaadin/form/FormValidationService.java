package com.youtopin.vaadin.form;

import java.util.Map;

/**
 * Service contract for validating dynamic forms generated from a JSON
 * specification. Implementations may perform arbitrary validation
 * against a form identifier and submitted field values. The return
 * value should contain field‑specific error messages keyed by the
 * field name. An empty map indicates that validation succeeded.
 */
public interface FormValidationService {

    /**
     * Validate the submitted data for the given form. Implementations
     * should examine the provided field values and return a map of
     * errors keyed by field name. If there are no errors the map
     * should be empty.
     *
     * @param formId a unique identifier for the form specification
     * @param fields a map of submitted values keyed by the field name
     * @return a map of field names to error messages; empty if valid
     */
    Map<String, String> validate(String formId, Map<String, Object> fields);

    /**
     * Validate the submitted data for a specific action. By default this
     * delegates to {@link #validate(String, Map)}, preserving backward
     * compatibility with existing implementations. Override this method
     * when action-specific validation rules are required.
     *
     * @param formId a unique identifier for the form specification
     * @param fields a map of submitted values keyed by the field name
     * @param actionId the identifier of the action that triggered submission
     * @return a map of field names to error messages; empty if valid
     */
    default Map<String, String> validate(String formId, Map<String, Object> fields, String actionId) {
        return validate(formId, fields);
    }
}