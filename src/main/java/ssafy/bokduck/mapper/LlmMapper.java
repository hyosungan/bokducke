package ssafy.bokduck.mapper;

import org.apache.ibatis.annotations.Mapper;
import ssafy.bokduck.dto.LlmRequestDto;

@Mapper
public interface LlmMapper {
    void insertRequest(LlmRequestDto request);

    LlmRequestDto findById(Long llmRequestId);
}
