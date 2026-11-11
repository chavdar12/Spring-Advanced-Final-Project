import {errorMessage} from './problem';

describe('errorMessage', () => {
  it('prefers field errors, then the detail, then explains rate limiting', () => {
    expect(errorMessage({error: {detail: 'Nope', errors: {title: 'must not be blank'}}})).toBe(
      'title must not be blank',
    );
    expect(errorMessage({error: {detail: 'Nope'}})).toBe('Nope');
    expect(errorMessage({status: 429})).toContain('Too many requests');
    expect(errorMessage(null)).toBe('Something went wrong.');
  });
});
