import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import Regions from './regions';

describe('Regions', () => {
  it('shows region totals and loads towns only when a region is opened', async () => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(Regions);
    TestBed.tick();
    http.expectOne('/api/regions').flush([{name: 'Варна', totalAds: 2, totalUsers: 1}]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('2 ads · 1 users');
    http.expectNone((request) => request.url.endsWith('/towns'));

    (fixture.nativeElement.querySelector('mat-expansion-panel-header') as HTMLElement).click();
    TestBed.tick();
    http.expectOne(`/api/regions/${encodeURIComponent('Варна')}/towns`).flush([]);
  });
});
