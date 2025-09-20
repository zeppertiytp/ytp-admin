import { LitElement, html, nothing } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import { classMap } from 'lit/directives/class-map.js';
import { toJalaali, toGregorian, jalaaliMonthLength, isValidJalaaliDate } from 'jalaali-js';
import type { PropertyValueMap } from 'lit';

type JalaliDate = { jy: number; jm: number; jd: number };
type DateParts = { year: number; month: number; day: number; hour: number; minute: number; second: number };
type DayCell = { jy: number; jm: number; jd: number; currentMonth: boolean; disabled: boolean };

@customElement('jalali-date-time-picker')
export class JalaliDateTimePicker extends LitElement {
  private static idSequence = 0;

  private readonly labelId = `jalali-dtp-label-${++JalaliDateTimePicker.idSequence}`;
  private readonly helperId = `jalali-dtp-helper-${JalaliDateTimePicker.idSequence}`;
  private readonly errorId = `jalali-dtp-error-${JalaliDateTimePicker.idSequence}`;

  private internalUpdate = false;
  private minParts: DateParts | null = null;
  private maxParts: DateParts | null = null;
  private readonly handleSlotChange = () => {
    this.requestUpdate();
  };
  private readonly handleDocumentPointerDown = (event: Event): void => {
    if (!this.opened) {
      return;
    }
    const path = event.composedPath();
    if (!path.includes(this)) {
      this.setOpened(false);
    }
  };
  private readonly handleDocumentKeyDown = (event: KeyboardEvent): void => {
    if (!this.opened) {
      return;
    }
    if (event.key === 'Escape') {
      this.setOpened(false);
    }
  };

  @property({ type: String })
  value = '';

  @property({ type: String })
  min = '';

  @property({ type: String })
  max = '';

  @property({ type: Boolean, reflect: true })
  disabled = false;

  @property({ type: Boolean, reflect: true, attribute: 'readonly' })
  readOnly = false;

  @property({ type: Boolean, reflect: true })
  required = false;

  @property({ type: Boolean, reflect: true })
  invalid = false;

  @property({ type: String })
  errorMessage = '';

  @property({ type: String })
  label = '';

  @property({ type: String })
  helperText = '';

  @property({ type: String })
  openButtonLabel = '';

  @property({ type: String })
  mode: 'date-time' | 'date' = 'date-time';

  @property({ type: Array })
  monthNames: string[] = [
    'فروردین',
    'اردیبهشت',
    'خرداد',
    'تیر',
    'مرداد',
    'شهریور',
    'مهر',
    'آبان',
    'آذر',
    'دی',
    'بهمن',
    'اسفند'
  ];

  @property({ type: Array })
  weekdayNames: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  @property({ type: Number })
  firstDayOfWeek = 6;

  @property({ type: String })
  calendarHeading = '';

  @property({ type: String })
  timeHeading = '';

  @property({ type: String })
  todayLabel = '';

  @property({ type: String })
  nowLabel = '';

  @property({ type: String })
  clearLabel = '';

  @property({ type: String })
  hourLabel = '';

  @property({ type: String })
  minuteLabel = '';

  @property({ type: String })
  noValueLabel = '';

  @property({ type: String })
  ariaSelectedLabel = '';

  @property({ type: String })
  ariaTodayLabel = '';

  @property({ type: Boolean })
  usePersianDigits = true;

  @property({ type: Number })
  minuteStep = 1;

  @state()
  private opened = false;

  @state()
  private displayYear = 0;

  @state()
  private displayMonth = 0;

  @state()
  private selectedDate: JalaliDate | null = null;

  @state()
  private selectedHour = 0;

  @state()
  private selectedMinute = 0;

  @state()
  private selectedSecond = 0;

  constructor() {
    super();
    const now = new Date();
    const jalaliToday = toJalaali(now.getFullYear(), now.getMonth() + 1, now.getDate());
    this.displayYear = jalaliToday.jy;
    this.displayMonth = jalaliToday.jm;
  }

  connectedCallback(): void {
    super.connectedCallback();
    this.syncFromValue(this.value);
    this.updateRangeState();
    this.addDocumentListenersIfNeeded();
  }

  updated(changed: PropertyValueMap<JalaliDateTimePicker>): void {
    if (changed.has('value') && !this.internalUpdate) {
      this.syncFromValue(this.value);
    }
    if (changed.has('min') || changed.has('max')) {
      this.updateRangeState();
      this.ensureSelectionWithinRange();
    }
    if (changed.has('minuteStep')) {
      const step = this.getMinuteStep();
      if (step !== this.minuteStep) {
        this.minuteStep = step;
      }
      this.ensureSelectionWithinRange();
    }
    if (changed.has('mode')) {
      if (this.mode === 'date') {
        this.selectedHour = 0;
        this.selectedMinute = 0;
      }
      this.updateRangeState();
      this.ensureSelectionWithinRange();
      this.requestUpdate();
    }
    if (changed.has('opened')) {
      this.addDocumentListenersIfNeeded();
      if (this.opened) {
        this.updateComplete.then(() => {
          const surface = this.renderRoot.querySelector('.picker-surface');
          if (surface instanceof HTMLElement) {
            surface.focus({ preventScroll: true });
          }
        });
      }
    }
    if ((changed.has('disabled') || changed.has('readOnly')) && (this.disabled || this.readOnly)) {
      this.setOpened(false);
    }
  }

