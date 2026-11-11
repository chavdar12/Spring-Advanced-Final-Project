import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {catchError, finalize, retry, throwError, timer} from 'rxjs';
import {Session} from '../session/session';
import {Loading} from './loading';

const UNSAFE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);
const RETRYABLE_STATUSES = new Set([0, 502, 503, 504]);

/** Bearer token on every call; an expired or invalid token ends the session. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(Session);
  const router = inject(Router);
  const token = session.token();
  const authed = token ? req.clone({setHeaders: {Authorization: `Bearer ${token}`}}) : req;
  return next(authed).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && token) {
        session.signOut();
        void router.navigate(['/auth/signin']);
      }
      return throwError(() => error);
    }),
  );
};

/**
 * Unsafe requests get one Idempotency-Key for their whole lifetime, so the transient-failure
 * retries below are safe: the server replays the first result instead of executing twice.
 */
export const idempotencyInterceptor: HttpInterceptorFn = (req, next) => {
  if (!UNSAFE_METHODS.has(req.method)) {
    return next(req);
  }
  const keyed = req.headers.has('Idempotency-Key')
    ? req
    : req.clone({setHeaders: {'Idempotency-Key': crypto.randomUUID()}});
  return next(keyed).pipe(
    retry({
      count: 2,
      delay: (error: HttpErrorResponse, attempt) =>
        RETRYABLE_STATUSES.has(error.status) ? timer(300 * 2 ** attempt) : throwError(() => error),
    }),
  );
};

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(Loading);
  loading.start();
  return next(req).pipe(finalize(() => loading.stop()));
};
