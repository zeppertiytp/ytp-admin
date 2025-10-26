import { unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin/register-styles';

import vaadinNotificationCardCss from 'themes/app/components/vaadin-notification-card.css?inline';
import vaadinLoginOverlayCss from 'themes/app/components/vaadin-login-overlay.css?inline';
import vaadinDetailsCss from 'themes/app/components/vaadin-details.css?inline';


if (!document['_vaadintheme_app_componentCss']) {
  registerStyles(
        'vaadin-notification-card',
        unsafeCSS(vaadinNotificationCardCss.toString())
      );
      registerStyles(
        'vaadin-login-overlay',
        unsafeCSS(vaadinLoginOverlayCss.toString())
      );
      registerStyles(
        'vaadin-details',
        unsafeCSS(vaadinDetailsCss.toString())
      );
      
  document['_vaadintheme_app_componentCss'] = true;
}

if (import.meta.hot) {
  import.meta.hot.accept((module) => {
    window.location.reload();
  });
}

