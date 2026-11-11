package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.model.entity.Region;
import bg.softuni.tradezone.model.entity.Town;
import bg.softuni.tradezone.repository.RegionRepository;
import bg.softuni.tradezone.repository.TownRepository;
import bg.softuni.tradezone.utils.FileUtilImpl;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeedingServiceImplTests {

    @Mock
    RegionRepository regionRepository;

    @Mock
    TownRepository townRepository;

    SeedingServiceImpl seedingService;

    @BeforeEach
    void init() {
        seedingService = new SeedingServiceImpl(regionRepository, townRepository, new FileUtilImpl(), new Gson(), new ModelMapper());
    }

    @Test
    @SuppressWarnings("unchecked")
    void seedIfNeeded_readsBundledJson_whenDatabaseIsEmpty() {
        Region burgas = new Region();
        burgas.setName("Бургас");
        when(regionRepository.count()).thenReturn(0L);
        when(townRepository.count()).thenReturn(0L);
        when(regionRepository.findByName(anyString())).thenReturn(Optional.of(burgas));

        seedingService.seedIfNeeded();

        ArgumentCaptor<List<Region>> regions = ArgumentCaptor.forClass(List.class);
        verify(regionRepository).saveAll(regions.capture());
        assertEquals(28, regions.getValue().size());
        assertTrue(regions.getValue().stream().anyMatch(r -> "Бургас".equals(r.getName())), "UTF-8 names must survive");

        ArgumentCaptor<List<Town>> towns = ArgumentCaptor.forClass(List.class);
        verify(townRepository).saveAll(towns.capture());
        assertFalse(towns.getValue().isEmpty());
        assertTrue(towns.getValue().stream().allMatch(t -> t.getRegion() == burgas));
    }

    @Test
    void seedIfNeeded_doesNothing_whenAlreadySeeded() {
        when(regionRepository.count()).thenReturn(28L);
        when(townRepository.count()).thenReturn(250L);

        seedingService.seedIfNeeded();

        verify(regionRepository, never()).saveAll(any());
        verify(townRepository, never()).saveAll(any());
    }
}
