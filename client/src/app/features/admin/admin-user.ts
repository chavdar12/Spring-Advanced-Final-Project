export interface AdminUser {
  id: number;
  username: string;
  photoUrl: string | null;
  role: string;
}

/** Nobody touches root; only root touches admins. Roles are "ROLE_*". */
export function canChangeRole(myRole: string | undefined, theirRole: string): boolean {
  return theirRole !== 'ROLE_ROOT' && (myRole === 'ROLE_ROOT' || theirRole !== 'ROLE_ADMIN');
}