  focus(options?: FocusOptions): void {
    const root = this.renderRoot as DocumentFragment & ParentNode;
    const target = (root.querySelector('.day-button[aria-pressed="true"]') as HTMLButtonElement | null)
      ?? (root.querySelector('.day-button:not([disabled])') as HTMLButtonElement | null)
      ?? (root.querySelector('.hour-select') as HTMLSelectElement | null)
      ?? (root.querySelector('.minute-select') as HTMLSelectElement | null);
    if (target) {
      target.focus(options);
    } else {
      super.focus(options);
    }
  }

  blur(): void {
    const rootNode = this.getRootNode();
    const active = rootNode instanceof Document
      ? rootNode.activeElement
      : rootNode instanceof ShadowRoot
        ? rootNode.activeElement
        : null;
    if (active instanceof HTMLElement && (this === active || (this.renderRoot as DocumentFragment & ParentNode).contains(active))) {
      active.blur();
    }
  }

  private syncFromValue(value: string): void {
    const parts = this.parseIso(value);
    if (!parts) {
      this.selectedDate = null;
      this.selectedHour = 0;
      this.selectedMinute = 0;
      this.selectedSecond = 0;
      return;
    }
    this.applySelectionFromGregorian(parts, true);
  }

  private updateRangeState(): void {
    this.minParts = this.normalizeRangeParts(this.parseIso(this.min));
    this.maxParts = this.normalizeRangeParts(this.parseIso(this.max));
  }

  private ensureSelectionWithinRange(): void {
    if (!this.selectedDate) {
      return;
    }
    const parts = this.composeGregorianParts(this.selectedDate, {
      hour: this.mode === 'date' ? 0 : this.selectedHour,
      minute: this.mode === 'date' ? 0 : this.selectedMinute,
      second: this.selectedSecond
    });
    const constrained = this.applyConstraints(parts);
    const currentValueParts = this.parseIso(this.value);
    const needsDateNormalization = this.mode === 'date'
      && currentValueParts !== null
      && (currentValueParts.hour !== 0 || currentValueParts.minute !== 0);
    if (!this.arePartsEqual(parts, constrained) || needsDateNormalization) {
      this.applySelectionFromGregorian(constrained, this.isAtBoundary(constrained));
      this.commitValue(constrained, false);
    }
  }

  private applySelectionFromGregorian(parts: DateParts, preserveMinute = false): void {
    const jalali = toJalaali(parts.year, parts.month, parts.day);
    this.selectedDate = { jy: jalali.jy, jm: jalali.jm, jd: jalali.jd };
    this.displayYear = jalali.jy;
    this.displayMonth = jalali.jm;
    if (this.mode === 'date') {
      this.selectedHour = 0;
      this.selectedMinute = 0;
    } else {
      this.selectedHour = this.normalizeHour(parts.hour);
      this.selectedMinute = preserveMinute
        ? this.clampMinute(parts.minute)
        : this.normalizeMinute(parts.minute);
    }
    this.selectedSecond = 0;
  }

  private normalizeHour(hour: number): number {
    if (!Number.isFinite(hour)) {
      return 0;
    }
    return Math.max(0, Math.min(23, Math.trunc(hour)));
  }

  private normalizeMinute(minute: number): number {
    const step = this.getMinuteStep();
    if (!Number.isFinite(minute)) {
      return 0;
    }
    const bounded = this.clampMinute(minute);
    const stepAligned = Math.floor(bounded / step) * step;
    return Math.max(0, Math.min(59, stepAligned));
  }

  private clampMinute(minute: number): number {
    if (!Number.isFinite(minute)) {
      return 0;
    }
    return Math.max(0, Math.min(59, Math.trunc(minute)));
  }

  private getMinuteStep(): number {
    const step = Number.isFinite(this.minuteStep) ? Math.trunc(this.minuteStep) : 1;
    if (step <= 0) {
      return 1;
    }
    if (step > 60) {
      return 60;
    }
    return step;
  }

