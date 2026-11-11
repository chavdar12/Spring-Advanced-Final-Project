package bg.softuni.tradezone.dto;

import java.util.List;

public record TopCategoryResponse(
    String name, String photoUrl, List<AdvertisementSummary> advertisements) {}
