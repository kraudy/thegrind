//import { bootstrapApplication } from '@angular/platform-browser';
//import { appConfig } from './app/app.config';
//import { App } from './app/app';

import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app/app.routes';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient()  // Reemplaza HttpClientModule
  ]
}).catch(err => console.error(err));

//bootstrapApplication(App, appConfig)
//  .catch((err) => console.error(err));
