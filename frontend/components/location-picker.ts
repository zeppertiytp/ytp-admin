/// <reference path="../types/assets.d.ts" />
import { LitElement, html } from 'lit';
import { customElement } from 'lit/decorators.js';
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
  createRenderRoot() { return this; }

  firstUpdated() {
    const mapDiv = this.querySelector('#map') as HTMLElement;
    const map = L.map(mapDiv).setView([35.6892, 51.3890], 11);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(map);

    let marker: L.Marker | null = null;

    const setLoc = (lat: number, lng: number) => {
      if (marker) {
        marker.setLatLng([lat, lng]);
      } else {
        marker = L.marker([lat, lng], { draggable: true }).addTo(map);
        marker.on('dragend', () => {
          const pos = marker!.getLatLng();
          // @ts-ignore Flow bridge
          this.$server.setLocation(pos.lat, pos.lng);
        });
      }
      // @ts-ignore Flow bridge
      this.$server.setLocation(lat, lng);
    };

    map.on('click', (ev: any) => setLoc(ev.latlng.lat, ev.latlng.lng));
  }

  render() {
    return html`<div id="map" style="height: 500px; width: 100%"></div>`;
  }
}
