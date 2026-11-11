package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.RoleName;
import jakarta.validation.constraints.NotNull;

public record RoleRequest(@NotNull RoleName role) {}
