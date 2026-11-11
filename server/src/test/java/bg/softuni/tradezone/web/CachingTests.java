package bg.softuni.tradezone.web;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bg.softuni.tradezone.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.data.redis.cache.RedisCacheManager;

class CachingTests extends IntegrationTest {

  @Autowired CacheManager cacheManager;

  @Test
  void readOnlyLookups_areCachedInRedis_andSurviveJsonRoundTrip() throws Exception {
    assertInstanceOf(RedisCacheManager.class, cacheManager);
    String token = signUpAndSignIn("cacher");

    for (int i = 0; i < 2; i++) { // second call is served from Redis and must deserialize
      mockMvc.perform(get("/api/towns").with(bearer(token))).andExpect(status().isOk());
    }

    var cached = cacheManager.getCache("towns").get(SimpleKey.EMPTY);
    assertNotNull(cached);
    assertInstanceOf(List.class, cached.get());
  }
}
