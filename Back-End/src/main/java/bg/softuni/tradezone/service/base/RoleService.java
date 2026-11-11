package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.service.RoleServiceModel;

import java.util.List;

public interface RoleService {

    void seed();

    boolean rolesAreSeeded();

    List<RoleServiceModel> getAllExceptRoot();
}
