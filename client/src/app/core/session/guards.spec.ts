import {TestBed} from '@angular/core/testing';
import {provideRouter, UrlTree} from '@angular/router';
import {adminGuard, anonymousGuard, authGuard} from './guards';
import {Session} from './session';

const run = (guard: typeof authGuard) =>
  TestBed.runInInjectionContext(() => guard({}, [], {} as never));

describe('guards', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({providers: [provideRouter([])]});
  });

  it('anonymous users are sent to sign in, and may open auth pages', () => {
    expect((run(authGuard) as UrlTree).toString()).toBe('/auth/signin');
    expect(run(anonymousGuard)).toBe(true);
  });

  it('only admins pass the admin guard', () => {
    const session = TestBed.inject(Session);
    session.signIn({token: 't', username: 'u', roles: ['ROLE_USER']});
    expect(run(authGuard)).toBe(true);
    expect((run(adminGuard) as UrlTree).toString()).toBe('/home');

    session.signIn({token: 't', username: 'u', roles: ['ROLE_ADMIN']});
    expect(run(adminGuard)).toBe(true);
  });
});
