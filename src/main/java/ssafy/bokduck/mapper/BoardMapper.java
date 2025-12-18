package ssafy.bokduck.mapper;

import org.apache.ibatis.annotations.Mapper;
import ssafy.bokduck.dto.BoardPostDto;
import ssafy.bokduck.dto.BoardCommentDto;
import java.util.List;

@Mapper
public interface BoardMapper {
    // Posts
    void insertPost(BoardPostDto post);

    List<BoardPostDto> findAllPosts();

    BoardPostDto findPostById(Long postId);

    void updatePost(BoardPostDto post);

    void deletePost(Long postId);

    void incrementViewCount(Long postId);

    // Comments
    void insertComment(BoardCommentDto comment);

    List<BoardCommentDto> findCommentsByPostId(Long postId);

    void deleteComment(Long commentId);
}
