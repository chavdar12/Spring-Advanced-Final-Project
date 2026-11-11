import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {MatDialogRef} from '@angular/material/dialog';
import {CategoryDialog} from './category-dialog';

describe('CategoryDialog', () => {
  it('needs a name and an image before creating', async () => {
    const close = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: MatDialogRef, useValue: {close}},
      ],
    });
    const fixture = TestBed.createComponent(CategoryDialog);
    await fixture.whenStable();
    const el: HTMLElement = fixture.nativeElement;
    const create = el.querySelector('button[type=submit]') as HTMLButtonElement;
    expect(create.disabled).toBe(true);

    const name = el.querySelector('input[matinput]') as HTMLInputElement;
    name.value = 'Books';
    name.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect(create.disabled).toBe(true);

    const file = new File(['x'], 'books.png', {type: 'image/png'});
    const picker = el.querySelector('input[type=file]') as HTMLInputElement;
    Object.defineProperty(picker, 'files', {value: [file]});
    picker.dispatchEvent(new Event('change'));
    await vi.waitFor(() => expect(el.querySelector('img')).not.toBeNull(), {timeout: 1000});
    await fixture.whenStable();
    expect(create.disabled).toBe(false);

    create.click();
    TestBed.tick();
    const request = TestBed.inject(HttpTestingController).expectOne('/api/categories');
    expect(request.request.body.name).toBe('Books');
    expect(request.request.body.image).toMatch(/^data:image\/png;base64,/);
    request.flush({id: 2, name: 'Books', photoUrl: ''});
    await fixture.whenStable();

    expect(close).toHaveBeenCalledWith(true);
  });
});
