import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Session} from '../../../core/session/session';
import AdDetails from './ad-details';

describe('AdDetails', () => {
  it('shows the advertisement, counts the view and loads more from the seller', async () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    TestBed.inject(Session).signIn({token: 't', username: 'visitor', roles: []});
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(AdDetails);
    fixture.componentRef.setInput('id', 7);
    TestBed.tick();

    http.expectOne({method: 'POST', url: '/api/advertisements/7/views'}).flush(null);
    http.expectOne({method: 'GET', url: '/api/advertisements/7'}).flush({
      id: 7,
      title: 'Red bicycle',
      images: [],
      description: 'Barely used',
      price: 120,
      condition: 'USED',
      delivery: 'COURIER',
      views: 4,
      createdOn: '2026-01-01T10:00:00',
      categoryId: 1,
      categoryName: 'Vehicles',
      seller: {
        username: 'seller',
        firstName: 'Ivan',
        lastName: 'Ivanov',
        aboutMe: '',
        photoUrl: '',
      },
    });
    // the seller's ads are requested once the details response has been applied
    (
      await vi.waitFor(() => http.expectOne('/api/advertisements/by/seller?page=0&excludeId=7'))
    ).flush([]);
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Red bicycle');
    expect(text).toContain('Ivan Ivanov');
    expect(text).toContain('Nothing else yet.');
    expect(fixture.nativeElement.querySelector('.owner-actions')).toBeNull();
    http.verify();
  });
});
