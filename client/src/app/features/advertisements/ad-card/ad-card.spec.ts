import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Session} from '../../../core/session/session';
import {AdSummary} from '../advertisement';
import {AdCard} from './ad-card';

const AD: AdSummary = {
  id: 7,
  title: 'Red bicycle',
  imageUrl: null,
  price: 120,
  condition: 'USED',
  description: '',
  views: 3,
  creator: 'seller',
  likedBy: ['someone'],
};

describe('AdCard', () => {
  let fixture: ComponentFixture<AdCard>;

  async function render(username: string) {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    TestBed.inject(Session).signIn({token: 't', username, roles: []});
    fixture = TestBed.createComponent(AdCard);
    fixture.componentRef.setInput('ad', AD);
    await fixture.whenStable();
  }

  const button = (label: string) =>
    fixture.nativeElement.querySelector(
      `button[aria-label="${label}"]`,
    ) as HTMLButtonElement | null;

  it('lets the creator edit and delete', async () => {
    await render('seller');
    const edit = vi.fn();
    fixture.componentInstance.edit.subscribe(edit);

    button('Edit')!.click();

    expect(edit).toHaveBeenCalledWith(AD);
    expect(button('Delete')).not.toBeNull();
    expect(button('Add to favorites')).toBeNull();
  });

  it('lets others like the advertisement and counts the like', async () => {
    await render('visitor');

    button('Add to favorites')!.click();
    TestBed.inject(HttpTestingController).expectOne('/api/advertisements/7/favorite').flush(null);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.likes').textContent).toBe('2');
    expect(button('Remove from favorites')!.getAttribute('aria-pressed')).toBe('true');
  });
});
