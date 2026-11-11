package bg.softuni.tradezone.entity;

/** Ordered from least to most privileged; each role implies the ones before it. */
public enum RoleName {
  ROLE_USER,
  ROLE_MODERATOR,
  ROLE_ADMIN,
  ROLE_ROOT
}
