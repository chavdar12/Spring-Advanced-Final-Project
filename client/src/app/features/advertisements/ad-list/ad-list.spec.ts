import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Session} from '../../../core/session/session';
import AdList from './ad-list';

describe('AdList', () => {
  let fixture: ComponentFixture<AdList>;
  let http: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    TestBed.inject(Session).signIn({token: 't', username: 'ivan', roles: ['ROLE_USER']});
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AdList);
    fixture.componentRef.setInput('category', 'All');
    TestBed.tick();

    http.expectOne('/api/categories').flush([]);
    http.expectOne('/api/advertisements/options').flush({conditions: ['NEW'], deliveries: []});
    http.expectOne('/api/profile').flush({completed: false});
  });

  const searchRequest = () =>
    http.expectOne((request) => request.url === '/api/advertisements' && request.method === 'GET');

  it('leaves "All" filters out of the query and shows the total', async () => {
    const request = searchRequest();
    expect(request.request.params.keys().sort()).toEqual(['max', 'min', 'order', 'page']);
    request.flush({content: [], total: 0});
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('0 results');
    expect(fixture.nativeElement.textContent).toContain('Complete your profile');
  });

  it('searches by title and category from the first page', async () => {
    searchRequest().flush({content: [], total: 0});
    fixture.componentRef.setInput('category', 'Vehicles');
    const box = fixture.nativeElement.querySelector('input[matinput]') as HTMLInputElement;
    box.value = 'bike';
    box.dispatchEvent(new Event('change'));
    TestBed.tick();

    const params = searchRequest().request.params;
    expect(params.get('search')).toBe('bike');
    expect(params.get('category')).toBe('Vehicles');
    expect(params.get('page')).toBe('1');
  });
});
