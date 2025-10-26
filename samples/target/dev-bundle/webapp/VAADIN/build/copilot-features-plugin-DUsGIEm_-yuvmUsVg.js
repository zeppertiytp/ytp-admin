import { p as p$1, B as Be, b as bt, S as So, R as Re, i as po, o as ol } from "./indexhtml-C1a2vvoG.js";
import { i } from "./base-panel-DX8wcF3q-ChPIfHGp.js";
import { C } from "./icons-Dftvqm4k-DZfO2A6_.js";
const b = "copilot-features-panel{padding:var(--space-100);font:var(--font-xsmall);display:grid;grid-template-columns:auto 1fr;gap:var(--space-50);height:auto}copilot-features-panel a{display:flex;align-items:center;gap:var(--space-50);white-space:nowrap}copilot-features-panel a svg{height:12px;width:12px;min-height:12px;min-width:12px}";
var $ = (e, a, t, n) => {
  for (var o = a, s = e.length - 1, r; s >= 0; s--)
    (r = e[s]) && (o = r(o) || o);
  return o;
};
const l = window.Vaadin.devTools;
let p = class extends i {
  render() {
    return Be` <style>
        ${b}
      </style>
      ${p$1.featureFlags.map(
      (e) => Be`
          <copilot-toggle-button
            .title="${e.title}"
            ?checked=${e.enabled}
            @on-change=${(a) => this.toggleFeatureFlag(a, e)}>
          </copilot-toggle-button>
          <a class="ahreflike" href="${e.moreInfoLink}" title="Learn more" target="_blank"
            >learn more ${C.share}</a
          >
        `
    )}`;
  }
  toggleFeatureFlag(e, a) {
    const t = e.target.checked;
    bt("use-feature", { source: "toggle", enabled: t, id: a.id }), l.frontendConnection ? (l.frontendConnection.send("setFeature", { featureId: a.id, enabled: t }), So({
      type: Re.INFORMATION,
      message: `“${a.title}” ${t ? "enabled" : "disabled"}`,
      details: a.requiresServerRestart ? "This feature requires a server restart" : void 0,
      dismissId: `feature${a.id}${t ? "Enabled" : "Disabled"}`
    }), po()) : l.log("error", `Unable to toggle feature ${a.title}: No server connection available`);
  }
};
p = $([
  ol("copilot-features-panel")
], p);
const x = {
  header: "Features",
  expanded: false,
  panelOrder: 35,
  panel: "right",
  floating: false,
  tag: "copilot-features-panel",
  helpUrl: "https://vaadin.com/docs/latest/flow/configuration/feature-flags"
}, F = {
  init(e) {
    e.addPanel(x);
  }
};
window.Vaadin.copilot.plugins.push(F);
export {
  p as CopilotFeaturesPanel
};
