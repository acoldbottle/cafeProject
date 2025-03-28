package cafeLogProject.cafeLog.api.user.elasticsearch;

import cafeLogProject.cafeLog.api.user.dto.UserSearchRes;
import cafeLogProject.cafeLog.common.exception.user.UserNicknameException;
import cafeLogProject.cafeLog.common.exception.user.UserNotFoundException;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cafeLogProject.cafeLog.common.exception.ErrorCode.USER_NICKNAME_NULL_ERROR;
import static cafeLogProject.cafeLog.common.exception.ErrorCode.USER_NOT_FOUND_ERROR;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ElasticsearchService {

    private final UserRepository userRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Transactional
    public void saveAllNickname() {

        List<NicknameDocument> nicknameDocuments = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            nicknameDocuments.add(NicknameDocument.from(user));
        }
        int batchSize = 2000;
        for (int i = 0; i < nicknameDocuments.size(); i += batchSize) {
            int end = Math.min(i + batchSize, nicknameDocuments.size());
            List<NicknameDocument> batch = nicknameDocuments.subList(i, end);

            List<IndexQuery> queries = new ArrayList<>();
            for (NicknameDocument document : batch) {
                IndexQuery indexQuery = new IndexQueryBuilder()
                        .withId(String.valueOf(document.getId()))
                        .withObject(document)
                        .build();
                queries.add(indexQuery);
            }
            elasticsearchOperations.bulkIndex(queries, NicknameDocument.class);
        }
    }

    public List<UserSearchRes> searchUserByNickname(String searchNickname, String currentUsername) {

        if (searchNickname == null || searchNickname.trim().isEmpty()) {
            throw new UserNicknameException(USER_NICKNAME_NULL_ERROR);
        }
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        Object[] searchAfter = null;
        String SEARCH_FIELD_ID = "id";
        String SEARCH_FIELD_NAME = "nickname";
        int batchSize = 2000;
        List<UserSearchRes> userSearchResList = new ArrayList<>();

        Criteria criteria = Criteria.where(SEARCH_FIELD_NAME).is(searchNickname);
        CriteriaQuery query = new CriteriaQuery(criteria)
                .addSort(Sort.by(SEARCH_FIELD_ID))
                .setPageable(PageRequest.ofSize(batchSize));

        while (true) {

            if (searchAfter != null) {
                query.setSearchAfter(List.of(searchAfter));
            }

            SearchHits<NicknameDocument> searchHits = elasticsearchOperations.search(query, NicknameDocument.class, IndexCoordinates.of("nicknames"));

            if (searchHits.hasSearchHits()) {
                List<UserSearchRes> newResults = searchHits.getSearchHits().stream()
                        .map(hit -> UserSearchRes.from(hit.getContent())) // 결과 변환
                        .collect(Collectors.toList());
                userSearchResList.addAll(newResults);

                searchAfter = searchHits.getSearchHits().get(searchHits.getSearchHits().size() - 1).getSortValues().toArray();
            } else {
                break;
            }
        }

        return userRepository.searchUserByNickname(searchNickname, user.getId(), userSearchResList);
    }

}
