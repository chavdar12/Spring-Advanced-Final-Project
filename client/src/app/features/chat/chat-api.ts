import {HttpClient, httpResource} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {ChatMessage, ChatUser} from './chat-message';

const BASE = '/api/chat';

@Injectable({providedIn: 'root'})
export class ChatApi {
  private readonly http = inject(HttpClient);

  users() {
    return httpResource<ChatUser[]>(() => `${BASE}/users`, {defaultValue: []});
  }

  /** History of a channel; no request while {@code channelId} is undefined. */
  messages(channelId: () => string | undefined) {
    return httpResource<ChatMessage[]>(
      () => {
        const id = channelId();
        return id && `${BASE}/channels/${id}/messages`;
      },
      {defaultValue: []},
    );
  }

  /** Idempotent: creates the channel and subscribes only when needed. */
  open(channelId: string) {
    return this.http.post<void>(`${BASE}/channels/${channelId}`, null);
  }

  markRead(channelId: string) {
    return this.http.post<void>(`${BASE}/channels/${channelId}/read`, null);
  }
}
