import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AdDetails} from '../advertisement';
import {AdFormDialog} from './ad-form-dialog';

const DETAILS = {
  id: 7,
  title: 'Red bicycle',
  description: 'Barely used',
  price: 120,
  condition: 'USED',
  delivery: 'COURIER',
  categoryId: 1,
} as AdDetails;

describe('AdFormDialog', () => {
  it('loads the advertisement and saves only the editable fields', async () => {
    const close = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: MAT_DIALOG_DATA, useValue: 7},
        {provide: MatDialogRef, useValue: {close}},
      ],
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(AdFormDialog);
    TestBed.tick();

    http
      .expectOne('/api/advertisements/options')
      .flush({conditions: ['USED'], deliveries: ['COURIER']});
    http.expectOne('/api/categories').flush([{id: 1, name: 'Vehicles', photoUrl: ''}]);
    http.expectOne('/api/advertisements/7').flush(DETAILS);
    await fixture.whenStable();

    const title = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    expect(title.value).toBe('Red bicycle');

    title.value = 'Blue bicycle';
    title.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    (fixture.nativeElement.querySelector('button[type=submit]') as HTMLButtonElement).click();
    TestBed.tick();

    const request = http.expectOne('/api/advertisements/7');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({...DETAILS, id: undefined, title: 'Blue bicycle'});
    request.flush(DETAILS);
    await fixture.whenStable();

    expect(close).toHaveBeenCalledWith(true);
  });
});
