import {canChangeRole} from './admin-user';

describe('canChangeRole', () => {
  it('lets nobody touch root and only root touch admins', () => {
    expect(canChangeRole('ROLE_ROOT', 'ROLE_ROOT')).toBe(false);
    expect(canChangeRole('ROLE_ROOT', 'ROLE_ADMIN')).toBe(true);
    expect(canChangeRole('ROLE_ADMIN', 'ROLE_ADMIN')).toBe(false);
    expect(canChangeRole('ROLE_ADMIN', 'ROLE_USER')).toBe(true);
    expect(canChangeRole(undefined, 'ROLE_USER')).toBe(true);
  });
});
