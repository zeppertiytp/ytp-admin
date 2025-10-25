/// <reference path="../types/assets.d.ts" />
import { LitElement, html } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import * as L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// @ts-ignore - icon assets
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
// @ts-ignore
import markerIcon from 'leaflet/dist/images/marker-icon.png';
// @ts-ignore
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
(L.Icon.Default as any).mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow
});

@customElement('location-picker')
export class LocationPicker extends LitElement {
  private map?: L.Map;
  private marker?: L.Marker;
  private resizeObserver?: ResizeObserver;
  private searchController?: AbortController;

  @property({ type: String })
  searchPlaceholder = '';

  @property({ type: String })
  searchLabel = '';

  @property({ type: String })
  searchingLabel = '';

  @property({ type: String })
  noResultsLabel = '';

  @property({ type: String })
  fetchErrorLabel = '';

  @property({ type: String })
  noSelectionLabel = '';

  @property({ type: String })
  selectedLabel = '';

  @state()
  private selectedLat: number | null = null;

  @state()
  private selectedLng: number | null = null;

  @state()
  private searchQuery = '';

  @state()
  private searching = false;

  @state()
  private searchError: string | null = null;

  @state()
  private searchResults: Array<{ displayName: string; lat: number; lng: number }> = [];

  createRenderRoot() { return this; }

  firstUpdated() {
    const mapDiv = this.querySelector('#map') as HTMLElement | null;
    if (!mapDiv) {
      return;
    }

    this.map = L.map(mapDiv).setView([35.6892, 51.3890], 11);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);

    this.map.on('click', (ev: L.LeafletMouseEvent) => {
      this.setLocation(ev.latlng.lat, ev.latlng.lng);
    });

    this.resizeObserver = new ResizeObserver(() => {
      this.map?.invalidateSize();
    });
    this.resizeObserver.observe(this);

    setTimeout(() => {
      this.map?.invalidateSize();
    }, 0);
  }

  render() {
    return html`
      <style>
        .picker-container {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
          width: 100%;
        }

        .search-bar {
          display: flex;
          gap: 0.5rem;
          align-items: center;
        }

        .search-bar input[type="text"] {
          flex: 1;
          padding: 0.35rem 0.5rem;
          border: 1px solid var(--lumo-contrast-30pct, #c3c3c3);
          border-radius: 4px;
          font: inherit;
        }

        .search-bar button {
          padding: 0.35rem 0.75rem;
          border-radius: 4px;
          border: none;
          background: var(--lumo-primary-color, #2e7d32);
          color: white;
          font-weight: 600;
          cursor: pointer;
        }

        .search-bar button[disabled] {
          opacity: 0.6;
          cursor: default;
        }

        .results {
          list-style: none;
          padding: 0;
          margin: 0;
          border: 1px solid var(--lumo-contrast-30pct, #c3c3c3);
          border-radius: 4px;
          max-height: 160px;
          overflow-y: auto;
        }

        .results li button {
          width: 100%;
          padding: 0.5rem;
          background: white;
          border: none;
          text-align: left;
          cursor: pointer;
        }

        .results li button:hover,
        .results li button:focus {
          background: var(--lumo-contrast-10pct, #f0f0f0);
        }

        .status {
          font-size: 0.9rem;
          color: var(--lumo-secondary-text-color, #555);
        }

        .error {
          color: var(--lumo-error-text-color, #b00020);
          font-size: 0.85rem;
        }

        #map {
          height: 500px;
          width: 100%;
        }
      </style>
      <div class="picker-container">
        <div class="search-bar">
          <input
            type="text"
            placeholder=${this.searchPlaceholder}
            .value=${this.searchQuery}
            @input=${this.handleSearchInput}
            @keydown=${this.handleSearchKeydown}
          />
          <button @click=${this.performSearch} ?disabled=${this.searching || this.searchQuery.trim().length < 3}>
            ${this.searching ? this.searchingLabel : this.searchLabel}
          </button>
        </div>
        ${this.searchError ? html`<div class="error">${this.searchError}</div>` : ''}
        ${this.searchResults.length > 0
          ? html`<ul class="results">
              ${this.searchResults.map(
                res => html`<li>
                  <button type="button" @click=${() => this.handleResultSelect(res)}>
                    ${res.displayName}
                  </button>
                </li>`
              )}
            </ul>`
          : ''}
        <div class="status">
          ${this.selectedLat !== null && this.selectedLng !== null
            ? html`${this.selectedLabel} ${this.selectedLat.toFixed(5)}, ${this.selectedLng.toFixed(5)}`
            : this.noSelectionLabel}
        </div>
        <div id="map"></div>
      </div>
    `;
  }

  disconnectedCallback(): void {
    super.disconnectedCallback();
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
      this.resizeObserver = undefined;
    }
    if (this.searchController) {
      this.searchController.abort();
      this.searchController = undefined;
    }
  }

  private handleSearchInput(ev: Event) {
    this.searchQuery = (ev.target as HTMLInputElement).value;
  }

  private handleSearchKeydown(ev: KeyboardEvent) {
    if (ev.key === 'Enter') {
      ev.preventDefault();
      this.performSearch();
    }
  }

  private async performSearch() {
    const query = this.searchQuery.trim();
    if (query.length < 3 || this.searching) {
      return;
    }

    if (this.searchController) {
      this.searchController.abort();
    }
    this.searchController = new AbortController();

    this.searching = true;
    this.searchError = null;
    this.searchResults = [];

    try {
      const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=5`;
      const response = await fetch(url, {
        headers: {
          Accept: 'application/json'
        },
        signal: this.searchController.signal
      });

      if (!response.ok) {
        throw new Error();
      }

      const payload = (await response.json()) as Array<{ lat: string; lon: string; display_name: string }>;
      const mapped = payload
        .map(item => ({
          displayName: item.display_name,
          lat: Number.parseFloat(item.lat),
          lng: Number.parseFloat(item.lon)
        }))
        .filter(item => Number.isFinite(item.lat) && Number.isFinite(item.lng));

      this.searchResults = mapped;
      if (mapped.length === 0) {
        this.searchError = this.noResultsLabel;
      }
    } catch (err: any) {
      if (err?.name === 'AbortError') {
        return;
      }
      this.searchError = this.fetchErrorLabel;
    } finally {
      this.searching = false;
    }
  }

  private handleResultSelect(result: { displayName: string; lat: number; lng: number }) {
    this.searchQuery = result.displayName;
    this.searchResults = [];
    this.focusMapOn(result.lat, result.lng);
  }

  private focusMapOn(lat: number, lng: number) {
    if (!this.map) {
      return;
    }

    const targetZoom = Math.max(this.map.getZoom(), 13);
    this.map.setView([lat, lng], targetZoom);
  }

  private setLocation(lat: number, lng: number) {
    if (!this.map) {
      return;
    }

    if (!this.marker) {
      this.marker = L.marker([lat, lng], { draggable: true }).addTo(this.map);
      this.marker.on('dragend', () => {
        const pos = this.marker?.getLatLng();
        if (pos) {
          this.updateSelected(pos.lat, pos.lng);
        }
      });
    } else {
      this.marker.setLatLng([lat, lng]);
    }

    this.map.setView([lat, lng], Math.max(this.map.getZoom(), 13));
    this.updateSelected(lat, lng);
  }

  private updateSelected(lat: number, lng: number) {
    this.selectedLat = lat;
    this.selectedLng = lng;
    // @ts-ignore Flow bridge
    if (this.$server && typeof this.$server.setLocation === 'function') {
      // @ts-ignore Flow bridge
      this.$server.setLocation(lat, lng);
    }
  }
}
