export interface ChatUser {
  username: string;
  firstName: string;
  lastName: string;
  connected: boolean;
  photoUrl: string | null;
}

export interface ChatMessage {
  channelId: string;
  sender: string;
  content: string;
  timestamp?: string;
  readDate?: string;
  senderAvatarUrl?: string;
}

/** Deterministic channel id shared by both participants. */
export function channelId(a: string, b: string): string {
  return [a, b].sort().join('.');
}
