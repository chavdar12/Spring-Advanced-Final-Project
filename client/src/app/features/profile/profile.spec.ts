import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import Profile from './profile';
import {UserProfile} from './user-profile';

const PROFILE: UserProfile = {
  id: 1,
  username: 'ivan',
  email: 'ivan@mail.bg',
  firstName: 'Ivan',
  lastName: 'Petrov',
  town: {id: 3, name: 'Варна', regionName: 'Варна'},
  aboutMe: 'Hello there',
  completed: false,
  photoUrl: null,
  roles: ['ROLE_USER', 'ROLE_MODERATOR'],
  subscribedTo: [],
  createdAdvertisements: [],
  favorites: [],
};

describe('Profile', () => {
  let fixture: ComponentFixture<Profile>;
  let http: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Profile);
    TestBed.tick();
    http.expectOne('/api/towns').flush([{id: 3, name: 'Варна', regionName: 'Варна'}]);
    http.expectOne('/api/profile').flush(PROFILE);
    await fixture.whenStable();
  });

  it('shows the overview with the highest role and a completion hint', () => {
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Ivan Petrov');
    expect(text).toContain('ivan@mail.bg · moderator');
    expect(text).toContain('Add your name, town and a profile picture');
  });

  it('saves the info form prefilled from the profile', async () => {
    fixture.componentInstance['tab'].set(1);
    // tab content renders lazily
    const save = await vi.waitFor(() => {
      const button = Array.from(
        fixture.nativeElement.querySelectorAll(
          'button[type=submit]',
        ) as NodeListOf<HTMLButtonElement>,
      ).find((b) => b.textContent?.includes('Save'));
      expect(button).toBeDefined();
      return button!;
    });
    expect(save.disabled).toBe(false);
    save.click();
    TestBed.tick();

    const request = http.expectOne({method: 'PATCH', url: '/api/profile'});
    expect(request.request.body).toEqual({
      firstName: 'Ivan',
      lastName: 'Petrov',
      townId: 3,
      aboutMe: 'Hello there',
    });
    request.flush({...PROFILE, completed: true});
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).not.toContain('Add your name, town');
  });
});
