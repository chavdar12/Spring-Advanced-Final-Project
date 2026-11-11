import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {Session} from '../../core/session/session';
import Admin from './admin';

describe('Admin', () => {
  it('offers role changes only where the signed-in admin is allowed to make them', async () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    TestBed.inject(Session).signIn({token: 't', username: 'boss', roles: ['ROLE_ADMIN']});
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(Admin);
    TestBed.tick();

    http.expectOne('/api/admin/roles').flush(['ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN']);
    http.expectOne('/api/admin/users').flush([
      {id: 1, username: 'root', photoUrl: null, role: 'ROLE_ROOT'},
      {id: 2, username: 'boss', photoUrl: null, role: 'ROLE_ADMIN'},
      {id: 3, username: 'peer', photoUrl: null, role: 'ROLE_ADMIN'},
      {id: 4, username: 'ivan', photoUrl: null, role: 'ROLE_USER'},
    ]);
    await fixture.whenStable();

    const selects = Array.from(
      fixture.nativeElement.querySelectorAll('td mat-select') as NodeListOf<HTMLElement>,
    ).map((select) => select.getAttribute('aria-label'));
    expect(selects).toEqual(['Role of ivan']);
  });
});
