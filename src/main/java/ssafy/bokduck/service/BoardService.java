package ssafy.bokduck.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.bokduck.dto.BoardPostDto;
import ssafy.bokduck.mapper.BoardMapper;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BoardService {

    private final BoardMapper boardMapper;

    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    public List<BoardPostDto> getPosts(Map<String, Object> params) {
        return boardMapper.findAllPosts();
    }

    public BoardPostDto getPostDetail(Long postId) {
        boardMapper.incrementViewCount(postId);
        return boardMapper.findPostById(postId);
    }

    public void createPost(BoardPostDto postDto) {
        boardMapper.insertPost(postDto);
    }

    public void updatePost(BoardPostDto postDto) {
        boardMapper.updatePost(postDto);
    }

    public void deletePost(Long postId) {
        boardMapper.deletePost(postId);
    }
}
