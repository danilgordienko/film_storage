package ru.danilgordienko.film_storage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> content;
    private int number;
    private long totalElements;
}
