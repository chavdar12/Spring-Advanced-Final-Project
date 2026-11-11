import {DatePipe} from '@angular/common';
import {
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  linkedSignal,
  signal,
  viewChild,
} from '@angular/core';
import {form, FormField, FormRoot, required} from '@angular/forms/signals';
import {MatBadgeModule} from '@angular/material/badge';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {Client, IMessage} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {Notify} from '../../core/notify/notify';
import {Session} from '../../core/session/session';
import {ChatApi} from './chat-api';
import {channelId, ChatMessage, ChatUser} from './chat-message';

@Component({
  selector: 'app-chat',
  imports: [
    DatePipe,
    FormField,
    FormRoot,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatBadgeModule,
  ],
  templateUrl: './chat.html',
  styleUrl: './chat.scss',
})
export class Chat {
  protected readonly peer = signal<ChatUser | null>(null);
  protected readonly unread = signal<Record<string, number>>({});
  protected readonly filter = signal('');
  protected readonly visibleUsers = computed(() => {
    const query = this.filter().trim().toLowerCase();
    return this.users().filter((user) =>
      `${user.firstName} ${user.lastName}`.toLowerCase().includes(query),
    );
  });
  protected readonly draft = signal({content: ''});
  protected readonly draftForm = form(this.draft, (path) => required(path.content), {
    submission: {action: async () => this.send()},
  });
  private readonly chatApi = inject(ChatApi);
  private readonly notify = inject(Notify);
  private readonly session = inject(Session);
  protected readonly me = this.session.username()!;
  private readonly people = this.chatApi.users();
  /** Completed profiles, kept current by login/logout broadcasts. */
  protected readonly users = linkedSignal(() =>
    this.people.hasValue() ? this.people.value() : [],
  );
  private readonly channel = computed(() => {
    const peer = this.peer();
    return peer ? channelId(this.me, peer.username) : undefined;
  });
  private readonly history = this.chatApi.messages(this.channel);
  /** History of the open conversation plus the messages received since. */
  protected readonly messages = linkedSignal(() =>
    this.history.hasValue() ? this.history.value() : [],
  );
  private readonly scroller = viewChild<ElementRef<HTMLElement>>('scroller');
  private readonly listening = new Set<string>();
  private readonly stomp = new Client({
    webSocketFactory: () => new SockJS('/wechat'),
    connectHeaders: {Authorization: `Bearer ${this.session.token()}`},
    reconnectDelay: 5000,
    onConnect: () => {
      this.listening.clear();
      this.stomp.subscribe('/channel/login', (frame) => this.presence(frame, true));
      this.stomp.subscribe('/channel/logout', (frame) => this.presence(frame, false));
      this.users().forEach((user) => this.listen(user.username));
    },
  });

  constructor() {
    this.stomp.activate();
    inject(DestroyRef).onDestroy(() => void this.stomp.deactivate());
    // listen to every conversation once connected (onConnect covers reconnects)
    effect(() => {
      const users = this.users();
      if (this.stomp.connected) {
        users.forEach((user) => this.listen(user.username));
      }
    });
    effect(() => {
      if (this.messages().length) {
        this.scrollDown();
      }
    });
  }

  protected open(user: ChatUser): void {
    const id = channelId(this.me, user.username);
    this.peer.set(user);
    this.unread.update((unread) => ({...unread, [user.username]: 0}));
    this.chatApi.open(id).subscribe({error: () => undefined});
    this.chatApi.markRead(id).subscribe({error: () => undefined});
  }

  private send(): undefined {
    const peer = this.peer();
    if (!peer) {
      return undefined;
    }
    if (!this.stomp.connected) {
      this.notify.error('Chat is reconnecting, try again in a moment.');
      return undefined;
    }
    this.stomp.publish({
      destination: '/app/messages',
      body: JSON.stringify({
        channelId: channelId(this.me, peer.username),
        content: this.draft().content.trim(),
      }),
    });
    this.draft.set({content: ''});
    this.draftForm().reset();
    return undefined;
  }

  private listen(username: string): void {
    const id = channelId(this.me, username);
    if (this.listening.has(id)) {
      return;
    }
    this.listening.add(id);
    this.stomp.subscribe(`/channel/chat/${id}`, (frame) => {
      const message: ChatMessage = JSON.parse(frame.body);
      if (this.peer()?.username === username) {
        this.messages.update((all) => [...all, message]);
      } else if (message.sender !== this.me) {
        this.unread.update((unread) => ({...unread, [username]: (unread[username] ?? 0) + 1}));
        this.notify.ok(`New message from ${message.sender}`);
      }
    });
  }

  private presence(frame: IMessage, connected: boolean): void {
    const user: ChatUser = {...JSON.parse(frame.body), connected};
    if (user.username === this.me) {
      return;
    }
    this.users.update((all) => [...all.filter((u) => u.username !== user.username), user]);
    this.listen(user.username);
  }

  private scrollDown(): void {
    setTimeout(() => {
      const element = this.scroller()?.nativeElement;
      element?.scrollTo({top: element.scrollHeight});
    });
  }
}
