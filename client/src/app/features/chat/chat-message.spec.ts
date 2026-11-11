import {channelId} from './chat-message';

describe('channelId', () => {
  it('is the same for both participants', () => {
    expect(channelId('maria', 'ivan')).toBe(channelId('ivan', 'maria'));
    expect(channelId('ivan', 'maria')).toBe('ivan.maria');
  });
});
