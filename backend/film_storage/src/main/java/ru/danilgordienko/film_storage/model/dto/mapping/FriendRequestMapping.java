package ru.danilgordienko.film_storage.model.dto.mapping;

import org.mapstruct.Mapper;
import ru.danilgordienko.film_storage.model.dto.FriendRequestDto;
import ru.danilgordienko.film_storage.model.entity.FriendRequest;

@Mapper(componentModel = "spring", uses = UserMapping.class)
public interface FriendRequestMapping {

    FriendRequestDto toFriendRequestDto(FriendRequest friendRequest);
}
