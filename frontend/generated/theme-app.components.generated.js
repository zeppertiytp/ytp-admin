import { unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin/register-styles';

import vaadinDetailsCss from 'themes/app/components/vaadin-details.css?inline';


if (!document['_vaadintheme_app_componentCss']) {
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

