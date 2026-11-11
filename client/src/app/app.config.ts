import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {ApplicationConfig, provideBrowserGlobalErrorListeners} from '@angular/core';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import {
  authInterceptor,
  idempotencyInterceptor,
  loadingInterceptor,
} from './core/http/interceptors';
import {routes} from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    // order matters: auth must wrap retries so each retry carries the token
    provideHttpClient(
      withInterceptors([loadingInterceptor, authInterceptor, idempotencyInterceptor]),
    ),
  ],
};
