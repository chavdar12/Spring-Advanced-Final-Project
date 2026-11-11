package bg.softuni.tradezone.model.rest.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseSearch {

    private BigDecimal min;

    private BigDecimal max;
}
