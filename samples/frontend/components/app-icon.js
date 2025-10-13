
class AppIcon extends HTMLElement {
  static get observedAttributes() { return ['name','size','aria-label']; }
  constructor(){
    super();
    const root = this.attachShadow({mode:'open'});
    const wrap = document.createElement('span');
    wrap.setAttribute('part','icon');
    const svg = document.createElementNS('http://www.w3.org/2000/svg','svg');
    svg.setAttribute('width','1em'); svg.setAttribute('height','1em');
    svg.setAttribute('viewBox','0 0 24 24');
    const use = document.createElementNS('http://www.w3.org/2000/svg','use');
    svg.appendChild(use); wrap.appendChild(svg);
    const style = document.createElement('style');
    style.textContent = `:host{display:inline-flex;line-height:1;vertical-align:middle}
      [part=icon]{display:inline-flex}
      svg{width: var(--app-icon-size, 20px); height: var(--app-icon-size, 20px); color: currentColor; stroke: currentColor; fill: none}`;
    root.append(style, wrap);
    this._use = use; this._svg = svg;
  }
  attributeChangedCallback(){ this._update(); }
  connectedCallback(){ this._update(); }
  _update(){
    const name = this.getAttribute('name') || 'dashboard';
    const size = this.getAttribute('size') || '20';
    this._svg.style.setProperty('--app-icon-size', size.endsWith('px')? size : size+'px');
    this._use.setAttributeNS('http://www.w3.org/1999/xlink','href', `${new URL('icons/iconoir-sprite.svg', document.baseURI).pathname}#${name}`);
    const al = this.getAttribute('aria-label') || name; this.setAttribute('role','img'); this.setAttribute('aria-label', al);
  }
}
customElements.define('app-icon', AppIcon);
