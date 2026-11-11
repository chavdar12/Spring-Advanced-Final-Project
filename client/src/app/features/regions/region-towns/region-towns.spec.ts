import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {RegionTowns} from './region-towns';

describe('RegionTowns', () => {
  it('lists the towns of its region with their counts', async () => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    const fixture = TestBed.createComponent(RegionTowns);
    fixture.componentRef.setInput('region', 'Варна');
    TestBed.tick();
    expect(fixture.nativeElement.textContent).toContain('Loading…');

    TestBed.inject(HttpTestingController)
      .expectOne(`/api/regions/${encodeURIComponent('Варна')}/towns`)
      .flush([{id: 1, name: 'Аксаково', advertisementsCount: 2}]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Аксаково (2)');
  });
});
