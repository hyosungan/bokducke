package ssafy.bokduck.mapper;

import org.apache.ibatis.annotations.Mapper;
import ssafy.bokduck.dto.UserDto;
import ssafy.bokduck.dto.UserInterestDto;
import java.util.List;

@Mapper
public interface UserMapper {
    void insertUser(UserDto user);

    UserDto findByEmail(String email);

    UserDto findById(Long userId);

    void updateUser(UserDto user);

    void deleteUser(Long userId); // Logic delete or Status update? API says DELETE /profile but ERD has status. I
                                  // will imply status update.

    // Interests
    void insertInterest(UserInterestDto interest);

    List<UserInterestDto> findInterestsByUserId(Long userId);

    void deleteInterestsByUserId(Long userId);
}
