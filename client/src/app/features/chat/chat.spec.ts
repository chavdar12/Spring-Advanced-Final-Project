import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {Session} from '../../core/session/session';
import {Client} from '@stomp/stompjs';
import {Chat, STOMP_CLIENT} from './chat';

const stomp = {activate: vi.fn(), deactivate: vi.fn(), connected: false};

describe('Chat', () => {
  // jsdom has no layout, so no scrolling
  beforeAll(() => (Element.prototype.scrollTo = () => undefined));

  it('lists people, opens the shared channel and marks it read', async () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: STOMP_CLIENT, useValue: () => stomp as unknown as Client},
      ],
    });
    TestBed.inject(Session).signIn({token: 't', username: 'maria', roles: []});
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(Chat);
    TestBed.tick();

    http
      .expectOne('/api/chat/users')
      .flush([
        {username: 'ivan', firstName: 'Ivan', lastName: 'Petrov', connected: true, photoUrl: null},
      ]);
    await fixture.whenStable();
    expect(stomp.activate).toHaveBeenCalled();

    (fixture.nativeElement.querySelector('mat-list-option') as HTMLElement).click();
    TestBed.tick();
    http.expectOne({method: 'POST', url: '/api/chat/channels/ivan.maria'}).flush(null);
    http
      .expectOne('/api/chat/channels/ivan.maria/messages')
      .flush([{channelId: 'ivan.maria', sender: 'ivan', content: 'Hi Maria'}]);
    http.expectOne({method: 'POST', url: '/api/chat/channels/ivan.maria/read'}).flush(null);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Hi Maria');
  });
});
