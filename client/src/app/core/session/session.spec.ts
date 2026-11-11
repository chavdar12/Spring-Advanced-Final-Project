import {TestBed} from '@angular/core/testing';
import {Session} from './session';

describe('Session', () => {
  beforeEach(() => sessionStorage.clear());

  it('stores the token and roles', () => {
    const session = TestBed.inject(Session);
    session.signIn({token: 't', username: 'ivan', roles: ['ROLE_USER', 'ROLE_ADMIN']});

    expect(session.loggedIn()).toBe(true);
    expect(session.isAdmin()).toBe(true);
    expect(session.isModerator()).toBe(false);
    expect(JSON.parse(sessionStorage.getItem('trade-zone.session')!).token).toBe('t');
  });

  it('signOut clears state and storage', () => {
    const session = TestBed.inject(Session);
    session.signIn({token: 't', username: 'ivan', roles: []});
    session.signOut();

    expect(session.loggedIn()).toBe(false);
    expect(sessionStorage.length).toBe(0);
  });

  it('survives corrupt storage', () => {
    sessionStorage.setItem('trade-zone.session', '{not json');
    expect(TestBed.inject(Session).loggedIn()).toBe(false);
  });
});
