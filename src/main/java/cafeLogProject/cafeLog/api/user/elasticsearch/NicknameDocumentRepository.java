package cafeLogProject.cafeLog.api.user.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NicknameDocumentRepository extends ElasticsearchRepository<NicknameDocument, Long> {

}