  private parseIso(value: string | null | undefined): DateParts | null {
    if (!value) {
      return null;
    }
    const trimmed = value.trim();
    if (!trimmed) {
      return null;
    }
    const dateOnlyMatch = trimmed.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (dateOnlyMatch) {
      const [, y, m, d] = dateOnlyMatch;
      return {
        year: Number(y),
        month: Number(m),
        day: Number(d),
        hour: 0,
        minute: 0,
        second: 0
      };
    }
    const match = trimmed.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?(?:\.\d+)?$/);
    if (!match) {
      return null;
    }
    const [, y, m, d, h, min, s] = match;
    return {
      year: Number(y),
      month: Number(m),
      day: Number(d),
      hour: Number(h),
      minute: Number(min),
      second: s ? Number(s) : 0
    };
  }

  private normalizeRangeParts(parts: DateParts | null): DateParts | null {
    if (!parts) {
      return null;
    }
    return {
      ...parts,
      hour: this.mode === 'date' ? 0 : this.normalizeHour(parts.hour),
      minute: this.mode === 'date' ? 0 : this.clampMinute(parts.minute),
      second: 0
    };
  }

  private composeGregorianParts(date: JalaliDate, time: { hour: number; minute: number; second: number }): DateParts {
    const gregorian = toGregorian(date.jy, date.jm, date.jd);
    return {
      year: gregorian.gy,
      month: gregorian.gm,
      day: gregorian.gd,
      hour: this.mode === 'date' ? 0 : this.normalizeHour(time.hour),
      minute: this.mode === 'date' ? 0 : this.normalizeMinute(time.minute),
      second: 0
    };
  }

  private applyConstraints(parts: DateParts): DateParts {
    let result: DateParts = { ...parts };
    result.hour = this.mode === 'date' ? 0 : this.normalizeHour(result.hour);
    result.minute = this.mode === 'date' ? 0 : this.normalizeMinute(result.minute);
    result.second = 0;

    const min = this.minParts;
    const max = this.maxParts;
    if (min && this.compareParts(result, min) < 0) {
      result = { ...min };
    }
    if (max && this.compareParts(result, max) > 0) {
      result = { ...max };
    }

    const boundary = this.isAtBoundary(result);
    result.hour = this.mode === 'date' ? 0 : this.normalizeHour(result.hour);
    if (this.mode === 'date') {
      result.minute = 0;
    } else if (boundary) {
      result.minute = this.clampMinute(result.minute);
    } else {
      result.minute = this.normalizeMinute(result.minute);
    }
    result.second = 0;
    return result;
  }

  private isAtBoundary(parts: DateParts): boolean {
    const min = this.minParts;
    const max = this.maxParts;
    const isAtMin = min !== null && this.compareParts(parts, min) === 0;
    const isAtMax = max !== null && this.compareParts(parts, max) === 0;
    return isAtMin || isAtMax;
  }

  private arePartsEqual(a: DateParts, b: DateParts): boolean {
    return a.year === b.year && a.month === b.month && a.day === b.day && a.hour === b.hour && a.minute === b.minute;
  }

  private compareParts(a: DateParts, b: DateParts): number {
    if (a.year !== b.year) return a.year - b.year;
    if (a.month !== b.month) return a.month - b.month;
    if (a.day !== b.day) return a.day - b.day;
    if (this.mode === 'date') {
      return 0;
    }
    if (a.hour !== b.hour) return a.hour - b.hour;
    if (a.minute !== b.minute) return a.minute - b.minute;
    return 0;
  }

  private compareDateOnly(a: DateParts, b: DateParts): number {
    if (a.year !== b.year) return a.year - b.year;
    if (a.month !== b.month) return a.month - b.month;
    return a.day - b.day;
  }

  private commitValue(parts: DateParts | null, fromUser: boolean): void {
    let iso = '';
    if (parts) {
      const mm = this.pad(parts.month);
      const dd = this.pad(parts.day);
      const hourValue = this.mode === 'date' ? 0 : this.normalizeHour(parts.hour);
      const hh = this.pad(hourValue);
      const minuteValue = this.mode === 'date' ? 0 : this.clampMinute(parts.minute);
      const min = this.pad(minuteValue);
      iso = `${parts.year}-${mm}-${dd}T${hh}:${min}:00`;
    }
    const old = this.value;
    if (old === iso) {
      if (fromUser) {
        this.dispatchValueChange();
      }
      return;
    }
    this.internalUpdate = true;
    this.value = iso;
    this.internalUpdate = false;
    this.dispatchValueChange();
  }

  private dispatchValueChange(): void {
    this.dispatchEvent(new CustomEvent('value-changed', {
      detail: { value: this.value },
      bubbles: true,
      composed: true
    }));
  }

  private buildCalendarCells(): DayCell[] {
    const year = this.displayYear;
    const month = this.displayMonth;
    const daysInMonth = jalaaliMonthLength(year, month);
    const firstGregorian = toGregorian(year, month, 1);
    const firstDate = new Date(firstGregorian.gy, firstGregorian.gm - 1, firstGregorian.gd);
    const firstWeekday = firstDate.getDay();
    const leading = this.modulo(firstWeekday - this.firstDayOfWeek, 7);
    const totalCells = Math.ceil((leading + daysInMonth) / 7) * 7;
    const cells: DayCell[] = [];
    const prevInfo = this.getAdjacentMonth(year, month, -1);
    const nextInfo = this.getAdjacentMonth(year, month, 1);
    const prevMonthLength = jalaaliMonthLength(prevInfo.year, prevInfo.month);

    for (let index = 0; index < totalCells; index += 1) {
      const offset = index - leading;
      let cell: DayCell;
      if (offset < 0) {
        const day = prevMonthLength + offset + 1;
        cell = {
          jy: prevInfo.year,
          jm: prevInfo.month,
          jd: day,
          currentMonth: false,
          disabled: !this.isValidDay(prevInfo.year, prevInfo.month, day)
        };
      } else if (offset >= daysInMonth) {
        const day = offset - daysInMonth + 1;
        cell = {
          jy: nextInfo.year,
          jm: nextInfo.month,
          jd: day,
          currentMonth: false,
          disabled: !this.isValidDay(nextInfo.year, nextInfo.month, day)
        };
      } else {
        const day = offset + 1;
        cell = {
          jy: year,
          jm: month,
          jd: day,
          currentMonth: true,
          disabled: !this.isValidDay(year, month, day)
        };
      }
      cells.push(cell);
    }

    return cells;
  }

  private isValidDay(jy: number, jm: number, jd: number): boolean {
    if (!isValidJalaaliDate(jy, jm, jd)) {
      return false;
    }
    const gregorian = toGregorian(jy, jm, jd);
    const dayParts: DateParts = {
      year: gregorian.gy,
      month: gregorian.gm,
      day: gregorian.gd,
      hour: 0,
      minute: 0,
      second: 0
    };
    if (this.minParts && this.compareDateOnly(dayParts, this.minParts) < 0) {
      return false;
    }
    if (this.maxParts && this.compareDateOnly(dayParts, this.maxParts) > 0) {
      return false;
    }
    return true;
  }

  private getAdjacentMonth(year: number, month: number, delta: number): { year: number; month: number } {
    let jy = year;
    let jm = month + delta;
    while (jm < 1) {
      jm += 12;
      jy -= 1;
    }
    while (jm > 12) {
      jm -= 12;
      jy += 1;
    }
    return { year: jy, month: jm };
  }

  private modulo(value: number, divisor: number): number {
    return ((value % divisor) + divisor) % divisor;
  }

  private handlePrevMonth = (): void => {
    if (this.disabled || this.readOnly) {
      return;
    }
    const prev = this.getAdjacentMonth(this.displayYear, this.displayMonth, -1);
    this.displayYear = prev.year;
    this.displayMonth = prev.month;
  };

  private handleNextMonth = (): void => {
    if (this.disabled || this.readOnly) {
      return;
    }
    const next = this.getAdjacentMonth(this.displayYear, this.displayMonth, 1);
    this.displayYear = next.year;
    this.displayMonth = next.month;
  };

  private handleDayClick(cell: DayCell): void {
    if (this.disabled || this.readOnly || cell.disabled) {
      return;
    }
    const jalali: JalaliDate = { jy: cell.jy, jm: cell.jm, jd: cell.jd };
    const parts = this.applyConstraints(
      this.composeGregorianParts(jalali, {
        hour: this.mode === 'date' ? 0 : this.selectedDate ? this.selectedHour : 0,
        minute: this.mode === 'date' ? 0 : this.selectedDate ? this.selectedMinute : 0,
        second: 0
      })
    );
    this.applySelectionFromGregorian(parts, this.isAtBoundary(parts));
    this.commitValue(parts, true);
  }

  private handleTodayClick = (): void => {
    if (this.disabled || this.readOnly) {
      return;
    }
    const now = new Date();
    const jalaliNow = toJalaali(now.getFullYear(), now.getMonth() + 1, now.getDate());
    const currentTime = {
      hour: this.mode === 'date' ? 0 : this.selectedHour,
      minute: this.mode === 'date' ? 0 : this.selectedMinute,
      second: this.selectedSecond
    };
    const parts = this.applyConstraints(
      this.composeGregorianParts({ jy: jalaliNow.jy, jm: jalaliNow.jm, jd: jalaliNow.jd }, currentTime)
    );
    this.applySelectionFromGregorian(parts, this.isAtBoundary(parts));
    this.commitValue(parts, true);
  };

  private handleNowClick = (): void => {
    if (this.disabled || this.readOnly || this.mode === 'date') {
      return;
    }
    const now = new Date();
    const parts = this.applyConstraints({
      year: now.getFullYear(),
      month: now.getMonth() + 1,
      day: now.getDate(),
      hour: now.getHours(),
      minute: now.getMinutes(),
      second: 0
    });
    this.applySelectionFromGregorian(parts, this.isAtBoundary(parts));
    this.commitValue(parts, true);
  };

  private handleClearClick = (): void => {
    if (this.disabled || this.readOnly) {
      return;
    }
    this.selectedDate = null;
    this.selectedHour = 0;
    this.selectedMinute = 0;
    this.selectedSecond = 0;
    this.commitValue(null, true);
  };

  private handleHourChange = (event: Event): void => {
    if (!this.selectedDate || this.disabled || this.readOnly || this.mode === 'date') {
      return;
    }
    const value = Number((event.target as HTMLSelectElement).value);
    if (Number.isNaN(value)) {
      return;
    }
    const parts = this.applyConstraints(
      this.composeGregorianParts(this.selectedDate, {
        hour: value,
        minute: this.selectedMinute,
        second: 0
      })
    );
    this.applySelectionFromGregorian(parts, this.isAtBoundary(parts));
    this.commitValue(parts, true);
  };

  private handleMinuteChange = (event: Event): void => {
    if (!this.selectedDate || this.disabled || this.readOnly || this.mode === 'date') {
      return;
    }
    const value = Number((event.target as HTMLSelectElement).value);
    if (Number.isNaN(value)) {
      return;
    }
    const parts = this.applyConstraints(
      this.composeGregorianParts(this.selectedDate, {
        hour: this.selectedHour,
        minute: value,
        second: 0
      })
    );
    this.applySelectionFromGregorian(parts, this.isAtBoundary(parts));
    this.commitValue(parts, true);
  };

  private formatPreview(): string {
    if (!this.selectedDate) {
      return this.noValueLabel || '';
    }
    const monthLabel = this.monthNames?.[this.selectedDate.jm - 1] ?? this.selectedDate.jm.toString();
    const dayText = this.formatNumber(this.selectedDate.jd);
    const yearText = this.formatNumber(this.selectedDate.jy, 4);
    if (this.mode === 'date') {
      return `${dayText} ${monthLabel} ${yearText}`;
    }
    const timeText = `${this.formatNumber(this.selectedHour)}:${this.formatNumber(this.selectedMinute)}`;
    return `${dayText} ${monthLabel} ${yearText} – ${timeText}`;
  }

  private formatNumber(value: number, minDigits = 2): string {
    const padded = value.toString().padStart(minDigits, '0');
    if (!this.usePersianDigits) {
      return padded;
    }
    const persianDigits = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
    return padded.replace(/\d/g, d => persianDigits[Number(d)]);
  }

  private pad(value: number): string {
    return value.toString().padStart(2, '0');
  }

  private getOrderedWeekdays(): string[] {
    const base = Array.isArray(this.weekdayNames) && this.weekdayNames.length >= 7
      ? this.weekdayNames
      : ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const ordered: string[] = [];
    for (let i = 0; i < 7; i += 1) {
      const index = (this.firstDayOfWeek + i) % 7;
      ordered.push(base[index] ?? '');
    }
    return ordered;
  }

  private isSelected(cell: DayCell): boolean {
    if (!this.selectedDate) {
      return false;
    }
    return (
      cell.jy === this.selectedDate.jy &&
      cell.jm === this.selectedDate.jm &&
      cell.jd === this.selectedDate.jd
    );
  }

  private isToday(cell: DayCell): boolean {
    const now = new Date();
    const jalali = toJalaali(now.getFullYear(), now.getMonth() + 1, now.getDate());
    return cell.jy === jalali.jy && cell.jm === jalali.jm && cell.jd === jalali.jd;
  }

  private dayAriaLabel(cell: DayCell): string {
    const monthLabel = this.monthNames?.[cell.jm - 1] ?? cell.jm.toString();
    const dayText = this.formatNumber(cell.jd);
    const yearText = this.formatNumber(cell.jy, 4);
    return `${dayText} ${monthLabel} ${yearText}`;
  }

  render() {
    const weekdays = this.getOrderedWeekdays();
    const cells = this.buildCalendarCells();
    const describedByIds: string[] = [];
    const hasHelper = this.hasHelperContent();
    if (hasHelper) {
      describedByIds.push(this.helperId);
    }
    if (this.invalid && this.errorMessage) {
      describedByIds.push(this.errorId);
    }
    const describedBy = describedByIds.join(' ');
    const minuteOptions = this.mode === 'date-time' ? this.getMinuteOptions() : [];
    const hasLabel = this.hasLabelContent();

    return html`
      <style>
        :host {
          display: inline-flex;
          flex-direction: column;
          font-family: var(--lumo-font-family, inherit);
          color: var(--lumo-body-text-color, inherit);
          gap: 0.5rem;
        }

        .field-wrapper {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
          min-width: 18rem;
          max-width: 24rem;
          position: relative;
        }

        .control-row {
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          gap: 0.75rem;
        }

        .toggle-button {
          border: 1px solid var(--lumo-contrast-30pct, rgba(0, 0, 0, 0.2));
          border-radius: var(--lumo-border-radius-m, 0.5rem);
          padding: 0.45rem 0.95rem;
          background: var(--lumo-base-color, #fff);
          color: inherit;
          cursor: pointer;
          font: inherit;
          min-width: 9rem;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 0.35rem;
          box-shadow: var(--lumo-box-shadow-xs, 0 1px 3px rgba(0, 0, 0, 0.12));
        }

        .toggle-button:hover:not([disabled]),
        .toggle-button:focus-visible:not([disabled]) {
          border-color: var(--lumo-primary-color, #006ee6);
          outline: none;
          box-shadow: var(--lumo-box-shadow-s, 0 2px 6px rgba(0, 0, 0, 0.18));
        }

        .toggle-button[disabled] {
          cursor: default;
          opacity: 0.6;
        }

        .field-label {
          font-size: var(--lumo-font-size-s, 0.9rem);
          font-weight: 600;
          display: inline-flex;
          align-items: baseline;
          gap: 0.25rem;
        }

        .required-indicator {
          color: var(--lumo-error-color, #c02020);
        }

        .overlay {
          position: absolute;
          top: calc(100% + 0.35rem);
          inset-inline-start: 0;
          z-index: 1000;
          min-width: 100%;
        }

        .picker-surface {
          border: 1px solid var(--lumo-contrast-20pct, rgba(0, 0, 0, 0.15));
          border-radius: var(--lumo-border-radius-m, 0.5rem);
          padding: 0.75rem;
          background: var(--lumo-base-color, #fff);
          display: flex;
          flex-direction: column;
          gap: 0.75rem;
          box-shadow: var(--lumo-box-shadow-m, 0 6px 18px rgba(0, 0, 0, 0.15));
        }

        :host([invalid]) .picker-surface {
          border-color: var(--lumo-error-color, #c02020);
        }

        .calendar-section {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
        }

        .section-heading,
        .time-heading {
          font-size: var(--lumo-font-size-s, 0.9rem);
          font-weight: 600;
        }

        .calendar-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 0.5rem;
        }

        .month-label {
          font-weight: 600;
        }

        .nav-button {
          background: none;
          border: none;
          color: inherit;
          cursor: pointer;
          padding: 0.25rem 0.5rem;
          border-radius: var(--lumo-border-radius-s, 0.3rem);
        }

        .nav-button:hover:not([disabled]),
        .nav-button:focus-visible:not([disabled]) {
          background: var(--lumo-contrast-10pct, rgba(0, 0, 0, 0.06));
          outline: none;
        }

        .nav-button[disabled] {
          opacity: 0.5;
          cursor: default;
        }

        .weekday-row,
        .calendar-grid {
          display: grid;
          grid-template-columns: repeat(7, minmax(0, 1fr));
          gap: 0.25rem;
          text-align: center;
        }

        .weekday {
          font-size: var(--lumo-font-size-xs, 0.75rem);
          color: var(--lumo-secondary-text-color, rgba(0, 0, 0, 0.6));
        }

        .day-button {
          border: none;
          border-radius: 999px;
          padding: 0.45rem 0;
          font: inherit;
          cursor: pointer;
          background: transparent;
          position: relative;
        }

        .day-button.other-month {
          color: var(--lumo-secondary-text-color, rgba(0, 0, 0, 0.55));
        }

        .day-button.today::after {
          content: '';
          position: absolute;
          inset-inline: 0.35rem;
          bottom: 0.3rem;
          height: 0.15rem;
          border-radius: 999px;
          background: var(--lumo-primary-color, #006ee6);
          opacity: 0.65;
        }

        .day-button[disabled] {
          cursor: default;
          color: var(--lumo-disabled-text-color, rgba(0, 0, 0, 0.38));
        }

        .day-button[aria-pressed="true"] {
          background: var(--lumo-primary-color, #006ee6);
          color: var(--lumo-base-color, #fff);
        }

        .time-section {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
        }

        .time-controls {
          display: flex;
          gap: 0.75rem;
          flex-wrap: wrap;
        }

        .time-control {
          display: flex;
          flex-direction: column;
          gap: 0.25rem;
          font-size: var(--lumo-font-size-xs, 0.75rem);
        }

        select {
          min-width: 5rem;
          padding: 0.35rem 0.5rem;
          border-radius: var(--lumo-border-radius-s, 0.25rem);
          border: 1px solid var(--lumo-contrast-20pct, rgba(0, 0, 0, 0.18));
          font: inherit;
        }

        select:focus-visible {
          outline: 2px solid var(--lumo-primary-color, #006ee6);
          outline-offset: 1px;
        }

        select:disabled {
          background: var(--lumo-contrast-5pct, rgba(0, 0, 0, 0.04));
          color: var(--lumo-disabled-text-color, rgba(0, 0, 0, 0.38));
        }

        .actions {
          display: flex;
          gap: 0.5rem;
          flex-wrap: wrap;
        }

        .actions button {
          border: none;
          padding: 0.35rem 0.75rem;
          border-radius: var(--lumo-border-radius-s, 0.3rem);
          cursor: pointer;
          font-size: var(--lumo-font-size-s, 0.85rem);
        }

        .primary-action {
          background: var(--lumo-primary-color, #006ee6);
          color: var(--lumo-base-color, #fff);
        }

        .secondary-action {
          background: var(--lumo-contrast-10pct, rgba(0, 0, 0, 0.06));
        }

        .actions button[disabled] {
          cursor: default;
          opacity: 0.6;
        }

        .preview {
          font-size: var(--lumo-font-size-s, 0.85rem);
          color: var(--lumo-secondary-text-color, rgba(0, 0, 0, 0.6));
        }

        .helper-text {
          font-size: var(--lumo-font-size-xs, 0.75rem);
          color: var(--lumo-secondary-text-color, rgba(0, 0, 0, 0.6));
        }

        .error-text {
          font-size: var(--lumo-font-size-xs, 0.75rem);
          color: var(--lumo-error-text-color, #c02020);
        }

        :host([dir="rtl"]) .field-wrapper {
          direction: rtl;
          text-align: right;
        }

        :host([dir="rtl"]) .overlay {
          inset-inline-start: auto;
          inset-inline-end: 0;
        }

        :host([dir="rtl"]) .calendar-header {
          flex-direction: row-reverse;
        }

        :host([dir="rtl"]) .weekday-row {
          direction: rtl;
        }

        :host([disabled]) {
          opacity: 0.7;
          pointer-events: none;
        }
      </style>
      <div
        class="field-wrapper"
        role="group"
        aria-labelledby=${hasLabel ? this.labelId : nothing}
        aria-describedby=${describedBy ? describedBy : nothing}
      >
        ${this.renderLabel()}
        <div class="control-row">
          <button
            type="button"
            class="toggle-button"
            part="toggle-button"
            @click=${this.toggleOpened}
            ?disabled=${this.disabled || this.readOnly}
            aria-haspopup="dialog"
            aria-expanded=${this.opened ? 'true' : 'false'}
          >
            ${this.getOpenButtonLabel()}
          </button>
          <div class="preview" part="value">${this.formatPreview()}</div>
        </div>
        ${this.opened
          ? html`
              <div class="overlay" part="overlay" role="dialog" aria-modal="false">
                ${this.renderSurface(weekdays, cells, minuteOptions)}
              </div>
            `
          : nothing}
        ${hasHelper
          ? html`<div class="helper-text" id=${this.helperId} part="helper"><slot name="helper" @slotchange=${this.handleSlotChange}>${this.helperText}</slot></div>`
          : nothing}
        ${this.invalid && this.errorMessage
          ? html`<div class="error-text" id=${this.errorId} part="error">${this.errorMessage}</div>`
          : null}
      </div>
    `;
  }

  private renderSurface(weekdays: string[], cells: DayCell[], minuteOptions: number[]) {
    return html`
      <div class="picker-surface" part="input" tabindex="-1">
        <div class="calendar-section" part="calendar">
          ${this.calendarHeading
            ? html`<div class="section-heading">${this.calendarHeading}</div>`
            : nothing}
          <div class="calendar-header">
            <button type="button" class="nav-button" @click=${this.handlePrevMonth} ?disabled=${this.disabled || this.readOnly}>◀</button>
            <div class="month-label">${this.getMonthLabel()}</div>
            <button type="button" class="nav-button" @click=${this.handleNextMonth} ?disabled=${this.disabled || this.readOnly}>▶</button>
          </div>
          <div class="weekday-row">
            ${weekdays.map(weekday => html`<div class="weekday">${weekday}</div>`)}
          </div>
          <div class="calendar-grid">
            ${cells.map(cell => this.renderDayCell(cell))}
          </div>
        </div>
        ${this.mode === 'date-time'
          ? html`
              <div class="time-section" part="time">
                ${this.timeHeading ? html`<div class="time-heading">${this.timeHeading}</div>` : nothing}
                <div class="time-controls">
                  <label class="time-control">
                    <span>${this.hourLabel}</span>
                    <select class="hour-select" @change=${this.handleHourChange} ?disabled=${this.disabled || this.readOnly || !this.selectedDate}>
                      ${Array.from({ length: 24 }, (_, index) => index).map(hour => html`
                        <option value=${hour} ?selected=${hour === this.selectedHour}>${this.formatNumber(hour)}</option>
                      `)}
                    </select>
                  </label>
                  <label class="time-control">
                    <span>${this.minuteLabel}</span>
                    <select class="minute-select" @change=${this.handleMinuteChange} ?disabled=${this.disabled || this.readOnly || !this.selectedDate}>
                      ${minuteOptions.map(minute => html`
                        <option value=${minute} ?selected=${minute === this.selectedMinute}>${this.formatNumber(minute)}</option>
                      `)}
                    </select>
                  </label>
                </div>
              </div>
            `
          : nothing}
        <div class="actions">
          ${this.mode === 'date-time'
            ? html`<button type="button" class="primary-action" @click=${this.handleNowClick} ?disabled=${this.disabled || this.readOnly}>${this.nowLabel}</button>`
            : nothing}
          <button type="button" class="secondary-action" @click=${this.handleTodayClick} ?disabled=${this.disabled || this.readOnly}>${this.todayLabel}</button>
          <button type="button" class="secondary-action" @click=${this.handleClearClick} ?disabled=${this.disabled || this.readOnly || !this.selectedDate}>${this.clearLabel}</button>
        </div>
      </div>
    `;
  }

  private renderLabel() {
    if (!this.hasLabelContent()) {
      return null;
    }
    return html`
      <label class="field-label" id=${this.labelId} part="label">
        <slot name="label" @slotchange=${this.handleSlotChange}>${this.label}</slot>
        ${this.required ? html`<span class="required-indicator" aria-hidden="true">*</span>` : ''}
      </label>
    `;
  }

  private renderDayCell(cell: DayCell) {
    const selected = this.isSelected(cell);
    const classes = classMap({
      'day-button': true,
      'other-month': !cell.currentMonth,
      today: this.isToday(cell)
    });
    const ariaPressed = selected ? 'true' : 'false';
    let ariaLabel = this.dayAriaLabel(cell);
    if (selected && this.ariaSelectedLabel) {
      ariaLabel = `${ariaLabel} – ${this.ariaSelectedLabel}`;
    }
    if (this.isToday(cell) && this.ariaTodayLabel) {
      ariaLabel = `${ariaLabel} – ${this.ariaTodayLabel}`;
    }
    const ariaCurrent = selected ? 'date' : undefined;
    return html`
      <button
        type="button"
        class=${classes}
        ?disabled=${cell.disabled}
        aria-pressed=${ariaPressed}
        aria-label=${ariaLabel}
        aria-current=${ariaCurrent}
        @click=${() => this.handleDayClick(cell)}
      >
        ${this.formatNumber(cell.jd)}
      </button>
    `;
  }

  private getMonthLabel(): string {
    const month = this.monthNames?.[this.displayMonth - 1] ?? this.displayMonth.toString();
    return `${month} ${this.formatNumber(this.displayYear, 4)}`;
  }

  private getOpenButtonLabel(): string {
    if (this.openButtonLabel && this.openButtonLabel.trim().length > 0) {
      return this.openButtonLabel;
    }
    return this.mode === 'date' ? 'Select date' : 'Select date & time';
  }

  private getMinuteOptions(): number[] {
    const step = this.getMinuteStep();
    const options: number[] = [];
    for (let minute = 0; minute < 60; minute += step) {
      options.push(minute);
    }
    if (this.selectedMinute % step !== 0) {
      options.push(this.selectedMinute);
    }
    return Array.from(new Set(options)).sort((a, b) => a - b);
  }

  private hasLabelContent(): boolean {
    return Boolean(this.label) || Boolean(this.querySelector('[slot="label"]'));
  }

  private hasHelperContent(): boolean {
    return Boolean(this.helperText) || Boolean(this.querySelector('[slot="helper"]'));
  }

  private toggleOpened = (): void => {
    if (this.disabled || this.readOnly) {
      return;
    }
    this.setOpened(!this.opened);
  };

  public openPicker(): void {
    if (this.disabled || this.readOnly) {
      return;
    }
    this.setOpened(true);
  }

  public closePicker(): void {
    this.setOpened(false);
  }

  private setOpened(open: boolean): void {
    const next = open && !this.disabled && !this.readOnly;
    if (this.opened === next) {
      return;
    }
    this.opened = next;
  }

  private addDocumentListenersIfNeeded(): void {
    if (this.opened) {
      document.addEventListener('mousedown', this.handleDocumentPointerDown, true);
      document.addEventListener('touchstart', this.handleDocumentPointerDown, true);
      document.addEventListener('keydown', this.handleDocumentKeyDown, true);
    } else {
      document.removeEventListener('mousedown', this.handleDocumentPointerDown, true);
      document.removeEventListener('touchstart', this.handleDocumentPointerDown, true);
      document.removeEventListener('keydown', this.handleDocumentKeyDown, true);
    }
  }

  disconnectedCallback(): void {
    super.disconnectedCallback();
    this.opened = false;
    this.addDocumentListenersIfNeeded();
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'jalali-date-time-picker': JalaliDateTimePicker;
  }
}
