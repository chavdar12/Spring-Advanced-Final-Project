package bg.softuni.tradezone.model.service;

import bg.softuni.tradezone.model.enums.RoleName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleServiceModel extends BaseServiceModel {

    private RoleName roleName;
}
