package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.bokduck.dto.BoardPostDto;
import ssafy.bokduck.service.BoardService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public ResponseEntity<List<BoardPostDto>> getPosts(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(boardService.getPosts(params));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<BoardPostDto> getPostDetail(@PathVariable Long postId) {
        BoardPostDto post = boardService.getPostDetail(postId);
        if (post != null) {
            return ResponseEntity.ok(post);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody BoardPostDto postDto) {
        boardService.createPost(postDto);
        return ResponseEntity.ok("Post created");
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody BoardPostDto postDto) {
        postDto.setPostId(postId);
        boardService.updatePost(postDto);
        return ResponseEntity.ok("Post updated");
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        boardService.deletePost(postId);
        return ResponseEntity.ok("Post deleted");
    }
}
