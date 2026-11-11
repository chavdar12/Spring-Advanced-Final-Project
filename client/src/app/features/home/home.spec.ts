import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Session} from '../../core/session/session';
import Home from './home';

describe('Home', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('welcomes visitors without calling the API', async () => {
    const fixture = TestBed.createComponent(Home);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Get started');
    TestBed.inject(HttpTestingController).expectNone('/api/categories/top?count=3');
  });

  it('shows top categories with the most liked advertisements first', async () => {
    TestBed.inject(Session).signIn({token: 't', username: 'ivan', roles: []});
    const fixture = TestBed.createComponent(Home);
    TestBed.tick();

    const ad = (id: number, likedBy: string[]) => ({
      id,
      title: `Ad ${id}`,
      likedBy,
      imageUrl: null,
    });
    TestBed.inject(HttpTestingController)
      .expectOne('/api/categories/top?count=3')
      .flush([{name: 'Vehicles', photoUrl: '', advertisements: [ad(1, []), ad(2, ['a', 'b'])]}]);
    await fixture.whenStable();

    const titles = Array.from(
      fixture.nativeElement.querySelectorAll('mat-card-title') as NodeListOf<HTMLElement>,
    ).map((title) => title.textContent?.trim());
    expect(fixture.nativeElement.textContent).toContain('Top Vehicles');
    expect(titles).toEqual(['Ad 2', 'Ad 1']);
  });
});
