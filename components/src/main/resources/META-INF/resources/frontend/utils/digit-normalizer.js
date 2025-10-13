
// Normalize Persian/Arabic-Indic digits to Latin for values emitted by Vaadin fields.
// Visual display remains as-is; we normalize the value before it reaches server/binder.
(function(){
  const map = {
    '۰':'0','۱':'1','۲':'2','۳':'3','۴':'4','۵':'5','۶':'6','۷':'7','۸':'8','۹':'9',
    '٠':'0','١':'1','٢':'2','٣':'3','٤':'4','٥':'5','٦':'6','٧':'7','٨':'8','٩':'9'
  };
  const normalize = (s) => (s || '').replace(/[۰-۹٠-٩]/g, ch => map[ch] || ch);

  const normalizeEventValue = (e) => {
    const t = e.target;
    if (!t) return;
    // Vaadin fields dispatch 'value-changed' with detail.value
    const val = t.value ?? (e.detail && e.detail.value);
    if (typeof val === 'string') {
      const norm = normalize(val);
      if (norm !== val) {
        try {
          t.value = norm;
          if (e.detail && typeof e.detail.value === 'string') e.detail.value = norm;
        } catch (err) { /* ignore */ }
      }
    }
  };

  // Capture value changes globally for Vaadin fields
  document.addEventListener('value-changed', normalizeEventValue, true);
  // Also handle native inputs (if any)
  document.addEventListener('change', normalizeEventValue, true);
  document.addEventListener('blur', normalizeEventValue, true);
  // Expose globally for debugging
  window.__digits = { normalize };
})();
