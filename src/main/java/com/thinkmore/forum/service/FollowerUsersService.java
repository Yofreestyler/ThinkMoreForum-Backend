package com.thinkmore.forum.service;

import com.thinkmore.forum.dto.followerUsers.FollowerUsersGetDto;
import com.thinkmore.forum.entity.FollowUser;
import com.thinkmore.forum.entity.Users;
import com.thinkmore.forum.exception.UserNotFoundException;
import com.thinkmore.forum.mapper.FollowerUsersMapper;
import com.thinkmore.forum.repository.FollowerUsersRepository;
import com.thinkmore.forum.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowerUsersService {
    private final FollowerUsersRepository followerUsersRepository;
    private final UsersRepository usersRepository;
    private final FollowerUsersMapper followerUsersMapper;

    public List<FollowerUsersGetDto> getFollowersByUsername(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid UserName"));
        return followerUsersRepository.findAllByUsersId(user.getId()).stream().map(followerUsersMapper::fromEntity).collect(Collectors.toList());
    }

    public List<FollowerUsersGetDto> getFriendsByUsername(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid UserName"));
        return followerUsersRepository.findAllByFollowedUsersId(user.getId()).stream().map(followerUsersMapper::fromEntity).collect(Collectors.toList());
    }

    public FollowerUsersGetDto followUsers(String username, UUID usersId) {
        Users tampUser = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid UserName"));
        FollowUser followUser = new FollowUser();

        Users user = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserNotFoundException("Invalid UserID"));
        Users followedUser = usersRepository.findById(tampUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Invalid UserID"));
        if (followerUsersRepository.findByUsersIdAndFollowedUsersId(usersId, tampUser.getId()).isEmpty()) {
            followUser.setUsers(user);
            followUser.setFollowedUsers(followedUser);
            followUser.setCreateTimestamp(OffsetDateTime.now());

            followerUsersRepository.save(followUser);
        } else {
            System.out.println("You have already followed this user");
        }

        return followerUsersMapper.fromEntity(followUser);
    }

    public void unfollowUsers(String username, String followedUsername) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid UserName"));
        Users followedUser = usersRepository.findByUsername(followedUsername)
                .orElseThrow(() -> new UserNotFoundException("Invalid UserName"));
        followerUsersRepository.deleteByUsersIdAndFollowedUsersId(user.getId(), followedUser.getId());
    }

    public boolean followStatus(String username, UUID usersId) {
        Users tampUser = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid UserName"));

        boolean status;
        status = !followerUsersRepository.findByUsersIdAndFollowedUsersId(usersId, tampUser.getId()).isEmpty();
        return status;
    }
}
