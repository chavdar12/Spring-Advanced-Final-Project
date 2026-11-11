import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import {App} from './app';
import {Session} from './core/session/session';

describe('App', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('offers login and sign up to visitors', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Sign up');
    expect(fixture.nativeElement.textContent).not.toContain('Logout');
  });

  it('logs out: tells the server, ends the session and goes to login', async () => {
    TestBed.inject(Session).signIn({token: 't', username: 'ivan', roles: ['ROLE_ADMIN']});
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Admin');

    const logout = Array.from(
      fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>,
    ).find((button) => button.textContent?.includes('Logout'))!;
    logout.click();
    await fixture.whenStable();

    TestBed.inject(HttpTestingController).expectOne('/api/profile/disconnect').flush(null);
    expect(TestBed.inject(Session).loggedIn()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/auth/signin']);
  });
});
