package ru.danilgordienko.film_storage.DTO.mapping;

import org.mapstruct.Mapper;
import ru.danilgordienko.film_storage.DTO.FriendRequestDto;
import ru.danilgordienko.film_storage.model.FriendRequest;

@Mapper(componentModel = "spring", uses = UserMapping.class)
public interface FriendRequestMapping {

    FriendRequestDto toFriendRequestDto(FriendRequest friendRequest);
}
