package cafeLogProject.cafeLog.api.common;

import cafeLogProject.cafeLog.api.user.dto.UserInfoRes;
import cafeLogProject.cafeLog.common.exception.follow.FollowNoMoreException;
import cafeLogProject.cafeLog.common.exception.user.UserNotFoundException;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.concurrent.TimeUnit;

import static cafeLogProject.cafeLog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CacheService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Cacheable(value = "cacheUser", key = "'users:' + #userId + ':info'")
    public UserInfoRes getUserBasicInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        return UserInfoRes.from(user);
    }

    public void cacheLastResult(Long currentUserId, Long otherUserId, Integer isFollow, Long follow_id, String type) {

        String key = type + ":" + otherUserId + ":by:" + currentUserId;
        String value = isFollow + ":" + follow_id;
        redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES);
    }

    public String cachingLastResult(Long currentUserId, Long otherUserId, String type) {
        String key = type + ":" + otherUserId + ":by:" + currentUserId;
        String value = redisTemplate.opsForValue().get(key);

        return value;
    }

    public void deleteLastResult(Long currentUserId, Long otherUserId, String type) {
        String key = type + ":" + otherUserId + ":by:" + currentUserId;
        redisTemplate.delete(key);
    }
}
