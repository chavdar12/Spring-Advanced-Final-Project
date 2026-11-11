import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import {Session} from '../../../core/session/session';
import Login from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Login);
    await fixture.whenStable();
  });

  async function submit(username: string, password: string) {
    const el: HTMLElement = fixture.nativeElement;
    const [user, pass] = Array.from(el.querySelectorAll('input'));
    user.value = username;
    user.dispatchEvent(new Event('input'));
    pass.value = password;
    pass.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    el.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
  }

  it('keeps submit disabled until both fields are filled', async () => {
    const button = fixture.nativeElement.querySelector('button[type=submit]') as HTMLButtonElement;
    expect(button.disabled).toBe(true);

    await submit('ivan', '');
    expect(button.disabled).toBe(true);
    http.expectNone('/api/auth/signin');
  });

  it('signs in and navigates home', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    await submit('ivan', 'secret');
    const request = http.expectOne('/api/auth/signin');
    expect(request.request.body).toEqual({username: 'ivan', password: 'secret'});
    request.flush({token: 't', username: 'ivan', roles: ['ROLE_USER']});
    await fixture.whenStable();

    expect(TestBed.inject(Session).username()).toBe('ivan');
    expect(navigate).toHaveBeenCalledWith(['/home']);
  });

  it('shows a friendly error on bad credentials', async () => {
    await submit('ivan', 'wrong');
    http.expectOne('/api/auth/signin').flush(null, {status: 401, statusText: 'Unauthorized'});
    await vi.waitFor(() =>
      expect(fixture.nativeElement.querySelector('[role=alert]')).not.toBeNull(),
    );

    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain(
      'Wrong username or password',
    );
  });
});
