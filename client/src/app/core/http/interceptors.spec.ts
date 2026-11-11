import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import {authInterceptor, idempotencyInterceptor} from './interceptors';
import {Session} from '../session/session';

describe('http interceptors', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let session: Session;

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor, idempotencyInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    session = TestBed.inject(Session);
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  it('adds the bearer token when signed in', () => {
    session.signIn({token: 'abc', username: 'u', roles: []});
    http.get('/api/x').subscribe();
    expect(backend.expectOne('/api/x').request.headers.get('Authorization')).toBe('Bearer abc');
  });

  it('adds no Idempotency-Key to safe requests', () => {
    http.get('/api/x').subscribe();
    expect(backend.expectOne('/api/x').request.headers.has('Idempotency-Key')).toBe(false);
  });

  it('retries a 503 with the same Idempotency-Key, then succeeds', async () => {
    let result: unknown;
    http.post('/api/create', {}).subscribe((r) => (result = r));

    const first = backend.expectOne('/api/create');
    const key = first.request.headers.get('Idempotency-Key');
    expect(key).toBeTruthy();
    first.flush('down', {status: 503, statusText: 'Unavailable'});

    await vi.advanceTimersByTimeAsync(1000);
    const second = backend.expectOne('/api/create');
    expect(second.request.headers.get('Idempotency-Key')).toBe(key);
    second.flush({ok: true});

    expect(result).toEqual({ok: true});
  });

  it('does not retry client errors', () => {
    let failed = false;
    http.post('/api/create', {}).subscribe({error: () => (failed = true)});
    backend
      .expectOne('/api/create')
      .flush({detail: 'bad'}, {status: 400, statusText: 'Bad Request'});
    expect(failed).toBe(true);
  });

  it('signs out and redirects on 401 with a token', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    session.signIn({token: 'expired', username: 'u', roles: []});
    http.get('/api/x').subscribe({error: () => undefined});
    backend.expectOne('/api/x').flush(null, {status: 401, statusText: 'Unauthorized'});

    expect(session.loggedIn()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/auth/signin']);
  });
});
