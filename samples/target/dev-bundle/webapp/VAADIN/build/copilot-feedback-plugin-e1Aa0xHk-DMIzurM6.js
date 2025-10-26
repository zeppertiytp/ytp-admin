import { B as Be, y, c as ae, b as bt, v as ve, j as he, s as su, o as ol } from "./indexhtml-C1a2vvoG.js";
import { b } from "./state-DuWvKhvg-BDjNtdwz.js";
import { u, h as h$1 } from "./overlay-monkeypatch-CMrLotsi-PHikFacc.js";
import { i } from "./base-panel-DX8wcF3q-ChPIfHGp.js";
import { C } from "./icons-Dftvqm4k-DZfO2A6_.js";
const $ = "copilot-feedback-panel{display:flex;flex-direction:column;font:var(--font-xsmall);gap:var(--space-200);padding:var(--space-150)}copilot-feedback-panel>p{margin:0}copilot-feedback-panel .dialog-footer{display:flex;gap:var(--space-100)}copilot-feedback-panel :is(vaadin-select,vaadin-text-area,vaadin-text-field){padding:0}copilot-feedback-panel :is(vaadin-select,vaadin-text-area,vaadin-text-field)::part(input-field),copilot-feedback-panel vaadin-select-value-button{padding:0}copilot-feedback-panel vaadin-select::part(toggle-button){align-items:center;display:flex;height:var(--size-m);justify-content:center;width:var(--size-m)}copilot-feedback-panel vaadin-text-area textarea{line-height:var(--line-height-1);padding:calc((var(--size-m) - var(--line-height-1)) / 2) var(--space-100)}copilot-feedback-panel vaadin-text-area:hover::part(input-field){background:none}copilot-feedback-panel vaadin-text-field input{padding:0 var(--space-100)}copilot-feedback-panel>*::part(label){font-weight:var(--font-weight-medium);line-height:var(--line-height-1);margin:0;padding:0 var(--space-150) var(--space-50) 0}copilot-feedback-panel>*::part(helper-text){line-height:var(--line-height-1);margin:0}";
var A = Object.defineProperty, P = Object.getOwnPropertyDescriptor, o = (e, t, s, n) => {
  for (var i2 = n > 1 ? void 0 : n ? P(t, s) : t, l = e.length - 1, r; l >= 0; l--)
    (r = e[l]) && (i2 = (n ? r(t, s, i2) : r(i2)) || i2);
  return n && i2 && A(t, s, i2), i2;
};
const h = "https://github.com/vaadin/copilot/issues/new", T = "?template=feature_request.md&title=%5BFEATURE%5D", F = "A short, concise description of the bug and why you consider it a bug. Any details like exceptions and logs can be helpful as well.", D = "Please provide as many details as possible, this will help us deliver a fix as soon as possible.%0AThank you!%0A%0A%23%23%23 Description of the Bug%0A%0A{description}%0A%0A%23%23%23 Expected Behavior%0A%0AA description of what you would expect to happen. (Sometimes it is clear what the expected outcome is if something does not work, other times, it is not super clear.)%0A%0A%23%23%23 Minimal Reproducible Example%0A%0AWe would appreciate the minimum code with which we can reproduce the issue.%0A%0A%23%23%23 Versions%0A{versionsInfo}";
let a = class extends i {
  constructor() {
    super(), this.description = "", this.items = [
      {
        label: "Report a Bug",
        value: "bug",
        ghTitle: "[BUG]"
      },
      {
        label: "Ask a Question",
        value: "question",
        ghTitle: "[QUESTION]"
      },
      {
        label: "Share an Idea",
        value: "idea",
        ghTitle: "[FEATURE]"
      }
    ];
  }
  render() {
    return Be`<style>
        ${$}</style
      >${this.renderContent()}${this.renderFooter()}`;
  }
  firstUpdated() {
    u(this);
  }
  renderContent() {
    return this.message === void 0 ? Be`
          <p>
            Your insights are incredibly valuable to us. Whether you’ve encountered a hiccup, have questions, or ideas
            to make our platform better, we're all ears! If you wish, leave your email and we’ll get back to you. You
            can even share your code snippet with us for a clearer picture.
          </p>
          <vaadin-select
            label="What's on Your Mind?"
            .items="${this.items}"
            .value="${this.items[0].value}"
            @value-changed=${(e) => {
      this.type = e.detail.value;
    }}>
          </vaadin-select>
          <vaadin-text-area
            .value="${this.description}"
            @keydown=${this.keyDown}
            @focus=${() => {
      this.descriptionField.invalid = false, this.descriptionField.placeholder = "";
    }}
            @value-changed=${(e) => {
      this.description = e.detail.value;
    }}
            label="Tell Us More"
            helper-text="Describe what you're experiencing, wondering about, or envisioning. The more you share, the better we can understand and act on your feedback"></vaadin-text-area>
          <vaadin-text-field
            @keydown=${this.keyDown}
            @value-changed=${(e) => {
      this.email = e.detail.value;
    }}
            id="email"
            label="Your Email (Optional)"
            helper-text="Leave your email if you’d like us to follow up. Totally optional, but we’d love to keep the conversation going."></vaadin-text-field>
        ` : Be`<p>${this.message}</p>`;
  }
  renderFooter() {
    return this.message === void 0 ? Be`
          <div class="dialog-footer">
            <button
              style="margin-inline-end: auto"
              @click="${() => y.emit("system-info-with-callback", {
      callback: (e) => this.openGithub(e, this),
      notify: false
    })}">
              <span class="prefix">${C.github}</span>
              Create GitHub Issue
            </button>
            <button @click="${this.close}">Cancel</button>
            <button class="primary" @click="${this.submit}">Submit</button>
          </div>
        ` : Be` <div class="footer">
          <vaadin-button @click="${this.close}">Close</vaadin-button>
        </div>`;
  }
  close() {
    ae.updatePanel("copilot-feedback-panel", {
      floating: false
    });
  }
  submit() {
    var _a;
    if (bt("feedback"), this.description.trim() === "") {
      this.descriptionField.invalid = true, this.descriptionField.placeholder = "Please tell us more before sending", this.descriptionField.value = "";
      return;
    }
    const e = {
      description: this.description,
      email: this.email,
      type: this.type
    };
    y.emit("system-info-with-callback", {
      callback: (t) => ve(`${he}feedback`, { ...e, versions: t }),
      notify: false
    }), (_a = this.parentNode) == null ? void 0 : _a.style.setProperty("--section-height", "150px"), this.message = "Thank you for sharing feedback.";
  }
  keyDown(e) {
    (e.key === "Backspace" || e.key === "Delete") && e.stopPropagation();
  }
  openGithub(e, t) {
    var _a, _b;
    if (this.type === "idea") {
      window.open(`${h}${T}`);
      return;
    }
    const s = e.replace(/\n/g, "%0A"), n = `${(_a = t.items.find((r) => r.value === this.type)) == null ? void 0 : _a.ghTitle}`, i2 = t.description !== "" ? t.description : F, l = D.replace("{description}", i2).replace("{versionsInfo}", s);
    (_b = window.open(`${h}?title=${n}&body=${l}`, "_blank")) == null ? void 0 : _b.focus();
  }
};
o([
  b()
], a.prototype, "description", 2);
o([
  b()
], a.prototype, "type", 2);
o([
  b()
], a.prototype, "email", 2);
o([
  b()
], a.prototype, "message", 2);
o([
  b()
], a.prototype, "items", 2);
o([
  h$1("vaadin-text-area")
], a.prototype, "descriptionField", 2);
a = o([
  ol("copilot-feedback-panel")
], a);
const E = su({
  header: "Help Us Improve!",
  tag: "copilot-feedback-panel",
  width: 500,
  height: 500,
  floatingPosition: {
    top: 50,
    left: 50
  }
}), B = {
  init(e) {
    e.addPanel(E);
  }
};
window.Vaadin.copilot.plugins.push(B);
export {
  a as CopilotFeedbackPanel
};
