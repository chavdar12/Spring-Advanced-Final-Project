import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import Register from './register';

describe('Register', () => {
  let fixture: ComponentFixture<Register>;
  let http: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Register);
    await fixture.whenStable();
  });

  async function fill(values: string[]) {
    const inputs = Array.from(
      fixture.nativeElement.querySelectorAll('input'),
    ) as HTMLInputElement[];
    inputs.forEach((input, i) => {
      input.value = values[i];
      input.dispatchEvent(new Event('input'));
    });
    await fixture.whenStable();
  }

  const submitButton = () =>
    fixture.nativeElement.querySelector('button[type=submit]') as HTMLButtonElement;

  it('blocks mismatched passwords and invalid usernames', async () => {
    await fill(['ivan', 'ivan@mail.bg', 'secret1', 'secret2']);
    expect(submitButton().disabled).toBe(true);

    await fill(['Ivan', 'ivan@mail.bg', 'secret1', 'secret1']);
    expect(submitButton().disabled).toBe(true);
  });

  it('signs up without the confirmation field and goes to login', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    await fill(['ivan', 'ivan@mail.bg', 'secret1', 'secret1']);
    submitButton().click();
    TestBed.tick();

    const request = http.expectOne('/api/auth/signup');
    expect(request.request.body).toEqual({
      username: 'ivan',
      email: 'ivan@mail.bg',
      password: 'secret1',
    });
    request.flush(null, {status: 201, statusText: 'Created'});
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith(['/auth/signin']);
  });

  it('shows the server problem detail', async () => {
    await fill(['ivan', 'ivan@mail.bg', 'secret1', 'secret1']);
    submitButton().click();
    TestBed.tick();
    http
      .expectOne('/api/auth/signup')
      .flush({detail: 'Username is already taken'}, {status: 409, statusText: 'Conflict'});
    await vi.waitFor(() =>
      expect(fixture.nativeElement.querySelector('[role=alert]')).not.toBeNull(),
    );

    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain(
      'Username is already taken',
    );
  });
});
